package hr.unipu.android.messengerapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import hr.unipu.android.messengerapp.R;
import hr.unipu.android.messengerapp.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners (){
        binding.textRegistration.setOnClickListener(view ->
                startActivity((new Intent(getApplicationContext(), SignUpActivity.class))));
    }
}