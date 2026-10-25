package com.example.verifly_android.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.example.verifly_android.data.db.DatabaseHelper;
import com.example.verifly_android.data.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AuthRepository {
    private static final String PREF_NAME = "verifly_auth_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";

    private static AuthRepository instance;
    private final SharedPreferences prefs;
    private final DatabaseHelper dbHelper;
    private final Map<String, String> pendingOtps = new HashMap<>();
    private User currentUser;

    private AuthRepository(Context context) {
        Context appContext = context.getApplicationContext();
        this.prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.dbHelper = DatabaseHelper.getInstance(appContext);
        loadSessionFromPrefs();
    }

    public static synchronized AuthRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AuthRepository(context);
        }
        return instance;
    }

    private void loadSessionFromPrefs() {
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        if (isLoggedIn) {
            String id = prefs.getString(KEY_USER_ID, "");
            if (id != null && !id.isEmpty()) {
                currentUser = dbHelper.getUserById(id);
            }
        }
    }

    public synchronized boolean isUserRegistered(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }
        return dbHelper.isIdentifierRegistered(identifier.trim());
    }

    @Nullable
    public synchronized String generateOtp(String target) {
        if (target == null || target.trim().isEmpty()) {
            return null;
        }

        String cleanedTarget = target.trim();
        // Check if user is registered before generating OTP
        if (!isUserRegistered(cleanedTarget)) {
            return null;
        }

        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        String otpStr = String.valueOf(otp);
        pendingOtps.put(cleanedTarget.toLowerCase(), otpStr);
        return otpStr;
    }

    public synchronized boolean verifyOtp(String target, String otpCode) {
        if (target == null || otpCode == null) return false;
        String cleanedTarget = target.trim();
        String storedOtp = pendingOtps.get(cleanedTarget.toLowerCase());

        if (storedOtp != null && storedOtp.equals(otpCode.trim())) {
            User user = dbHelper.getUserByEmailOrPhone(cleanedTarget);
            if (user == null) {
                // Do not allow un-registered users to login via OTP
                return false;
            }

            pendingOtps.remove(cleanedTarget.toLowerCase());
            user.setVerified(true);
            dbHelper.updateUserVerification(user.getId(), true);

            this.currentUser = user;
            saveSession(user);
            return true;
        }
        return false;
    }

    public synchronized boolean signUp(String fullName, String email, String phoneNumber, String password) {
        String cleanEmail = email != null ? email.trim() : "";
        String cleanPhone = phoneNumber != null ? phoneNumber.trim() : "";

        if (isUserRegistered(cleanEmail) || (!cleanPhone.isEmpty() && isUserRegistered(cleanPhone))) {
            return false; // User already exists
        }

        String id = UUID.randomUUID().toString();
        User user = new User(id, fullName.trim(), cleanEmail, cleanPhone, password, false, System.currentTimeMillis());

        boolean inserted = dbHelper.insertUser(user);
        if (inserted) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    public synchronized boolean login(String identifier, String password) {
        if (identifier == null || password == null) return false;
        String cleaned = identifier.trim();

        User user = dbHelper.getUserByEmailOrPhone(cleaned);
        if (user != null && user.getPassword() != null && user.getPassword().equals(password)) {
            this.currentUser = user;
            saveSession(user);
            return true;
        }
        return false;
    }

    public synchronized void logout() {
        this.currentUser = null;
        prefs.edit().clear().apply();
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null && prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    private void saveSession(User user) {
        if (user == null) return;
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_ID, user.getId())
                .apply();
    }
}
