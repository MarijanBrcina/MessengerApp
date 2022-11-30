package hr.unipu.android.messengerapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import hr.unipu.android.messengerapp.Adapter;
import hr.unipu.android.messengerapp.Message;
import hr.unipu.android.messengerapp.User;
import hr.unipu.android.messengerapp.databinding.ActivityMessagesBinding;
import hr.unipu.android.messengerapp.databinding.ActivityUsersBinding;
import hr.unipu.android.messengerapp.utilities.Constants;
import hr.unipu.android.messengerapp.utilities.PreferenceManager;

public class MessagesActivity extends AppCompatActivity {

    private ActivityMessagesBinding binding;
    private User user1;
    private List<Message> messages;
    private Adapter adapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadDetails();
        Listeners();
        init();
        listen();
    }
     private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        messages = new ArrayList<>();
        adapter = new Adapter(getBitmap(user1.picture), messages,
                        preferenceManager.getString(Constants.USER_ID));
        binding.RecyclerView.setAdapter(adapter);
        database = FirebaseFirestore.getInstance();
     }
     private void message(){
         HashMap<String, Object> message = new HashMap<>();
         message.put(Constants.SENDER, preferenceManager.getString(Constants.USER_ID));
         message.put(Constants.RECEIVER, user1.id);
         message.put(Constants.MESSAGE, binding.writeMessage.getText().toString());
         database.collection(Constants.COLLECTION).add(message);
         binding.writeMessage.setText(null);
     }
     private void listen(){
        database.collection(Constants.COLLECTION)
                .whereEqualTo(Constants.SENDER, preferenceManager.getString(Constants.USER_ID))
                .whereEqualTo(Constants.RECEIVER, user1.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.COLLECTION)
                .whereEqualTo(Constants.SENDER, user1.id)
                .whereEqualTo(Constants.RECEIVER, preferenceManager.getString(Constants.USER_ID))
                .addSnapshotListener(eventListener);
     }
     private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
         if (error!=null){
             return;
         }
         if (value != null) {
             int count = messages.size();
             for (DocumentChange documentChange : value.getDocumentChanges()){
                 if (documentChange.getType() == DocumentChange.Type.ADDED) {
                     Message message = new Message();
                     message.sender = documentChange.getDocument().getString(Constants.SENDER);
                     message.receiver = documentChange.getDocument().getString(Constants.RECEIVER);
                     message.message = documentChange.getDocument().getString(Constants.MESSAGE);
                     message.dateObject = documentChange.getDocument().getDate(Constants.TIME);
                     messages.add(message);
                 }
             }
//             Collections.sort(messages, (o1, o2) -> o1.dateObject.compareTo(o2.dateObject));
             if (count == 0){
                 adapter.notifyDataSetChanged();
             } else {
                 adapter.notifyItemRangeInserted(messages.size(), messages.size());
                 binding.RecyclerView.smoothScrollToPosition(messages.size()-1);
             }
             binding.RecyclerView.setVisibility(View.VISIBLE);
         }
     });
    private Bitmap getBitmap(String Image){
        byte[] bytes = Base64.decode(Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
    private void loadDetails(){
        user1 = (User) getIntent().getSerializableExtra(Constants.USER);
        binding.name.setText(user1.name);
    }
    private void Listeners(){
        binding.back.setOnClickListener(v -> onBackPressed());
        binding.send.setOnClickListener(v -> message());
    }
}