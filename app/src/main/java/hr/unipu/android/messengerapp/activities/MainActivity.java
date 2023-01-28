package hr.unipu.android.messengerapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import android.util.Base64;

import hr.unipu.android.messengerapp.ConversationsAdapter;
import hr.unipu.android.messengerapp.Message;
import hr.unipu.android.messengerapp.MessagesListener;
import hr.unipu.android.messengerapp.R;
import hr.unipu.android.messengerapp.User;
import hr.unipu.android.messengerapp.databinding.ActivityMainBinding;
import hr.unipu.android.messengerapp.utilities.Constants;
import hr.unipu.android.messengerapp.utilities.PreferenceManager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends UserStatus implements MessagesListener {

    String[] item = {"Engleski","Njemački","Francuski","Talijanski","Ruski","Španjolski" };

    AutoCompleteTextView autoCompleteTextView;

    ArrayAdapter<String> adapterItems;

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<Message> chat;
    private ConversationsAdapter messageAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        userDetails();
        getToken();
        logoutListeners();
        chats();
        autoCompleteTextView = findViewById(R.id.select_language);
        adapterItems = new ArrayAdapter<String>(this, R.layout.list_language, item);

        autoCompleteTextView.setAdapter(adapterItems);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String item = adapterView.getItemAtPosition(position).toString();
                Toast.makeText(MainActivity.this, "Odabrani jezik: " + item, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void init(){
        chat = new ArrayList<>();
        messageAdapter = new ConversationsAdapter(chat, this);
        binding.chatRecyclerView.setAdapter(messageAdapter);
        database = FirebaseFirestore.getInstance();
    }
    private void userDetails(){
        binding.text.setText(preferenceManager.getString(Constants.NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.IMAGE), android.util.Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        binding.profilePicture.setImageBitmap(bitmap);
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void chats(){
        database.collection(Constants.COLLECTION_CHAT)
                .whereEqualTo(Constants.SENDER, preferenceManager.getString(Constants.USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.COLLECTION_CHAT)
                .whereEqualTo(Constants.RECEIVER, preferenceManager.getString(Constants.USER_ID))
                .addSnapshotListener(eventListener);
    }
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType()== DocumentChange.Type.ADDED){
                    String sender = documentChange.getDocument().getString(Constants.SENDER);
                    String receiver = documentChange.getDocument().getString(Constants.RECEIVER);
                    Message message = new Message();
                    message.sender = sender;
                    message.receiver = receiver;
                    if (preferenceManager.getString(Constants.USER_ID).equals(sender)){
                        message.conPicture = documentChange.getDocument().getString(Constants.PICTURE_RECEIVER);
                        message.conName = documentChange.getDocument().getString(Constants.NAME_RECEIVER);
                        message.conId = documentChange.getDocument().getString(Constants.RECEIVER);
                    } else {
                        message.conPicture = documentChange.getDocument().getString(Constants.PICTURE_SENDER);
                        message.conName = documentChange.getDocument().getString(Constants.NAME_SENDER);
                        message.conId = documentChange.getDocument().getString(Constants.SENDER);
                    }
                    chat.add(message);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (int i = 0; i < chat.size(); i++){
                        String sender = documentChange.getDocument().getString(Constants.SENDER);
                        String receiver = documentChange.getDocument().getString(Constants.RECEIVER);
                        if (chat.get(i).sender.equals(sender) && chat.get(i).receiver.equals(receiver)){
                            chat.get(i).dateObject = documentChange.getDocument().getDate(Constants.TIME);
                            break;
                        }
                    }
                }
            }
            //Collections.sort(chat, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            messageAdapter.notifyDataSetChanged();
            binding.chatRecyclerView.smoothScrollToPosition(0);
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
    };
    private void updateToken(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.USERS).document(
                preferenceManager.getString(Constants.USER_ID)
                );
        documentReference.update(Constants.FCM_TOKEN, token);
    }
    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }
    private void logout(){
        showToast("Odjava");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.USERS).document(
                preferenceManager.getString(Constants.USER_ID)
        );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(unused -> {
            preferenceManager.clear();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        })
                .addOnFailureListener(e -> showToast("Odjava nije moguća"));
    }
    private void logoutListeners (){
        binding.logout.setOnClickListener(v -> logout());
        binding.NewMessage.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), MessagesActivity.class);
        intent.putExtra(Constants.USER, user);
        startActivity(intent);
    }
}