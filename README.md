# 🐝 WanderBee - Your Smart Travel Companion

A modern Android travel application built with Jetpack Compose, featuring AI-powered travel planning, real-time chat with fellow travelers, and comprehensive destination information.

## 📱 App Overview

WanderBee is an intelligent travel companion that helps users discover destinations, plan trips with AI assistance, connect with other travelers, and organize their travel experiences. The app combines cutting-edge AI technology with a beautiful, intuitive user interface.

## ✨ Key Features

### 🎯 Core Features
- **AI-Powered Travel Planning** - Generate personalized itineraries using AI
- **Destination Discovery** - Explore cities with detailed information, photos, and videos
- **Real-time Chat System** - Connect with fellow travelers in destination-specific chat rooms
- **Smart Search** - Find destinations with intelligent search and filtering
- **User Profiles** - Manage travel preferences and personal information
- **Save & Organize** - Bookmark favorite destinations for future reference
- **Push Notifications** - Stay updated with travel deals, weather alerts, and chat messages

### 🎨 User Experience
- **Beautiful Onboarding** - Engaging introduction screens for new users
- **Smooth Animations** - Fluid transitions and micro-interactions
- **Dark Theme** - Modern dark UI with vibrant accents
- **Responsive Design** - Optimized for all screen sizes
- **Offline Support** - Cached data for offline access

## 🏗️ Technical Architecture

### 🛠️ Tech Stack
- **UI Framework**: Jetpack Compose
- **Language**: Kotlin
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **Database**: Room (Local) + Firebase Firestore (Remote)
- **Authentication**: Firebase Auth
- **Image Loading**: Coil
- **Networking**: Retrofit + OkHttp
- **Navigation**: Compose Navigation
- **State Management**: Kotlin Flow
- **Notifications**: Firebase Cloud Messaging (FCM)

### 📁 Project Structure
```
app/src/main/java/com/example/wanderbee/
├── data/
│   ├── cache/           # In-memory caching
│   ├── local/           # Room database entities and DAOs
│   ├── remote/          # API services and models
│   └── repository/      # Data repositories
├── di/                  # Dependency injection modules
├── navigation/          # Navigation components
├── screens/             # UI screens organized by feature
│   ├── authentication/  # Login, signup, forgot password
│   ├── chat/           # Chat functionality
│   ├── details/        # Destination details
│   ├── home/           # Main home screen
│   ├── itinerary/      # AI itinerary planning
│   ├── onboarding/     # Onboarding screens
│   ├── profile/        # User profile management
│   ├── saved/          # Saved destinations
│   └── splash/         # Splash screen
├── services/           # Background services (FCM)
├── ui/                 # Theme and UI components
├── utils/              # Utility classes and components
└── WanderBeeApp.kt     # Application class
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+
- Kotlin 1.8+
- Google Services account for Firebase

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/wanderbee.git
   cd wanderbee
   ```

2. **Set up Firebase**
   - Create a new Firebase project
   - Download `google-services.json` and place it in the `app/` directory
   - Enable Authentication, Firestore, and Cloud Messaging

3. **Configure API Keys**
   Create a `local.properties` file in the root directory:
   ```properties
   OPENWEATHER_API_KEY=your_openweather_api_key
   GEO_DB_API_KEY=your_geodb_api_key
   HUGGINGFACE_API_KEY=your_huggingface_api_key
   PEXELS_API_KEY=your_pexels_api_key
   AI_API_KEY=your_ai_api_key
   ```

4. **Build and Run**
   ```bash
   ./gradlew build
   ./gradlew installDebug
   ```

## 🔧 Configuration

### API Services Used
- **GeoDB API** - City and country information
- **OpenWeather API** - Weather data for destinations
- **Pexels API** - High-quality travel images and videos
- **HuggingFace API** - AI-powered text generation
- **Custom AI API** - Itinerary planning and recommendations

### Firebase Services
- **Authentication** - User login/signup
- **Firestore** - User data and chat messages
- **Cloud Messaging** - Push notifications
- **Storage** - User profile images

## 📱 Features Deep Dive

### 🎯 Splash Screen & Onboarding
- **Beautiful Animations** - Scale, fade, and slide animations
- **Smart Navigation** - Directs users based on login state and first launch
- **Modern Design** - Card-based layout with gradient backgrounds
- **Loading Indicators** - Animated dots with smooth transitions

