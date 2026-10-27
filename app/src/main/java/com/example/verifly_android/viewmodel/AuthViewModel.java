package com.example.verifly_android.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.verifly_android.data.model.ChecklistItem;
import com.example.verifly_android.data.model.Customer;
import com.example.verifly_android.data.model.User;
import com.example.verifly_android.data.remote.SupabaseClient;
import com.example.verifly_android.data.repository.AuthRepository;
import com.example.verifly_android.data.repository.AuthRepository.SignupResult;
import com.example.verifly_android.util.Event;

import java.util.List;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;

    private String pendingOtpTarget = "";

    private final MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> generatedOtpLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> otpVerificationResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> authErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> authSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoggedInLiveData = new MutableLiveData<>();

    private final MutableLiveData<List<Customer>> customersLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ChecklistItem>> checklistLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<User>> registeredUsersLiveData = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = AuthRepository.getInstance(application);
        checkSession();
    }

    public String getPendingOtpTarget() {
        return pendingOtpTarget;
    }

    public void setPendingOtpTarget(String target) {
        if (target != null) {
            this.pendingOtpTarget = target.trim();
        }
    }

    public LiveData<User> getCurrentUserLiveData() {
        return currentUserLiveData;
    }

    public LiveData<Event<String>> getOtpSentState() {
        return generatedOtpLiveData;
    }

    public LiveData<Event<Boolean>> getAuthResultState() {
        return otpVerificationResultLiveData;
    }

    public LiveData<Event<String>> getErrorMessage() {
        return authErrorLiveData;
    }

    public LiveData<Event<Boolean>> getAuthSuccessLiveData() {
        return authSuccessLiveData;
    }

    public LiveData<Boolean> getIsLoggedInLiveData() {
        return isLoggedInLiveData;
    }

    public LiveData<List<Customer>> getCustomersLiveData() {
        return customersLiveData;
    }

    public LiveData<List<ChecklistItem>> getChecklistLiveData() {
        return checklistLiveData;
    }

    public LiveData<List<User>> getRegisteredUsersLiveData() {
        return registeredUsersLiveData;
    }

    public void checkSession() {
        boolean loggedIn = authRepository.isLoggedIn();
        isLoggedInLiveData.setValue(loggedIn);
        if (loggedIn) {
            currentUserLiveData.setValue(authRepository.getCurrentUser());
        } else {
            currentUserLiveData.setValue(null);
        }
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSymbol = true;
            }
        }
        return hasUpper && hasLower && hasDigit && hasSymbol;
    }

    public void signUp(String fullName, String email, String phoneNumber, String address, String password) {
        if (fullName == null || fullName.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                phoneNumber == null || phoneNumber.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            authErrorLiveData.setValue(new Event<>("Please fill in all required fields."));
            authSuccessLiveData.setValue(new Event<>(false));
            return;
        }

        if (!isValidPassword(password)) {
            authErrorLiveData.setValue(new Event<>("PASSWORD_INVALID: Password must be at least 8 characters long and contain at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special symbol (e.g. Pass@1234)."));
            authSuccessLiveData.setValue(new Event<>(false));
            return;
        }

        SignupResult result = authRepository.signUp(fullName, email, phoneNumber, address, password);
        switch (result) {
            case SUCCESS:
                currentUserLiveData.setValue(authRepository.getCurrentUser());
                authSuccessLiveData.setValue(new Event<>(true));
                isLoggedInLiveData.setValue(false);
                break;
            case EMAIL_EXISTS:
                authErrorLiveData.setValue(new Event<>("EMAIL_EXISTS: This email address is already registered. Please log in or use another email."));
                authSuccessLiveData.setValue(new Event<>(false));
                break;
            case PHONE_EXISTS:
                authErrorLiveData.setValue(new Event<>("PHONE_EXISTS: This phone number is already registered. Please log in or use another phone number."));
                authSuccessLiveData.setValue(new Event<>(false));
                break;
            case FAILED:
            default:
                authErrorLiveData.setValue(new Event<>("Sign up failed. Please try again."));
                authSuccessLiveData.setValue(new Event<>(false));
                break;
        }
    }

    public void login(String identifier, String password) {
        if (identifier == null || identifier.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            authErrorLiveData.setValue(new Event<>("Please enter identifier and password."));
            authSuccessLiveData.setValue(new Event<>(false));
            return;
        }

        String cleaned = identifier.trim();
        if (!authRepository.isUserRegistered(cleaned)) {
            authErrorLiveData.setValue(new Event<>("This account is not registered. Please sign up first."));
            authSuccessLiveData.setValue(new Event<>(false));
            return;
        }

        boolean success = authRepository.login(cleaned, password);
        if (success) {
            currentUserLiveData.setValue(authRepository.getCurrentUser());
            authSuccessLiveData.setValue(new Event<>(true));
            isLoggedInLiveData.setValue(true);
        } else {
            authErrorLiveData.setValue(new Event<>("Invalid credentials. Please check your password."));
            authSuccessLiveData.setValue(new Event<>(false));
        }
    }

    public void requestOtp(String target) {
        if (target == null || target.trim().isEmpty()) {
            authErrorLiveData.setValue(new Event<>("Please enter an email address or phone number."));
            return;
        }

        String cleaned = target.trim();
        this.pendingOtpTarget = cleaned;

        if (!authRepository.isUserRegistered(cleaned)) {
            authErrorLiveData.setValue(new Event<>("This email or phone number is not registered. Please sign up first."));
            return;
        }

        String otp = authRepository.generateOtp(cleaned);
        if (otp != null) {
            generatedOtpLiveData.setValue(new Event<>(otp));
        } else {
            authErrorLiveData.setValue(new Event<>("This email or phone number is not registered. Please sign up first."));
        }
    }

    public void verifyOtp(String target, String code) {
        String effectiveTarget = (target != null && !target.trim().isEmpty()) ? target.trim() : pendingOtpTarget;

        if (effectiveTarget.isEmpty() || code == null || code.trim().isEmpty()) {
            otpVerificationResultLiveData.setValue(new Event<>(false));
            authErrorLiveData.setValue(new Event<>("Invalid OTP input."));
            return;
        }

        if (!authRepository.isUserRegistered(effectiveTarget)) {
            otpVerificationResultLiveData.setValue(new Event<>(false));
            authErrorLiveData.setValue(new Event<>("This email or phone number is not registered. Please sign up first."));
            return;
        }

        boolean isVerified = authRepository.verifyOtp(effectiveTarget, code);
        otpVerificationResultLiveData.setValue(new Event<>(isVerified));

        if (isVerified) {
            currentUserLiveData.setValue(authRepository.getCurrentUser());
            isLoggedInLiveData.setValue(true);
        } else {
            authErrorLiveData.setValue(new Event<>("Incorrect OTP code. Please try again."));
        }
    }

    // --- CUSTOMERS, CHECKLIST & REGISTERED USERS ---

    public void loadCustomers() {
        customersLiveData.setValue(authRepository.getAllCustomers());
    }

    public boolean addCustomer(String name, String email, String phone, String address, String notes) {
        if (name == null || name.trim().isEmpty()) {
            authErrorLiveData.setValue(new Event<>("Customer name is required"));
            return false;
        }
        Customer customer = new Customer(name.trim(), email != null ? email.trim() : "", phone != null ? phone.trim() : "", address != null ? address.trim() : "", notes != null ? notes.trim() : "");
        boolean success = authRepository.addCustomer(customer);
        if (success) {
            loadCustomers();
        }
        return success;
    }

    public void loadChecklist() {
        checklistLiveData.setValue(authRepository.getAllChecklistItems());
    }

    public boolean addChecklistItem(String title, String category) {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        ChecklistItem item = new ChecklistItem(title.trim(), category != null ? category.trim() : "General");
        boolean success = authRepository.addChecklistItem(item);
        if (success) {
            loadChecklist();
        }
        return success;
    }

    public void toggleChecklistItem(String id, boolean isCompleted) {
        authRepository.updateChecklistItemCompletion(id, isCompleted);
        loadChecklist();
    }

    public void loadRegisteredUsers() {
        registeredUsersLiveData.setValue(authRepository.getAllRegisteredUsers());
    }

    public SupabaseClient getSupabaseClient() {
        return authRepository.getSupabaseClient();
    }

    public void saveSupabaseConfig(String url, String key) {
        authRepository.getSupabaseClient().saveConfig(url, key);
    }

    public void logout() {
        authRepository.logout();
        pendingOtpTarget = "";
        currentUserLiveData.setValue(null);
        isLoggedInLiveData.setValue(false);
    }
}
