package com.example.verifly_android.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.verifly_android.data.model.User;
import com.example.verifly_android.data.repository.AuthRepository;
import com.example.verifly_android.util.Event;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;

    private final MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> generatedOtpLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> otpVerificationResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> authErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> authSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoggedInLiveData = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = AuthRepository.getInstance(application);
        checkSession();
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

    public void checkSession() {
        boolean loggedIn = authRepository.isLoggedIn();
        isLoggedInLiveData.setValue(loggedIn);
        if (loggedIn) {
            currentUserLiveData.setValue(authRepository.getCurrentUser());
        } else {
            currentUserLiveData.setValue(null);
        }
    }

    public void signUp(String fullName, String email, String phoneNumber, String password) {
        if (fullName == null || fullName.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            authErrorLiveData.setValue(new Event<>("Please fill in all required fields."));
            authSuccessLiveData.setValue(new Event<>(false));
            return;
        }

        boolean success = authRepository.signUp(fullName, email, phoneNumber, password);
        if (success) {
            currentUserLiveData.setValue(authRepository.getCurrentUser());
            authSuccessLiveData.setValue(new Event<>(true));
            // Do NOT set isLoggedIn to true yet so user can verify via OTP
            isLoggedInLiveData.setValue(false);
        } else {
            authErrorLiveData.setValue(new Event<>("An account with this email or phone number already exists."));
            authSuccessLiveData.setValue(new Event<>(false));
        }
    }

    public void login(String identifier, String password) {
        if (identifier == null || identifier.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            authErrorLiveData.setValue(new Event<>("Please enter identifier and password."));
            authSuccessLiveData.setValue(new Event<>(false));
            return;
        }

        if (!authRepository.isUserRegistered(identifier.trim())) {
            authErrorLiveData.setValue(new Event<>("This account is not registered. Please sign up first."));
            authSuccessLiveData.setValue(new Event<>(false));
            return;
        }

        boolean success = authRepository.login(identifier, password);
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
        if (target == null || target.trim().isEmpty() || code == null || code.trim().isEmpty()) {
            otpVerificationResultLiveData.setValue(new Event<>(false));
            authErrorLiveData.setValue(new Event<>("Invalid OTP input."));
            return;
        }

        String cleaned = target.trim();
        if (!authRepository.isUserRegistered(cleaned)) {
            otpVerificationResultLiveData.setValue(new Event<>(false));
            authErrorLiveData.setValue(new Event<>("This email or phone number is not registered. Please sign up first."));
            return;
        }

        boolean isVerified = authRepository.verifyOtp(cleaned, code);
        otpVerificationResultLiveData.setValue(new Event<>(isVerified));

        if (isVerified) {
            currentUserLiveData.setValue(authRepository.getCurrentUser());
            isLoggedInLiveData.setValue(true);
        } else {
            authErrorLiveData.setValue(new Event<>("Incorrect OTP code. Please try again."));
        }
    }

    public void logout() {
        authRepository.logout();
        currentUserLiveData.setValue(null);
        isLoggedInLiveData.setValue(false);
    }
}
