package com.example.verifly_android.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.example.verifly_android.data.db.DatabaseHelper;
import com.example.verifly_android.data.model.ChecklistItem;
import com.example.verifly_android.data.model.Customer;
import com.example.verifly_android.data.model.User;
import com.example.verifly_android.data.remote.SupabaseClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AuthRepository {
    private static final String PREF_NAME = "verifly_auth_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";

    public enum SignupResult {
        SUCCESS,
        EMAIL_EXISTS,
        PHONE_EXISTS,
        FAILED
    }

    private static AuthRepository instance;
    private final SharedPreferences prefs;
    private final DatabaseHelper dbHelper;
    private final SupabaseClient supabaseClient;
    private final Map<String, String> pendingOtps = new HashMap<>();
    private User currentUser;

    private AuthRepository(Context context) {
        Context appContext = context.getApplicationContext();
        this.prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.dbHelper = DatabaseHelper.getInstance(appContext);
        this.supabaseClient = SupabaseClient.getInstance(appContext);
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

    public synchronized boolean isEmailRegistered(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return dbHelper.isEmailRegistered(email.trim().toLowerCase());
    }

    public synchronized boolean isPhoneRegistered(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        return dbHelper.isPhoneRegistered(phone.trim());
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

        String cleanedTarget = target.trim().toLowerCase();
        User user = dbHelper.getUserByEmailOrPhone(cleanedTarget);
        if (user == null) {
            return null;
        }

        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        String otpStr = String.valueOf(otp);

        // Store OTP associated with target, user's email, and user's phone
        pendingOtps.put(cleanedTarget, otpStr);
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            pendingOtps.put(user.getEmail().trim().toLowerCase(), otpStr);
        }
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) {
            pendingOtps.put(user.getPhoneNumber().trim().toLowerCase(), otpStr);
        }
        return otpStr;
    }

    public synchronized boolean verifyOtp(String target, String otpCode) {
        if (target == null || target.trim().isEmpty() || otpCode == null || otpCode.trim().isEmpty()) {
            return false;
        }
        String cleanedTarget = target.trim().toLowerCase();
        String storedOtp = pendingOtps.get(cleanedTarget);

        if (storedOtp != null && storedOtp.equals(otpCode.trim())) {
            User user = dbHelper.getUserByEmailOrPhone(cleanedTarget);
            if (user == null) {
                // Do not allow un-registered users to login via OTP
                return false;
            }

            // Clear pending OTP entries
            pendingOtps.remove(cleanedTarget);
            if (user.getEmail() != null) {
                pendingOtps.remove(user.getEmail().trim().toLowerCase());
            }
            if (user.getPhoneNumber() != null) {
                pendingOtps.remove(user.getPhoneNumber().trim().toLowerCase());
            }

            user.setVerified(true);
            dbHelper.updateUserVerification(user.getId(), true);

            // Sync verified user to Supabase cloud
            supabaseClient.syncUser(user, null);

            this.currentUser = user;
            saveSession(user);
            return true;
        }
        return false;
    }

    public synchronized SignupResult signUp(String fullName, String email, String phoneNumber, String address, String password) {
        String cleanEmail = email != null ? email.trim().toLowerCase() : "";
        String cleanPhone = phoneNumber != null ? phoneNumber.trim() : "";

        if (!cleanEmail.isEmpty() && isEmailRegistered(cleanEmail)) {
            return SignupResult.EMAIL_EXISTS;
        }
        if (!cleanPhone.isEmpty() && isPhoneRegistered(cleanPhone)) {
            return SignupResult.PHONE_EXISTS;
        }

        String id = UUID.randomUUID().toString();
        User user = new User(id, fullName.trim(), cleanEmail, cleanPhone, address != null ? address.trim() : "", password, false, System.currentTimeMillis());

        boolean inserted = dbHelper.insertUser(user);
        if (inserted) {
            this.currentUser = user;
            // Sync user to Supabase cloud asynchronously
            supabaseClient.syncUser(user, null);
            return SignupResult.SUCCESS;
        }
        return SignupResult.FAILED;
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

    public synchronized List<User> getAllRegisteredUsers() {
        return dbHelper.getAllUsers();
    }

    // --- CUSTOMERS ---

    public synchronized boolean addCustomer(Customer customer) {
        if (customer == null) return false;
        boolean result = dbHelper.insertCustomer(customer);
        if (result) {
            supabaseClient.syncCustomer(customer, null);
        }
        return result;
    }

    public synchronized List<Customer> getAllCustomers() {
        return dbHelper.getAllCustomers();
    }

    // --- CHECKLIST ---

    public synchronized boolean addChecklistItem(ChecklistItem item) {
        if (item == null) return false;
        boolean result = dbHelper.insertChecklistItem(item);
        if (result) {
            supabaseClient.syncChecklistItem(item, null);
        }
        return result;
    }

    public synchronized boolean updateChecklistItemCompletion(String id, boolean isCompleted) {
        boolean result = dbHelper.updateChecklistItemCompletion(id, isCompleted);
        if (result) {
            for (ChecklistItem item : dbHelper.getAllChecklistItems()) {
                if (item.getId().equals(id)) {
                    supabaseClient.syncChecklistItem(item, null);
                    break;
                }
            }
        }
        return result;
    }

    public synchronized List<ChecklistItem> getAllChecklistItems() {
        return dbHelper.getAllChecklistItems();
    }

    public SupabaseClient getSupabaseClient() {
        return supabaseClient;
    }

    public void fetchCustomersFromCloud(SupabaseClient.FetchCustomersCallback callback) {
        supabaseClient.fetchCustomers(callback);
    }

    public void fetchUsersFromCloud(SupabaseClient.FetchUsersCallback callback) {
        supabaseClient.fetchUsers(callback);
    }

    private void saveSession(User user) {
        if (user == null) return;
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_ID, user.getId())
                .apply();
    }
}
