package com.example.studentinfoapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etName, etAge, etRoll, etDept, etYear, etPercentage, etGrade;
    Button btnLogin;
    CheckBox cbRemember;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize all EditTexts
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etRoll = findViewById(R.id.etRoll);
        etDept = findViewById(R.id.etDept);
        etYear = findViewById(R.id.etYear);
        etPercentage = findViewById(R.id.etPercentage);
        etGrade = findViewById(R.id.etGrade);

        btnLogin = findViewById(R.id.btnLogin);
        cbRemember = findViewById(R.id.cbRemember);

        preferences = getSharedPreferences("StudentInfo", MODE_PRIVATE);

        // Auto-login if Remember Me was checked
        String savedName = preferences.getString("NAME", null);
        int savedAge = preferences.getInt("AGE", 0);
        if (savedName != null && savedAge != 0) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("NAME", savedName);
            intent.putExtra("AGE", savedAge);
            // Add saved extras if needed
            intent.putExtra("ROLL", preferences.getString("ROLL", ""));
            intent.putExtra("DEPT", preferences.getString("DEPT", ""));
            intent.putExtra("YEAR", preferences.getString("YEAR", ""));
            intent.putExtra("PERCENTAGE", preferences.getInt("PERCENTAGE", 0));
            intent.putExtra("GRADE", preferences.getString("GRADE", ""));
            startActivity(intent);
            finish();
        }

        btnLogin.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String ageStr = etAge.getText().toString().trim();
            String roll = etRoll.getText().toString().trim();
            String dept = etDept.getText().toString().trim();
            String year = etYear.getText().toString().trim();
            String percStr = etPercentage.getText().toString().trim();
            String grade = etGrade.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Name is required");
                etName.requestFocus();
                return;
            }
            if (ageStr.isEmpty()) {
                etAge.setError("Age is required");
                etAge.requestFocus();
                return;
            }
            if (roll.isEmpty()) {
                etRoll.setError("Roll Number is required");
                etRoll.requestFocus();
                return;
            }
            if (dept.isEmpty()) {
                etDept.setError("Department is required");
                etDept.requestFocus();
                return;
            }
            if (year.isEmpty()) {
                etYear.setError("Year is required");
                etYear.requestFocus();
                return;
            }
            if (percStr.isEmpty()) {
                etPercentage.setError("Percentage is required");
                etPercentage.requestFocus();
                return;
            }
            if (grade.isEmpty()) {
                etGrade.setError("Grade is required");
                etGrade.requestFocus();
                return;
            }

            int age, percentage;
            try {
                age = Integer.parseInt(ageStr);
                if (age <= 0) {
                    etAge.setError("Enter valid age");
                    etAge.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etAge.setError("Age must be a number");
                etAge.requestFocus();
                return;
            }

            try {
                percentage = Integer.parseInt(percStr);
                if (percentage < 0 || percentage > 100) {
                    etPercentage.setError("Enter valid percentage");
                    etPercentage.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etPercentage.setError("Percentage must be a number");
                etPercentage.requestFocus();
                return;
            }

            // Save in SharedPreferences if Remember Me checked
            if (cbRemember.isChecked()) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("NAME", name);
                editor.putInt("AGE", age);
                editor.putString("ROLL", roll);
                editor.putString("DEPT", dept);
                editor.putString("YEAR", year);
                editor.putInt("PERCENTAGE", percentage);
                editor.putString("GRADE", grade);
                editor.apply();
            }

            // Pass all data to HomeActivity
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("NAME", name);
            intent.putExtra("AGE", age);
            intent.putExtra("ROLL", roll);
            intent.putExtra("DEPT", dept);
            intent.putExtra("YEAR", year);
            intent.putExtra("PERCENTAGE", percentage);
            intent.putExtra("GRADE", grade);

            startActivity(intent);
            finish();

            Toast.makeText(LoginActivity.this, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();
        });
    }
}
