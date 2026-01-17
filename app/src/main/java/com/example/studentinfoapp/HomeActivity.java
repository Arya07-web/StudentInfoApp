package com.example.studentinfoapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;




public class HomeActivity extends AppCompatActivity {

    TextView tvWelcome, tvInfo;
    Button btnLogout, btnEdit, btnAddStudent;
    SharedPreferences preferences;
    DatabaseReference databaseReference;
    ArrayList<Student> studentList = new ArrayList<>();
    ArrayList<String> studentIdList = new ArrayList<>();

    String loggedRollNo;
    RecyclerView rvStudents;
    StudentAdapter studentAdapter;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    String selectedStudentId = null; // Firebase key of selected student
    int selectedStudentIndex = -1;   // Optional: index in the list for RecyclerView updates


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();
        tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Welcome, " + email);


        // 1️⃣ Initialize Firebase with correct region URL
        databaseReference = FirebaseDatabase
                .getInstance("https://studentmanagementapp-cf281-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("students").child(uid);
        rvStudents = findViewById(R.id.rvStudents);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        studentAdapter = new StudentAdapter(studentList);
        rvStudents.setAdapter(studentAdapter);
        studentAdapter.setOnStudentClickListener((student, position) -> {
            selectedStudentId = studentIdList.get(position); // Firebase key
            selectedStudentIndex = position;
            tvInfo.setText("Selected: " + student.name + " (" + student.rollNo + ")");
        });


        // 2️⃣ Initialize views
        tvWelcome = findViewById(R.id.tvWelcome);
        tvInfo = findViewById(R.id.tvInfo);
        btnLogout = findViewById(R.id.btnLogout);
        btnEdit = findViewById(R.id.btnEdit);
        btnAddStudent = findViewById(R.id.btnAddStudent);
        Button btnDelete = findViewById(R.id.btnDelete);
        preferences = getSharedPreferences("StudentInfo", MODE_PRIVATE);



        // 5️⃣ Fetch students in real-time
        fetchStudents();

        // 6️⃣ Button listeners
        btnAddStudent.setOnClickListener(v -> {
            showAddEditDialog(null, null);
        });



        btnEdit.setOnClickListener(v -> {
            if (selectedStudentId != null && selectedStudentIndex != -1) {
                Student studentToEdit = studentList.get(selectedStudentIndex);
                showAddEditDialog(studentToEdit, selectedStudentId);
            } else {
                Toast.makeText(this, "Please select a student to edit", Toast.LENGTH_SHORT).show();
            }
        });


       /* btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        preferences.edit().clear().apply();
                        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });*/
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            if (selectedStudentId != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Student")
                        .setMessage("Are you sure you want to delete this student?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            databaseReference.child(selectedStudentId).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Student deleted!", Toast.LENGTH_SHORT).show();
                                        // Update RecyclerView immediately
                                        if (selectedStudentIndex != -1) {
                                            studentList.remove(selectedStudentIndex);
                                            studentIdList.remove(selectedStudentIndex);
                                            studentAdapter.notifyItemRemoved(selectedStudentIndex);
                                        }
                                        // Clear selection
                                        selectedStudentId = null;
                                        selectedStudentIndex = -1;
                                        tvInfo.setText("");
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                        })
                        .setNegativeButton("No", null)
                        .show();
            } else {
                Toast.makeText(this, "Please select a student to delete", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Add student to Firebase
    private void addStudentToFirebase(String name, int age, String rollNo, String dept, String year, int percentage, String grade) {
        Student student = new Student(rollNo, name, age, percentage, grade, dept, year);
        databaseReference.child(rollNo).setValue(student)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Student added to database!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Fetch students safely
    private void fetchStudents() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                studentIdList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        Student s = ds.getValue(Student.class);
                        if (s != null) {
                            studentList.add(s);
                            studentIdList.add(ds.getKey());
                        }
                    } catch (Exception e) {
                        Log.e("FirebaseError", "Skipping non-Student node: " + ds.getKey());
                    }
                }
                studentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Add/Edit/Delete student dialog
    private void showAddEditDialog(Student student, String studentId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(student == null ? "Add Student" : "Edit Student");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // --- Name ---
        EditText etName = new EditText(this);
        etName.setHint("Name");
        if (student != null) etName.setText(student.name);
        layout.addView(etName);

        // --- Age ---
        EditText etAge = new EditText(this);
        etAge.setHint("Age");
        etAge.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (student != null) etAge.setText(String.valueOf(student.age));
        layout.addView(etAge);

        // --- Roll No ---
        EditText etRoll = new EditText(this);
        etRoll.setHint("Roll No");
        if (student != null) etRoll.setText(student.rollNo);
        layout.addView(etRoll);

        // --- Department ---
        EditText etDept = new EditText(this);
        etDept.setHint("Department");
        if (student != null) etDept.setText(student.department);
        layout.addView(etDept);

        // --- Year ---
        EditText etYear = new EditText(this);
        etYear.setHint("Year");
        if (student != null) etYear.setText(student.year);
        layout.addView(etYear);

        // --- Percentage ---
        EditText etPerc = new EditText(this);
        etPerc.setHint("Percentage");
        etPerc.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (student != null) etPerc.setText(String.valueOf(student.percentage));
        layout.addView(etPerc);

        // --- Grade ---
        EditText etGrade = new EditText(this);
        etGrade.setHint("Grade");
        if (student != null) etGrade.setText(student.grade);
        layout.addView(etGrade);

        builder.setView(layout);

        // --- Positive Button ---
        builder.setPositiveButton(student == null ? "Add" : "Save", (dialog, which) -> {
            try {
                // --- Get values ---
                String name = etName.getText().toString().trim();
                String roll = etRoll.getText().toString().trim();
                String dept = etDept.getText().toString().trim();
                String year = etYear.getText().toString().trim();
                String grade = etGrade.getText().toString().trim();
                int age = Integer.parseInt(etAge.getText().toString().trim());
                int perc = Integer.parseInt(etPerc.getText().toString().trim());

                // --- Validate ---
                if (name.isEmpty() || roll.isEmpty() || dept.isEmpty() || year.isEmpty() || grade.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                Student newStudent = new Student(roll, name, age, perc, grade, dept, year);

                if (student == null) { // Add new
                    databaseReference.child(roll).setValue(newStudent)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Student added!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else { // Edit existing
                    databaseReference.child(studentId).setValue(newStudent)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Student updated!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number input!", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Delete button (only for edit) ---
        if (student != null) {
            builder.setNeutralButton("Delete", (dialog, which) -> {
                databaseReference.child(studentId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Student deleted!", Toast.LENGTH_SHORT).show();
                            int index = studentIdList.indexOf(studentId);
                            if (index != -1) {
                                studentList.remove(index);
                                studentIdList.remove(index);
                                studentAdapter.notifyItemRemoved(index);
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });
        }

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

}
