package com.example.campuscompanion;

// Experiment 3: Add / Edit a student record via SQLite

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuscompanion.db.DatabaseHelper;

public class AddEditStudentActivity extends AppCompatActivity {

    private EditText etName, etRoll;
    private Button btnSave;
    private DatabaseHelper dbHelper;
    private int studentId = -1; // -1 means "adding new", otherwise "editing"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_student);

        dbHelper = new DatabaseHelper(this);
        etName = findViewById(R.id.etStudentName);
        etRoll = findViewById(R.id.etStudentRoll);
        btnSave = findViewById(R.id.btnSaveStudent);

        if (getIntent().hasExtra(RecordsFragment.EXTRA_STUDENT_ID)) {
            studentId = getIntent().getIntExtra(RecordsFragment.EXTRA_STUDENT_ID, -1);
            etName.setText(getIntent().getStringExtra(RecordsFragment.EXTRA_STUDENT_NAME));
            etRoll.setText(getIntent().getStringExtra(RecordsFragment.EXTRA_STUDENT_ROLL));
            btnSave.setText("Update");
        }

        btnSave.setOnClickListener(v -> saveStudent());
    }

    private void saveStudent() {
        String name = etName.getText().toString().trim();
        String roll = etRoll.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(roll)) {
            Toast.makeText(this, "Both fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (studentId == -1) {
            dbHelper.addStudent(name, roll);
        } else {
            dbHelper.updateStudent(studentId, name, roll);
        }
        finish();
    }
}
