package com.example.verifly_android.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.verifly_android.R;
import com.example.verifly_android.data.model.Customer;
import com.example.verifly_android.data.model.User;
import com.example.verifly_android.data.remote.SupabaseClient;
import com.example.verifly_android.databinding.DialogAddCustomerBinding;
import com.example.verifly_android.databinding.DialogCustomerListBinding;
import com.example.verifly_android.databinding.DialogRegisteredUsersBinding;
import com.example.verifly_android.databinding.DialogSupabaseConfigBinding;
import com.example.verifly_android.databinding.FragmentDashboardBinding;
import com.example.verifly_android.viewmodel.AuthViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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

        setupBackPressedCallback();
        observeViewModel();
        setupClickListeners();
    }

    private void setupBackPressedCallback() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Exit app cleanly on back press from Dashboard instead of navigating back to login
                requireActivity().finish();
            }
        });
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

        binding.cardAddCustomer.setOnClickListener(v -> showAddCustomerDialog());

        binding.cardCustomerList.setOnClickListener(v -> showCustomerListDialog());

        binding.cardRegisteredUsers.setOnClickListener(v -> showRegisteredUsersDialog());

        binding.btnSupabaseConfig.setOnClickListener(v -> showSupabaseConfigDialog());

        binding.btnLogout.setOnClickListener(this::handleLogout);
    }

    private void showAddCustomerDialog() {
        DialogAddCustomerBinding dialogBinding = DialogAddCustomerBinding.inflate(getLayoutInflater());

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Save & Sync", (dialog, which) -> {
                    String name = dialogBinding.etCustName.getText() != null ? dialogBinding.etCustName.getText().toString().trim() : "";
                    String email = dialogBinding.etCustEmail.getText() != null ? dialogBinding.etCustEmail.getText().toString().trim() : "";
                    String phone = dialogBinding.etCustPhone.getText() != null ? dialogBinding.etCustPhone.getText().toString().trim() : "";
                    String address = dialogBinding.etCustAddress.getText() != null ? dialogBinding.etCustAddress.getText().toString().trim() : "";
                    String notes = dialogBinding.etCustNotes.getText() != null ? dialogBinding.etCustNotes.getText().toString().trim() : "";

                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Customer name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean added = authViewModel.addCustomer(name, email, phone, address, notes);
                    if (added) {
                        Toast.makeText(requireContext(), "Customer saved locally & synced to Supabase!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to save customer", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCustomerListDialog() {
        DialogCustomerListBinding dialogBinding = DialogCustomerListBinding.inflate(getLayoutInflater());

        authViewModel.loadCustomers();

        authViewModel.getCustomersLiveData().observe(getViewLifecycleOwner(), customers -> {
            dialogBinding.layoutCustomersList.removeAllViews();
            if (customers != null && !customers.isEmpty()) {
                dialogBinding.tvCustomersCount.setText("Total Customers: " + customers.size() + " (SQLite & Supabase)");
                for (Customer c : customers) {
                    MaterialCardView custCard = new MaterialCardView(requireContext());
                    custCard.setCardElevation(2);
                    custCard.setRadius(24);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, 20);
                    custCard.setLayoutParams(params);

                    LinearLayout cardContent = new LinearLayout(requireContext());
                    cardContent.setOrientation(LinearLayout.VERTICAL);
                    cardContent.setPadding(28, 24, 28, 24);

                    TextView nameTv = new TextView(requireContext());
                    nameTv.setText(c.getName() != null && !c.getName().isEmpty() ? c.getName() : "Unnamed Customer");
                    nameTv.setTextSize(16);
                    nameTv.setTypeface(null, Typeface.BOLD);

                    TextView phoneTv = new TextView(requireContext());
                    phoneTv.setText("Mobile Phone: " + (c.getPhone() != null && !c.getPhone().isEmpty() ? c.getPhone() : "N/A"));
                    phoneTv.setTextSize(13);

                    TextView addressTv = new TextView(requireContext());
                    addressTv.setText("Address: " + (c.getAddress() != null && !c.getAddress().isEmpty() ? c.getAddress() : "N/A"));
                    addressTv.setTextSize(13);

                    TextView emailTv = new TextView(requireContext());
                    if (c.getEmail() != null && !c.getEmail().isEmpty()) {
                        emailTv.setText("Email: " + c.getEmail());
                        emailTv.setTextSize(13);
                    }

                    cardContent.addView(nameTv);
                    cardContent.addView(phoneTv);
                    cardContent.addView(addressTv);
                    if (c.getEmail() != null && !c.getEmail().isEmpty()) {
                        cardContent.addView(emailTv);
                    }
                    custCard.addView(cardContent);

                    dialogBinding.layoutCustomersList.addView(custCard);
                }
            } else {
                dialogBinding.tvCustomersCount.setText("No customers added yet");
            }
        });

        dialogBinding.btnRefreshCustomers.setOnClickListener(v -> {
            authViewModel.loadCustomers();
            Toast.makeText(requireContext(), "Customer list refreshed!", Toast.LENGTH_SHORT).show();
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Close", null)
                .show();
    }

    private void showRegisteredUsersDialog() {
        DialogRegisteredUsersBinding dialogBinding = DialogRegisteredUsersBinding.inflate(getLayoutInflater());

        authViewModel.loadRegisteredUsers();

        authViewModel.getRegisteredUsersLiveData().observe(getViewLifecycleOwner(), users -> {
            dialogBinding.layoutUsersList.removeAllViews();
            if (users != null && !users.isEmpty()) {
                dialogBinding.tvUsersCount.setText("Total Registered Users: " + users.size() + " (SQLite & Supabase)");
                for (User u : users) {
                    MaterialCardView userCard = new MaterialCardView(requireContext());
                    userCard.setCardElevation(2);
                    userCard.setRadius(24);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, 20);
                    userCard.setLayoutParams(params);

                    LinearLayout cardContent = new LinearLayout(requireContext());
                    cardContent.setOrientation(LinearLayout.VERTICAL);
                    cardContent.setPadding(28, 24, 28, 24);

                    TextView nameTv = new TextView(requireContext());
                    nameTv.setText(u.getFullName() != null && !u.getFullName().isEmpty() ? u.getFullName() : "User");
                    nameTv.setTextSize(16);
                    nameTv.setTypeface(null, Typeface.BOLD);

                    TextView detailsTv = new TextView(requireContext());
                    String statusStr = u.isVerified() ? "VERIFIED" : "UNVERIFIED";
                    String addrStr = u.getAddress() != null && !u.getAddress().isEmpty() ? u.getAddress() : "N/A";
                    detailsTv.setText("Email: " + (u.getEmail() != null && !u.getEmail().isEmpty() ? u.getEmail() : "N/A") +
                            "\nPhone: " + (u.getPhoneNumber() != null && !u.getPhoneNumber().isEmpty() ? u.getPhoneNumber() : "N/A") +
                            "\nAddress: " + addrStr +
                            "\nStatus: " + statusStr);
                    detailsTv.setTextSize(13);

                    cardContent.addView(nameTv);
                    cardContent.addView(detailsTv);
                    userCard.addView(cardContent);

                    dialogBinding.layoutUsersList.addView(userCard);
                }
            } else {
                dialogBinding.tvUsersCount.setText("No registered users found");
            }
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Close", null)
                .show();
    }

    private void showSupabaseConfigDialog() {
        DialogSupabaseConfigBinding dialogBinding = DialogSupabaseConfigBinding.inflate(getLayoutInflater());
        SupabaseClient client = authViewModel.getSupabaseClient();

        dialogBinding.etSupabaseUrl.setText(client.getSupabaseUrl());
        dialogBinding.etSupabaseKey.setText(client.getSupabaseKey());

        dialogBinding.btnCopySqlSchema.setOnClickListener(v -> {
            String sqlSchema = "-- SUPABASE SQL SCHEMA FOR VERIFLY-ANDROID --\n\n" +
                    "CREATE TABLE IF NOT EXISTS public.users (\n" +
                    "  id TEXT PRIMARY KEY,\n" +
                    "  full_name TEXT NOT NULL,\n" +
                    "  email TEXT UNIQUE,\n" +
                    "  phone_number TEXT,\n" +
                    "  address TEXT,\n" +
                    "  is_verified BOOLEAN DEFAULT false,\n" +
                    "  created_at BIGINT\n" +
                    ");\n\n" +
                    "CREATE TABLE IF NOT EXISTS public.customers (\n" +
                    "  id TEXT PRIMARY KEY,\n" +
                    "  name TEXT NOT NULL,\n" +
                    "  email TEXT,\n" +
                    "  phone TEXT,\n" +
                    "  address TEXT,\n" +
                    "  status TEXT,\n" +
                    "  notes TEXT,\n" +
                    "  created_at BIGINT\n" +
                    ");\n\n" +
                    "CREATE TABLE IF NOT EXISTS public.customer_checklists (\n" +
                    "  id TEXT PRIMARY KEY,\n" +
                    "  customer_id TEXT,\n" +
                    "  title TEXT NOT NULL,\n" +
                    "  category TEXT,\n" +
                    "  is_completed BOOLEAN DEFAULT false,\n" +
                    "  created_at BIGINT\n" +
                    ");\n";

            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Supabase SQL Schema", sqlSchema);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
            Toast.makeText(requireContext(), "Supabase SQL Schema copied to clipboard!", Toast.LENGTH_LONG).show();
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Save Credentials", (dialog, which) -> {
                    String url = dialogBinding.etSupabaseUrl.getText() != null ? dialogBinding.etSupabaseUrl.getText().toString().trim() : "";
                    String key = dialogBinding.etSupabaseKey.getText() != null ? dialogBinding.etSupabaseKey.getText().toString().trim() : "";

                    authViewModel.saveSupabaseConfig(url, key);
                    Toast.makeText(requireContext(), "Supabase Cloud configuration saved!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleLogout(View view) {
        authViewModel.logout();
        if (getView() != null) {
            NavController navController = Navigation.findNavController(getView());
            if (navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() == R.id.dashboardFragment) {
                navController.navigate(R.id.action_dashboardFragment_to_loginFragment);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
