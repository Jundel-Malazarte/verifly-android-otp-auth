package com.example.verifly_android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.verifly_android.R;
import com.example.verifly_android.data.model.User;
import com.example.verifly_android.databinding.FragmentDashboardBinding;
import com.example.verifly_android.viewmodel.AuthViewModel;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        observeViewModel();
        setupClickListeners();
    }

    private void observeViewModel() {
        authViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateUserUi(user);
            }
        });
    }

    private void updateUserUi(@NonNull User user) {
        String notProvided = getString(R.string.not_provided);

        String name = user.getFullName() != null && !user.getFullName().trim().isEmpty()
                ? user.getFullName().trim() : "User";
        binding.tvWelcome.setText(name);
        binding.tvFullName.setText(name);

        String email = user.getEmail() != null && !user.getEmail().trim().isEmpty()
                ? user.getEmail().trim() : notProvided;
        binding.tvEmail.setText(email);

        String phone = user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()
                ? user.getPhoneNumber().trim() : notProvided;
        binding.tvPhone.setText(phone);

        if (user.isVerified()) {
            binding.chipVerificationStatus.setText(R.string.verified);
            binding.chipVerificationStatus.setChipIconResource(R.drawable.ic_verified);
        } else {
            binding.chipVerificationStatus.setText(R.string.unverified);
            binding.chipVerificationStatus.setChipIconResource(R.drawable.ic_security);
        }
    }

    private void setupClickListeners() {
        binding.cardAccountSettings.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.account_settings, Toast.LENGTH_SHORT).show()
        );

        binding.cardSecurityOtp.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.security_and_otp, Toast.LENGTH_SHORT).show()
        );

        binding.cardRecentActivity.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.recent_activity, Toast.LENGTH_SHORT).show()
        );

        binding.cardNotifications.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.notifications, Toast.LENGTH_SHORT).show()
        );

        binding.btnLogout.setOnClickListener(this::handleLogout);
    }

    private void handleLogout(View view) {
        authViewModel.logout();
        Navigation.findNavController(view).navigate(R.id.action_dashboardFragment_to_loginFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
