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

// Began development on using fragments for the app, however it didn't get far as issues with layout kept hindering progress and was disregarded
// for later development.
public class HomeFragment extends Fragment {

    private Button signOutButton;
    private Button calculatorNavigateButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        signOutButton = view.findViewById(R.id.SignOutButton);
        calculatorNavigateButton = view.findViewById(R.id.CalculatorNavigateButton);

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