### 🏠 Home Screen
- **Destination Search** - Intelligent search with debouncing and caching
- **Popular Destinations** - Curated list of trending places
- **Recent Searches** - Quick access to previously viewed destinations
- **Smooth Animations** - Fluid transitions between states

### 🔍 Destination Details
- **Comprehensive Information** - Currency, timezone, language, tags
- **Dynamic Data Fetching** - AI-generated content for non-static destinations
- **Photo Gallery** - High-quality images from Pexels
- **Video Content** - Travel videos and experiences
- **Cultural Tips** - AI-generated local insights

### 🤖 AI Itinerary Planning
- **Smart Recommendations** - AI-powered day-by-day planning
- **Cultural Integration** - Local customs and traditions
- **Flexible Scheduling** - Customizable time slots
- **Export Options** - Save and share itineraries

### 💬 Chat System
- **Destination-Specific Rooms** - Chat with travelers visiting the same place
- **Private Messaging** - Direct conversations with other users
- **Real-time Updates** - Live message synchronization
- **Media Support** - Share images and travel tips

### 👤 User Profile
- **Editable Information** - Update personal details and preferences
- **Travel Preferences** - Set preferred travel styles
- **Location Services** - Automatic location detection
- **Profile Pictures** - Upload and manage profile images

### 🔔 Push Notifications
- **Smart Categories** - Travel deals, weather alerts, chat messages
- **Topic Subscriptions** - Subscribe to relevant notification types
- **User Preferences** - Customizable notification settings
- **Real-time Delivery** - Instant message delivery

## 🎨 UI/UX Design

### Color Scheme
- **Primary Background**: Dark theme (`#1F1F26`)
- **Accent Color**: Yellow highlights (`#EEB644`)
- **Primary Button**: Blue (`#19DCFF`)
- **Text Colors**: White and light gray variants
- **Error Color**: Red (`#E45656`)

### Typography
- **Primary Font**: Istok Web (Regular & Bold)
- **Secondary Font**: Coustard Regular
- **Hierarchy**: Clear size and weight variations

### Animations
- **Smooth Transitions** - 300ms duration for most animations
- **Easing Curves** - Natural motion with EaseOutBack and EaseOutCubic
- **Micro-interactions** - Subtle feedback for user actions
- **Loading States** - Animated indicators for async operations

## 🔒 Security & Privacy

### Data Protection
- **Secure Storage** - Encrypted local database
- **API Security** - HTTPS-only network calls
- **User Privacy** - Minimal data collection
- **Authentication** - Firebase Auth with secure tokens

### Permissions
- **Location** - For nearby destination suggestions
- **Notifications** - For push notifications
- **Internet** - For API calls and real-time features
- **Storage** - For profile images and cached data

## 📊 Performance Optimization

### Caching Strategy
- **In-Memory Cache** - Fast access to frequently used data
- **Database Cache** - Persistent storage for offline access
- **Image Cache** - Efficient image loading with Coil
- **API Response Cache** - Reduce redundant network calls

### Network Optimization
- **Debouncing** - Prevent excessive API calls during search
- **Retry Logic** - Handle network failures gracefully
- **Rate Limiting** - Respect API rate limits
- **Compression** - Optimize data transfer

## 🧪 Testing

### Unit Tests
- **ViewModel Testing** - Business logic validation
- **Repository Testing** - Data layer verification
- **Utility Testing** - Helper function validation

### UI Tests
- **Compose Testing** - UI component validation
- **Navigation Testing** - Screen flow verification
- **Integration Testing** - End-to-end user flows

## 🚀 Deployment

### Build Variants
- **Debug** - Development with logging and debugging
- **Release** - Production-optimized build
- **Staging** - Pre-production testing

### Release Process
1. **Version Update** - Increment version code and name
2. **Build Generation** - Create signed APK/AAB
3. **Testing** - Comprehensive testing on multiple devices
4. **Store Submission** - Upload to Google Play Console

## 🤝 Contributing

### Development Setup
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new features
5. Submit a pull request

### Code Style
- **Kotlin Style Guide** - Follow official Kotlin conventions
- **Compose Guidelines** - Use recommended Compose patterns
- **Architecture Patterns** - Maintain MVVM and Clean Architecture
- **Documentation** - Add comments for complex logic


## 🙏 Acknowledgments

- **Firebase** - Backend services and authentication
- **Jetpack Compose** - Modern UI framework
- **Unsplash** - High-quality travel images
- **Pexels** - Stock photos and videos
- **OpenWeather** - Weather data API
- **GeoDB** - Geographic data API

**Made with ❤️ for travelers around the world**
