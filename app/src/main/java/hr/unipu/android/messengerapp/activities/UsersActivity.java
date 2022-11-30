package hr.unipu.android.messengerapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import hr.unipu.android.messengerapp.Listener;
import hr.unipu.android.messengerapp.User;
import hr.unipu.android.messengerapp.UsersView;
import hr.unipu.android.messengerapp.databinding.ActivityUsersBinding;
import hr.unipu.android.messengerapp.utilities.Constants;
import hr.unipu.android.messengerapp.utilities.PreferenceManager;

public class UsersActivity extends AppCompatActivity implements Listener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        getUsers();
        setListeners();
    }
    private void setListeners(){
        binding.Back.setOnClickListener(v-> onBackPressed());
    }

    private void getUsers(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.USERS)
                .get()
                .addOnCompleteListener(task -> {

                    String currentId = preferenceManager.getString(Constants.USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (currentId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.NAME);
                            user.picture = queryDocumentSnapshot.getString(Constants.IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size()>0) {
                            UsersView usersView = new UsersView(users, this);
                            binding.userView.setAdapter(usersView);
                            binding.userView.setVisibility(View.VISIBLE);
                        }
                    }
                });

    }

    @Override
    public void onClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), MessagesActivity.class);
        intent.putExtra(Constants.USER,user);
        startActivity(intent);
        finish();
    }
}