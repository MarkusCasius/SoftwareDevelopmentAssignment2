package com.example.softwaredevelopmentassessment2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.firebase.ui.auth.AuthUI;

public class HomeFragment extends Fragment {

    private Button gotoAuthButton;
    private Button signOutButton;
    private Button calculatorNavigateButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        gotoAuthButton = view.findViewById(R.id.GotoAuthButton);
        signOutButton = view.findViewById(R.id.SignOutButton);
        calculatorNavigateButton = view.findViewById(R.id.CalculatorNavigateButton);

        gotoAuthButton.setOnClickListener(v -> {
            Log.d("HomeFragment", "GotoAuthButton clicked → launching VerificationActivity");
            Intent intent = new Intent(getActivity(), VerificationActivity.class);
            startActivity(intent);
            getActivity().finish();  // if you want to finish the hosting activity
        });

        signOutButton.setOnClickListener(v -> {
            Log.d("HomeFragment", "SignOutButton clicked → signing out");
            AuthUI.getInstance().signOut(requireContext())
                    .addOnCompleteListener(task -> Log.d("HomeFragment", "Sign out complete"));
        });

        calculatorNavigateButton.setOnClickListener(v -> {
            Log.d("HomeFragment", "CalculatorNavigateButton clicked → launching CalculatorActivity");
            Intent intent = new Intent(getActivity(), GradeCalculatorActivity.class);
            startActivity(intent);
        });

        return view;
    }
}