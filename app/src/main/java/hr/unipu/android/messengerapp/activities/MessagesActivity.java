package hr.unipu.android.messengerapp.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import hr.unipu.android.messengerapp.Adapter;
import hr.unipu.android.messengerapp.Message;
import hr.unipu.android.messengerapp.User;
import hr.unipu.android.messengerapp.databinding.ActivityMessagesBinding;
import hr.unipu.android.messengerapp.utilities.Constants;
import hr.unipu.android.messengerapp.utilities.PreferenceManager;

public class MessagesActivity extends UserStatus {

    private ActivityMessagesBinding binding;
    private User user1;
    private List<Message> messages;
    private Adapter adapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String chatId=null;
    private Boolean isOnline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Listeners();
        loadDetails();
        init();
        Messages();
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
         if (chatId != null) {
             updateChat(binding.writeMessage.getText().toString());
         } else {
             HashMap<String, Object> chat = new HashMap<>();
             chat.put(Constants.SENDER, preferenceManager.getString(Constants.USER_ID));
             chat.put(Constants.NAME_SENDER, preferenceManager.getString(Constants.NAME));
             chat.put(Constants.PICTURE_SENDER, preferenceManager.getString(Constants.IMAGE));
             chat.put(Constants.RECEIVER, user1.id);
             chat.put(Constants.NAME_RECEIVER, user1.name);
             chat.put(Constants.PICTURE_RECEIVER, user1.picture);
             chat.put(Constants.TIME, new Date());
             addChat(chat);
         }
         binding.writeMessage.setText(null);
     }
     private void UserStatus (){
        database.collection(Constants.USERS).document(
                user1.id
        ).addSnapshotListener(MessagesActivity.this, (value, error) -> {
            if (error != null){
                return;
            }
            if (value != null){
                if (value.getLong(Constants.USER_STATUS) != null) {
                    int status = Objects.requireNonNull(
                            value.getLong(Constants.USER_STATUS)
                    ).intValue();
                    isOnline = status == 1;
                }
            }
            if (isOnline){
                binding.textStatus.setVisibility(View.VISIBLE);
            }else {
                binding.textStatus.setVisibility(View.GONE);
            }
        });
    }
     private void Messages(){
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
             //Collections.sort(messages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
             if (count == 0){
                 adapter.notifyDataSetChanged();
             } else {
                 adapter.notifyItemRangeInserted(messages.size(), messages.size());
                 binding.RecyclerView.smoothScrollToPosition(messages.size()-1);
             }
             binding.RecyclerView.setVisibility(View.VISIBLE);
         }
         if (chatId == null){
             checkForChat();
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
    private void addChat(HashMap<String,Object> chat) {
        database.collection(Constants.COLLECTION_CHAT)
                .add(chat)
                .addOnSuccessListener(documentReference -> chatId = documentReference.getId());
    }
    private void updateChat(String message){
        DocumentReference documentReference =
                database.collection(Constants.COLLECTION_CHAT).document(chatId);
        documentReference.update(
                Constants.TIME, new Date()
        );
    }
    private void checkForChat(){
        if (messages.size() != 0){
            checkChat(
                    preferenceManager.getString(Constants.USER_ID),
                    user1.id
            );
            checkChat(
                    user1.id,
                    preferenceManager.getString(Constants.USER_ID)
            );
        }
    }
    private void checkChat(String senderId, String receiverId){
        database.collection(Constants.COLLECTION_CHAT)
                .whereEqualTo(Constants.SENDER, senderId)
                .whereEqualTo(Constants.RECEIVER, receiverId)
                .get()
                .addOnCompleteListener(messageOnCompleteListener);
    }
    private final OnCompleteListener<QuerySnapshot> messageOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size()>0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            chatId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        UserStatus();
    }
}