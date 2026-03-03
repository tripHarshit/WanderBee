package com.wanderbee.identityservice.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.wanderbee.identityservice.dto.GoogleAuthResponse;
import com.wanderbee.identityservice.entity.UserCredentials;
import com.wanderbee.identityservice.repository.UserCredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoogleAuthService {

    @Value("${google.client-id}")
    private String googleClientId;

    @Autowired
    private UserCredentialsRepository userCredentialsRepository;

    @Autowired
    private JwtService jwtService;

    /**
     * Validates the Google ID token and returns a WanderBee JWT.
     * Creates a new user if they don't exist in the system.
     */
    public GoogleAuthResponse authenticateGoogleUser(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), 
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            
            if (idToken == null) {
                throw new RuntimeException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            // Check if user exists in the database
            Optional<UserCredentials> existingUser = userCredentialsRepository.findByEmail(email);
            boolean isNewUser = existingUser.isEmpty();

            if (isNewUser) {
                // Create new user for Google OAuth
                UserCredentials newUser = new UserCredentials();
                newUser.setEmail(email);
                newUser.setName(name != null ? name : email.split("@")[0]);
                // Generate a random password for OAuth users (they won't use password login)
                newUser.setPassword(UUID.randomUUID().toString());
                newUser.setAuthProvider("GOOGLE");
                newUser.setProfilePictureUrl(pictureUrl);
                userCredentialsRepository.save(newUser);
            } else {
                // Update existing user's profile picture if changed
                UserCredentials user = existingUser.get();
                if (pictureUrl != null && !pictureUrl.equals(user.getProfilePictureUrl())) {
                    user.setProfilePictureUrl(pictureUrl);
                    userCredentialsRepository.save(user);
                }
            }

            // Generate WanderBee JWT token
            String token = jwtService.generateToken(email);

            return GoogleAuthResponse.builder()
                    .token(token)
                    .email(email)
                    .name(name)
                    .newUser(isNewUser)
                    .build();

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to verify Google ID token: " + e.getMessage(), e);
        }
    }
}
