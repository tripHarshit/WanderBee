# Software Requirements Specification (SRS)

## 1. Introduction

### 1.1 Purpose
The purpose of this document is to define the software requirements for "WanderBee", an AI-powered travel assistant Android application. This app aims to simplify travel planning by integrating itinerary generation, cultural tips, language assistance, and social features such as traveler matchmaking and chat.

### 1.2 Scope
WanderBee is an Android mobile application that leverages AI and real-time data to help users plan and optimize their travel experiences. It includes smart itinerary creation, cultural briefings, basic travel phrase translations, user matchmaking based on destinations, and real-time chat.

### 1.3 Intended Audience
- Developers and testers involved in app development
- Reviewers for academic/project evaluation
- Resume/portfolio viewers

### 1.4 Definitions and Acronyms
- **AI**: Artificial Intelligence
- **UI**: User Interface
- **DI**: Dependency Injection
- **CI/CD**: Continuous Integration / Continuous Deployment
- **MVVM**: Model-View-ViewModel

---

## 2. Overall Description

### 2.1 Product Perspective
WanderBee is a standalone mobile application built using Kotlin and Jetpack Compose. It integrates Firebase services and utilizes open-source or free REST APIs for data and AI features.

### 2.2 Product Functions
- User authentication and profile management
- AI-generated itineraries
- AI-generated cultural tips
- Travel language assistant
- Matching travelers visiting the same destination
- Real-time chat functionality
- Bookmark/favorite feature
- Offline support for itineraries

### 2.3 User Classes and Characteristics
- **Solo Travelers**: Independent users seeking smart travel planning.
- **Backpackers**: Low-budget travelers needing cultural and linguistic support.
- **Groups**: Users traveling with friends and looking to coordinate plans.

### 2.4 Operating Environment
- Android 8.0 (API 26) and above
- Firebase backend services
- REST APIs for AI and data retrieval

### 2.5 Constraints
- Limited to mobile (no web counterpart initially)
- AI responses rely on API availability
- Budget restrictions limit use of paid APIs or Play Store publication

### 2.6 Assumptions and Dependencies
- Free APIs (OpenTripMap, GeoDB Cities) will remain accessible
- Firebase services will be sufficient for user and data management

---

## 3. Specific Requirements

### 3.1 Functional Requirements
| ID | Requirement | Priority | Description |
|----|-------------|----------|-------------|
| FR-1 | User Authentication | High | Login/Sign-up with Firebase Auth |
| FR-2 | User Profile | High | Create/edit user travel profile |
| FR-3 | Itinerary Generator | High | AI generates personalized plans |
| FR-4 | Cultural Tips | Medium | Display AI-based cultural insights |
| FR-5 | Language Assistant | Medium | Translate basic travel phrases |
| FR-6 | Traveler Matchmaking | Medium | Show users going to same place/time |
| FR-7 | Chat System | Medium | Real-time messaging between users |
| FR-8 | Bookmark Trips | Medium | Save trips locally and on cloud |
| FR-9 | Offline Mode | High | Use Room DB to access offline data |
| FR-10 | App UI | High | Jetpack Compose-driven modern UI |

### 3.2 Non-Functional Requirements
| ID | Requirement | Priority | Description |
|----|-------------|----------|-------------|
| NFR-1 | Performance | High | Screen transitions in <2s |
| NFR-2 | Scalability | Medium | Firebase handles increasing users |
| NFR-3 | Security | High | Secure Firebase rules for auth/data |
| NFR-4 | CI/CD Pipeline | Medium | GitHub Actions to auto test/build |
| NFR-5 | Maintainability | High | Follow MVVM and modular structure |

### 3.3 External Interface Requirements
- Firebase SDKs
- REST API access to AI and travel data services
- Device storage access for offline data

---

## 4. System Features

### 4.1 AI Travel Itinerary
- Input: Destination, duration, interests
- Output: Day-wise trip plan with activities

### 4.2 Cultural Tips
- Input: Destination
- Output: Do’s & don’ts, etiquette, dress code, etc.

### 4.3 Travel Language Assistant
- Input: Common phrases, destination language
- Output: Translated phrases (offline or via API)

### 4.4 Real-time Chat & Matchmaking
- Input: Destination and dates
- Output: List of other travelers, chat interface

### 4.5 Saved Plans
- Ability to save & retrieve past or favorite trips
- Works offline via Room + Firestore sync

---

## 5. Agile Workflow Summary

### Tools
- Version Control: GitHub
- Project Board: GitHub Projects / Notion
- CI/CD: GitHub Actions
- Design: Figma (optional)

### Sprint Cycle
- Sprint 1: Auth + Basic UI Navigation
- Sprint 2: Itinerary & Tips Integration
- Sprint 3: Travel Assistant + Bookmarks
- Sprint 4: Chat & Social Features
- Sprint 5: Testing, CI/CD & Final Review

---

## 6. Appendices
- A. APIs: OpenTripMap, GeoDB Cities, LibreTranslate
- B. Firebase: Authentication, Firestore, Realtime DB
- C. Hilt: For DI across app layers
- D. Testing Frameworks: JUnit, Compose UI Tests

---

**Document Owner:** [Your Name]  
**Version:** 1.0  
**Date:** [Fill based on project start]  

> ✅ Next Step (when you're ready): Feature-wise Sprint Task Breakdown + GitHub Repo Setup
