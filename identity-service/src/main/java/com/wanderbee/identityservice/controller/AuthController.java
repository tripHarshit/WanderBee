package com.wanderbee.identityservice.controller;

import com.wanderbee.identityservice.dto.AuthRequest;
import com.wanderbee.identityservice.dto.GoogleAuthResponse;
import com.wanderbee.identityservice.dto.GoogleTokenRequest;
import com.wanderbee.identityservice.entity.UserCredentials;
import com.wanderbee.identityservice.service.AuthService;
import com.wanderbee.identityservice.service.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

        @Autowired
        private AuthService service;

        @Autowired
        private GoogleAuthService googleAuthService;

        @Autowired
        private AuthenticationManager authenticationManager;

        @PostMapping("/register")
        public String addNewUser(@RequestBody UserCredentials user) {
            return service.saveUser(user);
        }

        @PostMapping("/login")
        public String login(@RequestBody AuthRequest authRequest) {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
            if (authenticate.isAuthenticated()) {
                return service.generateToken(authRequest.getEmail());
            } else {
                throw new RuntimeException("Invalid username or password");
            }
        }

        @PostMapping("/token")
        public String getToken(@RequestBody AuthRequest authRequest) {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
            if (authenticate.isAuthenticated()) {
                return service.generateToken(authRequest.getEmail());
            } else {
                throw new RuntimeException("invalid access");
            }
        }

        @GetMapping("/validate")
        public String validateToken(@RequestParam("token") String token) {
            service.validateToken(token);
            return "Token is valid";
        }

        /**
         * Handles Google OAuth2 login from mobile app.
         * Validates the Google ID token and returns a WanderBee JWT.
         * Creates a new user if they don't exist in the system.
         */
        @PostMapping("/google-login")
        public ResponseEntity<GoogleAuthResponse> googleLogin(@RequestBody GoogleTokenRequest request) {
            GoogleAuthResponse response = googleAuthService.authenticateGoogleUser(request.getIdToken());
            return ResponseEntity.ok(response);
        }
}
