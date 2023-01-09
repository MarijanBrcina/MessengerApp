package hr.unipu.android.messengerapp.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import hr.unipu.android.messengerapp.utilities.Constants;
import hr.unipu.android.messengerapp.utilities.PreferenceManager;

public class UserStatus extends AppCompatActivity {
    private DocumentReference documentReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        documentReference = database.collection(Constants.USERS)
                .document(preferenceManager.getString(Constants.USER_ID));

    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Constants.USER_STATUS, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Constants.USER_STATUS, 1);
    }
}
