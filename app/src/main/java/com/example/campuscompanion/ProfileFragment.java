package com.example.campuscompanion;

// Experiment 7: camera capture -> internal storage
// Experiment 9: start/stop the background music Service
// Also: logout (Firebase Auth)

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProfileFragment extends Fragment {

    private TextView tvUserEmail;
    private Button btnPlayMusic, btnStopMusic, btnOpenCamera, btnLogout;
    private ImageView ivProfilePhoto;

    private Uri photoUri;
    private File photoFile;

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> requestCameraPermission;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        btnPlayMusic = view.findViewById(R.id.btnPlayMusic);
        btnStopMusic = view.findViewById(R.id.btnStopMusic);
        btnOpenCamera = view.findViewById(R.id.btnOpenCamera);
        btnLogout = view.findViewById(R.id.btnLogout);
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            tvUserEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        }

        // --- Exp 9: background music controls ---
        btnPlayMusic.setOnClickListener(v ->
                ContextCompat.startForegroundService(requireContext(),
                        new Intent(requireContext(), MusicService.class)));

        btnStopMusic.setOnClickListener(v ->
                requireContext().stopService(new Intent(requireContext(), MusicService.class)));

        // --- Exp 7: camera capture setup ---
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(), success -> {
                    if (success) {
                        displaySavedPhoto();
                    }
                });

        requestCameraPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> {
                    if (granted) {
                        launchCamera();
                    } else {
                        Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show();
                    }
                });

        btnOpenCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                requestCameraPermission.launch(android.Manifest.permission.CAMERA);
            }
        });

        // --- Logout ---
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            requireActivity().finish();
            startActivity(new Intent(requireContext(), LoginActivity.class));
        });
    }

    private void launchCamera() {
        try {
            // Save into the app's internal storage (files/images/), not shared external storage
            File imagesDir = new File(requireContext().getFilesDir(), "images");
            if (!imagesDir.exists()) imagesDir.mkdirs();
            photoFile = new File(imagesDir, "profile_" + System.currentTimeMillis() + ".jpg");

            photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.campuscompanion.fileprovider",
                    photoFile);

            takePictureLauncher.launch(photoUri);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Could not open camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void displaySavedPhoto() {
        if (photoFile != null && photoFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            ivProfilePhoto.setImageBitmap(bitmap);
            Toast.makeText(requireContext(), "Saved to internal storage: " + photoFile.getName(), Toast.LENGTH_SHORT).show();
        }
    }
}
