package hr.unipu.android.messengerapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import hr.unipu.android.messengerapp.R;
import hr.unipu.android.messengerapp.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.textLogin.setOnClickListener(v -> onBackPressed());
    }
}