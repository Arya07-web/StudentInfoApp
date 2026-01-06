package com.example.studentinfoapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1️⃣ Initialize Firebase with correct region URL
        databaseReference = FirebaseDatabase
                .getInstance("https://studentmanagementapp-cf281-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("students");
        rvStudents = findViewById(R.id.rvStudents);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        studentAdapter = new StudentAdapter(studentList);
        rvStudents.setAdapter(studentAdapter);


        // 2️⃣ Initialize views
        tvWelcome = findViewById(R.id.tvWelcome);
        tvInfo = findViewById(R.id.tvInfo);
        btnLogout = findViewById(R.id.btnLogout);
        btnEdit = findViewById(R.id.btnEdit);
        btnAddStudent = findViewById(R.id.btnAddStudent);
        Button btnDelete = findViewById(R.id.btnDelete);
        preferences = getSharedPreferences("StudentInfo", MODE_PRIVATE);

        // 3️⃣ Get logged-in student data from Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("NAME");
        int age = intent.getIntExtra("AGE", 0);
        loggedRollNo = intent.getStringExtra("ROLL");
        String dept = intent.getStringExtra("DEPT");
        String year = intent.getStringExtra("YEAR");
        int percentage = intent.getIntExtra("PERCENTAGE", 0);
        String grade = intent.getStringExtra("GRADE");

        tvWelcome.setText("Welcome, " + name + "!");

        // Display info immediately
        tvInfo.setText(
                "Name: " + name +
                        "\nAge: " + age +
                        "\nRoll: " + loggedRollNo +
                        "\nDept: " + dept +
                        "\nYear: " + year +
                        "\nPercentage: " + percentage +
                        "\nGrade: " + grade
        );

        // 4️⃣ Add logged-in student to Firebase
        addStudentToFirebase(name, age, loggedRollNo, dept, year, percentage, grade);

        // 5️⃣ Fetch students in real-time
        fetchStudents();

        // 6️⃣ Button listeners
        btnAddStudent.setOnClickListener(v -> showAddEditDialog(null, null));

        btnEdit.setOnClickListener(v -> {
            if (!studentList.isEmpty()) {
                for (int i = 0; i < studentList.size(); i++) {
                    if (studentList.get(i).rollNo.equals(loggedRollNo)) {
                        showAddEditDialog(studentList.get(i), studentIdList.get(i));
                        return;
                    }
                }
                Toast.makeText(this, "Student not found in database", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No student to edit", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(v -> {
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
        });
        btnDelete.setOnClickListener(v -> {
            if (!loggedRollNo.isEmpty()) {
                databaseReference.child(loggedRollNo).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Student deleted!", Toast.LENGTH_SHORT).show();
                            // Clear the info card after deletion
                            tvInfo.setText("");
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
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

        EditText etName = new EditText(this);
        etName.setHint("Name");
        if (student != null) etName.setText(student.name);
        layout.addView(etName);

        EditText etAge = new EditText(this);
        etAge.setHint("Age");
        etAge.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (student != null) etAge.setText(String.valueOf(student.age));
        layout.addView(etAge);

        EditText etRoll = new EditText(this);
        etRoll.setHint("Roll No");
        if (student != null) etRoll.setText(student.rollNo);
        layout.addView(etRoll);

        EditText etDept = new EditText(this);
        etDept.setHint("Department");
        if (student != null) etDept.setText(student.department);
        layout.addView(etDept);

        EditText etYear = new EditText(this);
        etYear.setHint("Year");
        if (student != null) etYear.setText(student.year);
        layout.addView(etYear);

        EditText etPerc = new EditText(this);
        etPerc.setHint("Percentage");
        etPerc.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (student != null) etPerc.setText(String.valueOf(student.percentage));
        layout.addView(etPerc);

        EditText etGrade = new EditText(this);
        etGrade.setHint("Grade");
        if (student != null) etGrade.setText(student.grade);
        layout.addView(etGrade);

        builder.setView(layout);

        builder.setPositiveButton(student == null ? "Add" : "Save", (dialog, which) -> {
            try {
                String name = etName.getText().toString().trim();
                int age = Integer.parseInt(etAge.getText().toString().trim());
                String roll = etRoll.getText().toString().trim();
                String dept = etDept.getText().toString().trim();
                String year = etYear.getText().toString().trim();
                int perc = Integer.parseInt(etPerc.getText().toString().trim());
                String grade = etGrade.getText().toString().trim();

                Student newStudent = new Student(roll, name, age, perc, grade, dept, year);

                if (student == null) {
                    databaseReference.child(roll).setValue(newStudent);
                    Toast.makeText(this, "Student added!", Toast.LENGTH_SHORT).show();
                } else {
                    databaseReference.child(studentId).setValue(newStudent);
                    Toast.makeText(this, "Student updated!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Invalid input!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNeutralButton("Delete", (dialog, which) -> {
            if (student != null && studentId != null) {
                databaseReference.child(studentId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Student deleted!", Toast.LENGTH_SHORT).show();
                            // Optional: remove from local list immediately to avoid delay
                            int index = studentIdList.indexOf(studentId);
                            if (index != -1) {
                                studentList.remove(index);
                                studentIdList.remove(index);
                                studentAdapter.notifyItemRemoved(index);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
