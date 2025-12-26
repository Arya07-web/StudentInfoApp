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

    EditText etName, etAge;
    Button btnLogin;
    CheckBox cbRemember;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
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
            startActivity(intent);
            finish();
        }

        btnLogin.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String ageStr = etAge.getText().toString().trim();

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

            int age;
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

            // Save in SharedPreferences if Remember Me checked
            if (cbRemember.isChecked()) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("NAME", name);
                editor.putInt("AGE", age);
                editor.apply();
            }

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("NAME", name);
            intent.putExtra("AGE", age);
            startActivity(intent);
            finish();

            Toast.makeText(LoginActivity.this, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();
        });
    }
}
