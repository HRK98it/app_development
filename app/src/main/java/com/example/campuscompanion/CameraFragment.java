package com.example.campuscompanion;

// Experiment 7: capture an image and store it in internal storage
// (Kept as a standalone fragment; ProfileFragment below launches it inline instead
//  so this class is here in case you want it as its own tab.)

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CameraFragment extends Fragment {

    private ImageView ivCaptured;
    private Button btnCapture;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivCaptured = view.findViewById(R.id.ivCaptured);
        btnCapture = view.findViewById(R.id.btnCapture);
        // See ProfileFragment.java for the full working capture-to-internal-storage flow,
        // which uses the same FileProvider + ACTION_IMAGE_CAPTURE pattern.
    }
}
