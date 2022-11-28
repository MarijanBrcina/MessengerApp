package hr.unipu.android.messengerapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import hr.unipu.android.messengerapp.R;
import hr.unipu.android.messengerapp.User;
import hr.unipu.android.messengerapp.databinding.ActivityMessagesBinding;
import hr.unipu.android.messengerapp.databinding.ActivityUsersBinding;
import hr.unipu.android.messengerapp.utilities.Constants;

public class MessagesActivity extends AppCompatActivity {

    private ActivityMessagesBinding binding;
    private User user1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadDetails();
        Listeners();
    }
    private void loadDetails(){
        user1 = (User) getIntent().getSerializableExtra(Constants.USER);
        binding.name.setText(user1.name);
    }
    private void Listeners(){
        binding.back.setOnClickListener(v -> onBackPressed());
    }
}