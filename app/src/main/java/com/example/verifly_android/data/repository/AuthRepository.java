package com.example.verifly_android.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.verifly_android.data.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AuthRepository {
    private static final String PREF_NAME = "verifly_auth_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_VERIFIED = "user_verified";

    private static AuthRepository instance;
    private final SharedPreferences prefs;
    private final Map<String, User> registeredUsers = new HashMap<>();
    private final Map<String, String> pendingOtps = new HashMap<>();
    private User currentUser;

    private AuthRepository(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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
            String name = prefs.getString(KEY_USER_NAME, "");
            String email = prefs.getString(KEY_USER_EMAIL, "");
            String phone = prefs.getString(KEY_USER_PHONE, "");
            boolean isVerified = prefs.getBoolean(KEY_USER_VERIFIED, false);

            currentUser = new User(id, name, email, phone, "", isVerified, System.currentTimeMillis());
            if (!email.isEmpty()) {
                registeredUsers.put(email, currentUser);
            }
            if (!phone.isEmpty()) {
                registeredUsers.put(phone, currentUser);
            }
        }
    }

    public synchronized String generateOtp(String target) {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        String otpStr = String.valueOf(otp);
        pendingOtps.put(target, otpStr);
        return otpStr;
    }

    public synchronized boolean verifyOtp(String target, String otpCode) {
        if (target == null || otpCode == null) return false;
        String storedOtp = pendingOtps.get(target);
        if (storedOtp != null && storedOtp.equals(otpCode.trim())) {
            pendingOtps.remove(target);
            User user = registeredUsers.get(target);
            if (user == null) {
                for (User u : registeredUsers.values()) {
                    if ((u.getEmail() != null && u.getEmail().equalsIgnoreCase(target)) ||
                        (u.getPhoneNumber() != null && u.getPhoneNumber().equals(target))) {
                        user = u;
                        break;
                    }
                }
            }
            if (user == null) {
                String id = UUID.randomUUID().toString();
                boolean isEmail = target.contains("@");
                String email = isEmail ? target : "";
                String phone = isEmail ? "" : target;
                user = new User(id, "User " + target, email, phone, "", true, System.currentTimeMillis());
                registeredUsers.put(target, user);
            } else {
                user.setVerified(true);
            }
            this.currentUser = user;
            saveSession(user);
            return true;
        }
        return false;
    }

    public synchronized boolean signUp(String fullName, String email, String phoneNumber, String password) {
        if (registeredUsers.containsKey(email) || (phoneNumber != null && !phoneNumber.isEmpty() && registeredUsers.containsKey(phoneNumber))) {
            return false; // User already exists
        }

        String id = UUID.randomUUID().toString();
        User user = new User(id, fullName, email, phoneNumber, password, false, System.currentTimeMillis());
        registeredUsers.put(email, user);
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            registeredUsers.put(phoneNumber, user);
        }

        this.currentUser = user;
        saveSession(user);
        return true;
    }

    public synchronized boolean login(String identifier, String password) {
        User user = registeredUsers.get(identifier);
        if (user != null && user.getPassword() != null && user.getPassword().equals(password)) {
            this.currentUser = user;
            saveSession(user);
            return true;
        }
        if (currentUser != null && (identifier.equalsIgnoreCase(currentUser.getEmail()) || identifier.equals(currentUser.getPhoneNumber()))) {
            saveSession(currentUser);
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
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_ID, user.getId())
                .putString(KEY_USER_NAME, user.getFullName())
                .putString(KEY_USER_EMAIL, user.getEmail())
                .putString(KEY_USER_PHONE, user.getPhoneNumber())
                .putBoolean(KEY_USER_VERIFIED, user.isVerified())
                .apply();
    }
}
