# Verifly Android OTP Auth

Verifly is a small Android authentication demo built with Java, Kotlin, and Android
Jetpack libraries. It demonstrates account registration, one-time-password (OTP)
verification, session persistence, and a profile dashboard.

> **Prototype only:** OTPs are generated and shown inside the app for testing. The
> project does not send email or SMS messages and is not suitable for production
> authentication. See [Security notes](#security-notes).

## Features

- Register with a name, email address, phone number, and password.
- Validate email and phone inputs and require passwords with at least eight
  characters, including uppercase and lowercase letters, a digit, and a symbol.
- Request and verify a six-digit OTP for a registered email address or phone
  number. The demo displays the generated code in a snackbar, with an auto-fill
  action for convenient testing.
- Persist user records in a local SQLite database and the login session in
  `SharedPreferences`.
- View account details and verification status on the dashboard, and log out.

## Requirements

- Android Studio with Android SDK Platform 37 installed.
- A Gradle JDK compatible with the Android Gradle Plugin version in this
  project; Android Studio's bundled JDK is a suitable starting point.
- An Android device or emulator running Android 10 (API 29) or newer.

## Run the app

1. Open this project in Android Studio and allow Gradle to sync.
2. Select an Android 10+ device or emulator.
3. Run the `app` configuration.

To build and test from a terminal:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

The debug APK is written to `app/build/outputs/apk/debug/`.

## Try the authentication flow

1. Create an account from the sign-up screen. Use a password that satisfies the
   rules listed above.
2. On the login screen, enter the account's email address or phone number and
   request an OTP.
3. Use the code shown in the **Generated Test OTP** snackbar. Select **AUTO-FILL**
   to copy and fill it, or enter it manually.
4. Verify the code to open the dashboard. Log out to return to login.

The app also has password-authentication logic in its repository, but the current
login screen is centered on OTP verification.

## Project structure

```text
app/src/main/
├── java/com/example/verifly_android/
│   ├── data/
│   │   ├── db/          # SQLiteOpenHelper and user persistence
│   │   ├── model/       # User model
│   │   └── repository/  # Registration, OTP, and session operations
│   ├── ui/              # Login, sign-up, and dashboard fragments
│   ├── util/            # LiveData event helper
│   ├── viewmodel/       # Authentication state and validation
│   └── MainActivity.java
└── res/
    ├── layout/          # Screen layouts
    └── navigation/      # Navigation graph
```

The UI is built from Android views and XML layouts, with AndroidX Navigation,
ViewModel, and LiveData managing screen transitions and observable state. User
records are stored by `DatabaseHelper`; `AuthRepository` coordinates
authentication and persists the signed-in user ID in `SharedPreferences`.

## Tests

The local unit-test suite can be run with `./gradlew testDebugUnitTest`. It
currently includes password-rule checks and a six-digit OTP-format check.

## Security notes

This project is for learning and local demonstrations, not real-world identity
verification:

- OTPs are generated on-device and displayed in the UI; no trusted delivery or
  remote verification service is involved.
- User records, including passwords, are stored locally by the demo. Passwords
  are not hashed, and the local database is not a secure credential store.
- The local OTP implementation has no server-side rate limiting or expiry.

Do not use real credentials or rely on this app to protect accounts. A production
implementation needs a trusted authentication backend, secure password handling,
and a verified email/SMS delivery provider.