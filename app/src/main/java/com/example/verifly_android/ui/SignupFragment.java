package com.example.verifly_android.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.verifly_android.R;
import com.example.verifly_android.databinding.FragmentSignupBinding;
import com.example.verifly_android.viewmodel.AuthViewModel;
import com.google.android.material.snackbar.Snackbar;

public class SignupFragment extends Fragment {

    private FragmentSignupBinding binding;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupTextWatchers();
        setupClickListeners();
        observeViewModel();
    }

    private void setupTextWatchers() {
        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilFullName.setError(null);
                binding.tilEmail.setError(null);
                binding.tilPhone.setError(null);
                binding.tilPassword.setError(null);
                binding.tilConfirmPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etFullName.addTextChangedListener(clearErrorWatcher);
        binding.etEmail.addTextChangedListener(clearErrorWatcher);
        binding.etPhone.addTextChangedListener(clearErrorWatcher);
        binding.etPassword.addTextChangedListener(clearErrorWatcher);
        binding.etConfirmPassword.addTextChangedListener(clearErrorWatcher);
    }

    private void setupClickListeners() {
        binding.btnSignup.setOnClickListener(v -> attemptSignup());

        binding.btnGoToLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void attemptSignup() {
        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

        boolean isValid = true;

        if (fullName.isEmpty()) {
            binding.tilFullName.setError("Full Name is required");
            isValid = false;
        }

        if (email.isEmpty()) {
            binding.tilEmail.setError("Email Address is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Please enter a valid email address");
            isValid = false;
        }

        if (phone.isEmpty()) {
            binding.tilPhone.setError("Phone Number is required");
            isValid = false;
        } else if (phone.length() < 7) {
            binding.tilPhone.setError("Please enter a valid phone number");
            isValid = false;
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.setError("Confirm Password is required");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        if (isValid) {
            authViewModel.signUp(fullName, email, phone, password);
        }
    }

    private void observeViewModel() {
        authViewModel.getAuthSuccessLiveData().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                Boolean success = event.getContentIfNotHandled();
                if (Boolean.TRUE.equals(success)) {
                    Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show();

                    String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";

                    // Request OTP for the newly created user
                    if (!email.isEmpty()) {
                        authViewModel.requestOtp(email);
                    }

                    navigateToLogin();
                }
            }
        });

        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                String error = event.getContentIfNotHandled();
                if (error != null && !error.isEmpty()) {
                    if (error.contains("email") || error.contains("exists")) {
                        binding.tilEmail.setError(error);
                    }
                    Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void navigateToLogin() {
        if (getView() != null) {
            NavController navController = Navigation.findNavController(getView());
            if (navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() == R.id.signupFragment) {
                navController.navigate(R.id.action_signupFragment_to_loginFragment);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
