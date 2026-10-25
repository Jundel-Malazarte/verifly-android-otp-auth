package com.example.verifly_android.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.verifly_android.data.model.User;
import com.example.verifly_android.data.repository.AuthRepository;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;

    private final MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> generatedOtpLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> otpVerificationResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> authErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> authSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoggedInLiveData = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = AuthRepository.getInstance(application);
        checkSession();
    }

    public LiveData<User> getCurrentUserLiveData() {
        return currentUserLiveData;
    }

    public LiveData<String> getGeneratedOtpLiveData() {
        return generatedOtpLiveData;
    }

    public LiveData<String> getOtpSentState() {
        return generatedOtpLiveData;
    }

    public LiveData<Boolean> getOtpVerificationResultLiveData() {
        return otpVerificationResultLiveData;
    }

    public LiveData<Boolean> getAuthResultState() {
        return otpVerificationResultLiveData;
    }

    public LiveData<String> getAuthErrorLiveData() {
        return authErrorLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return authErrorLiveData;
    }

    public LiveData<Boolean> getAuthSuccessLiveData() {
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
            authErrorLiveData.setValue("Please fill in all required fields.");
            authSuccessLiveData.setValue(false);
            return;
        }

        boolean success = authRepository.signUp(fullName, email, phoneNumber, password);
        if (success) {
            currentUserLiveData.setValue(authRepository.getCurrentUser());
            authSuccessLiveData.setValue(true);
            isLoggedInLiveData.setValue(true);
            authErrorLiveData.setValue(null);
        } else {
            authErrorLiveData.setValue("Account with this email/phone already exists.");
            authSuccessLiveData.setValue(false);
        }
    }

    public void login(String identifier, String password) {
        if (identifier == null || identifier.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            authErrorLiveData.setValue("Please enter identifier and password.");
            authSuccessLiveData.setValue(false);
            return;
        }

        boolean success = authRepository.login(identifier, password);
        if (success) {
            currentUserLiveData.setValue(authRepository.getCurrentUser());
            authSuccessLiveData.setValue(true);
            isLoggedInLiveData.setValue(true);
            authErrorLiveData.setValue(null);
        } else {
            authErrorLiveData.setValue("Invalid credentials.");
            authSuccessLiveData.setValue(false);
        }
    }

    public void requestOtp(String target) {
        if (target == null || target.trim().isEmpty()) {
            authErrorLiveData.setValue("Invalid phone or email for OTP generation.");
            return;
        }
        authErrorLiveData.setValue(null);
        String otp = authRepository.generateOtp(target);
        generatedOtpLiveData.setValue(otp);
    }

    public void verifyOtp(String target, String code) {
        if (target == null || code == null || code.trim().isEmpty()) {
            otpVerificationResultLiveData.setValue(false);
            authSuccessLiveData.setValue(false);
            authErrorLiveData.setValue("Invalid OTP input.");
            return;
        }
        boolean isVerified = authRepository.verifyOtp(target, code);
        otpVerificationResultLiveData.setValue(isVerified);
        if (isVerified) {
            currentUserLiveData.setValue(authRepository.getCurrentUser());
            authSuccessLiveData.setValue(true);
            isLoggedInLiveData.setValue(true);
            authErrorLiveData.setValue(null);
        } else {
            authErrorLiveData.setValue("Incorrect OTP code. Please try again.");
            authSuccessLiveData.setValue(false);
        }
    }

    public void clearError() {
        authErrorLiveData.setValue(null);
    }

    public void clearOtpSentState() {
        generatedOtpLiveData.setValue(null);
    }

    public void logout() {
        authRepository.logout();
        currentUserLiveData.setValue(null);
        isLoggedInLiveData.setValue(false);
        authSuccessLiveData.setValue(false);
        generatedOtpLiveData.setValue(null);
        otpVerificationResultLiveData.setValue(null);
        authErrorLiveData.setValue(null);
    }
}
