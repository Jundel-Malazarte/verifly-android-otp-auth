# Project Plan

Mobile app in Java with Login page with OTP, Signup page, and a basic Dashboard.

## Project Brief

# Project Brief: Verifly Android App (MVP)

## Features
1. **User Signup**: Registration screen for new users to input profile details and create an account.
2. **OTP Login & Verification**: Login page supporting phone/email input, One-Time Password (OTP) request, and code verification.
3. **User Dashboard**: Main landing screen displayed after authentication, featuring user profile details and core navigation options.

## High-Level Tech Stack
- **Language**: Java
- **UI Framework**: Classic Android Views with XML Layouts & Material Components for Android
- **View Binding**: Android ViewBinding
- **Architecture & Lifecycle**: Activity / Fragment architecture with ViewModel and LiveData
- **Navigation**: Jetpack Navigation Component (Fragment-based Navigation)
- **Concurrency**: Java Concurrency (`ExecutorService` / `CompletableFuture`)

## Implementation Steps
**Total Duration:** 15m 29s

### Task_1_SetupNavigationAndDataLayer: Set up ViewBinding, Jetpack Navigation graph, data models, and Auth/Session Repository with ViewModel and LiveData using Java.
- **Status:** COMPLETED
- **Updates:** ViewBinding enabled, dependencies added, User model, AuthRepository, AuthViewModel created in Java, nav_graph created, MainActivity and Fragments instantiated with ViewBinding, build verified.
- **Acceptance Criteria:**
  - ViewBinding and Jetpack Navigation configured
  - User repository and Auth ViewModel logic implemented
  - build pass
- **Duration:** 10m 20s

### Task_2_ImplementAuthFlowUI: Implement Login Fragment (with OTP request and verification flow) and Signup Fragment with XML layouts and Material components.
- **Status:** COMPLETED
- **Updates:** LoginFragment with 2-state OTP request/verification flow, auto-fill test OTP snackbar, resend/change contact actions, and SignupFragment with full field validation, ViewModel integration, and Material 3 layouts implemented in Java and compiled successfully.
- **Acceptance Criteria:**
  - Login UI supports phone/email input and OTP request/verification flow
  - Signup UI supports user registration
  - Navigation between Login and Signup screens functions correctly
  - build pass
- **Duration:** 2m 54s

### Task_3_ImplementDashboardUI: Implement Dashboard Fragment displaying user profile details, core navigation options, and logout functionality.
- **Status:** COMPLETED
- **Updates:** DashboardFragment and layout implemented with user profile card, 2x2 action grid, logout functionality, and nav_graph backstack popping. Build verified successfully.
- **Acceptance Criteria:**
  - Dashboard screen displays user profile information post-authentication
  - Logout action returns user to Login screen
  - build pass
- **Duration:** 2m 15s

### Task_4_RunAndVerify: Run and verify application stability, instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - All features (Signup, OTP Login, Dashboard) verified end-to-end
- **StartTime:** 2026-10-24 12:42:03 GMT+08:00

