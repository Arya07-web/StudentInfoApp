package com.example.studentinfoapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    TextView tvWelcome, tvInfo;
    Button btnLogout, btnEdit;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome);
        tvInfo = findViewById(R.id.tvInfo);
        btnLogout = findViewById(R.id.btnLogout);
        btnEdit = findViewById(R.id.btnEdit);

        preferences = getSharedPreferences("StudentInfo", MODE_PRIVATE);

        // Get data from Intent or SharedPreferences
        Intent intent = getIntent();
        String name = intent.getStringExtra("NAME");
        int age = intent.getIntExtra("AGE", 0);

        // Display user info
        tvWelcome.setText("Welcome, " + name + "!");
        tvInfo.setText("Name: " + name + "\nAge: " + age);

        // Edit info button
        btnEdit.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Edit Info");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText inputName = new EditText(this);
            inputName.setHint("Name");
            inputName.setText(name);
            layout.addView(inputName);

            final EditText inputAge = new EditText(this);
            inputAge.setHint("Age");
            inputAge.setInputType(InputType.TYPE_CLASS_NUMBER);
            inputAge.setText(String.valueOf(age));
            layout.addView(inputAge);

            builder.setView(layout);

            builder.setPositiveButton("Save", (dialog, which) -> {
                String newName = inputName.getText().toString().trim();
                int newAge = Integer.parseInt(inputAge.getText().toString().trim());

                tvWelcome.setText("Welcome, " + newName + "!");
                tvInfo.setText("Name: " + newName + "\nAge: " + newAge);

                // Save updated info
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("NAME", newName);
                editor.putInt("AGE", newAge);
                editor.apply();
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        });

        // Logout button with confirmation
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        preferences.edit().clear().apply(); // Clear saved info
                        Intent logoutIntent = new Intent(HomeActivity.this, LoginActivity.class);
                        startActivity(logoutIntent);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

}
