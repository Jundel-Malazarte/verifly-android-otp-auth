package com.example.verifly_android.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.verifly_android.databinding.FragmentLoginBinding;
import com.example.verifly_android.viewmodel.AuthViewModel;
import com.google.android.material.snackbar.Snackbar;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel authViewModel;
    private String currentTarget = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // Check if already logged in
        if (Boolean.TRUE.equals(authViewModel.getIsLoggedInLiveData().getValue())) {
            navigateToDashboard();
            return;
        }

        setupTextWatchers();
        setupClickListeners();
        observeViewModel();
    }

    private void setupTextWatchers() {
        binding.etIdentifier.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilIdentifier.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etOtp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilOtp.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        // State 1: Send OTP
        binding.btnSendOtp.setOnClickListener(v -> {
            String identifier = binding.etIdentifier.getText() != null ?
                    binding.etIdentifier.getText().toString().trim() : "";

            if (identifier.isEmpty()) {
                binding.tilIdentifier.setError("Email Address or Phone Number is required");
                return;
            }

            currentTarget = identifier;
            binding.tilIdentifier.setError(null);
            authViewModel.requestOtp(identifier);
        });

        // State 2: Verify OTP
        binding.btnVerifyOtp.setOnClickListener(v -> {
            String otpCode = binding.etOtp.getText() != null ?
                    binding.etOtp.getText().toString().trim() : "";

            if (otpCode.isEmpty() || otpCode.length() < 6) {
                binding.tilOtp.setError("Please enter the 6-digit OTP code");
                return;
            }

            binding.tilOtp.setError(null);
            authViewModel.verifyOtp(currentTarget, otpCode);
        });

        // Resend OTP
        binding.btnResendOtp.setOnClickListener(v -> {
            if (!currentTarget.isEmpty()) {
                binding.etOtp.setText("");
                binding.tilOtp.setError(null);
                authViewModel.requestOtp(currentTarget);
            } else {
                showRequestOtpState();
            }
        });

        // Change Target (Email / Phone)
        binding.btnChangeTarget.setOnClickListener(v -> {
            showRequestOtpState();
        });

        // Navigate to Signup
        binding.btnGoToSignup.setOnClickListener(v -> {
            if (getView() != null) {
                NavController navController = Navigation.findNavController(getView());
                if (navController.getCurrentDestination() != null &&
                        navController.getCurrentDestination().getId() == R.id.loginFragment) {
                    navController.navigate(R.id.action_loginFragment_to_signupFragment);
                }
            }
        });
    }

    private void observeViewModel() {
        // Observe Generated OTP
        authViewModel.getOtpSentState().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                String otp = event.getContentIfNotHandled();
                if (otp != null && !otp.isEmpty()) {
                    showVerifyOtpState(currentTarget, otp);
                }
            }
        });

        // Observe OTP Verification Result
        authViewModel.getAuthResultState().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                Boolean success = event.getContentIfNotHandled();
                if (Boolean.TRUE.equals(success)) {
                    Toast.makeText(requireContext(), "Verification Successful!", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                }
            }
        });

        // Observe Auth Success
        authViewModel.getAuthSuccessLiveData().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                Boolean success = event.getContentIfNotHandled();
                if (Boolean.TRUE.equals(success)) {
                    navigateToDashboard();
                }
            }
        });

        // Observe Error Messages
        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                String error = event.getContentIfNotHandled();
                if (error != null && !error.isEmpty()) {
                    if (binding.layoutVerifyOtp.getVisibility() == View.VISIBLE) {
                        binding.tilOtp.setError(error);
                    } else {
                        binding.tilIdentifier.setError(error);
                    }
                    Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void navigateToDashboard() {
        if (getView() != null) {
            NavController navController = Navigation.findNavController(getView());
            if (navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() == R.id.loginFragment) {
                navController.navigate(R.id.action_loginFragment_to_dashboardFragment);
            }
        }
    }

    private void showRequestOtpState() {
        binding.layoutRequestOtp.setVisibility(View.VISIBLE);
        binding.layoutVerifyOtp.setVisibility(View.GONE);
        binding.tilIdentifier.setError(null);
        binding.tilOtp.setError(null);
        binding.etOtp.setText("");
    }

    private void showVerifyOtpState(@NonNull String target, @NonNull String generatedOtp) {
        binding.layoutRequestOtp.setVisibility(View.GONE);
        binding.layoutVerifyOtp.setVisibility(View.VISIBLE);
        binding.tvOtpSentTo.setText("A 6-digit verification code was sent to " + target);
        binding.tilOtp.setError(null);

        // Show test OTP code in Snackbar with Auto-Fill / Copy action
        Snackbar snackbar = Snackbar.make(binding.getRoot(),
                "Generated Test OTP: " + generatedOtp,
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("AUTO-FILL", v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("OTP Code", generatedOtp);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
            binding.etOtp.setText(generatedOtp);
            Toast.makeText(requireContext(), "OTP filled into input field!", Toast.LENGTH_SHORT).show();
        });
        snackbar.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
