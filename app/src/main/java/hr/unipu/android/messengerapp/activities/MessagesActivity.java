package hr.unipu.android.messengerapp.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private String jezik_odabran;
    private boolean kraj_chata = false;
    private int broj_poruka = 0;
    private int broj_poruka_temp = 0;  // broji sve trenutne poruke prije poruke koju si poslao
    private int final_broj_poruka = 0; // BROJI sve poruke
    public int a;
    public int stari_broj = 0;
    public boolean cont = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        jezik_odabran = getIntent().getExtras().getString("jezik"); // dobiva vrijednost iz proslog activitya
        if(jezik_odabran == null)
            a = 1;
        if(jezik_odabran == null)
            jezik_odabran = "org";
        if (jezik_odabran.equals("Francuski") )
            jezik_odabran = "FR";
        else if (jezik_odabran.equals("Engleski"))
            jezik_odabran = "EN";
        else if (jezik_odabran.equals("Njemački"))
            jezik_odabran = "DE";
        else if (jezik_odabran.equals("Španjolski"))
            jezik_odabran = "ES";
        else if (jezik_odabran.equals("Ruski"))
            jezik_odabran = "RU";
        else if (jezik_odabran.equals("Talijanski"))
            jezik_odabran = "IT";
        else
            jezik_odabran = "org";

        broj_poruka = 0;
        broj_poruka_temp = 0;
        final_broj_poruka = 0;

        kraj_chata = false; // uvijek pocetna vrijednost false , true je u slucaju kad se chat ispise cijeli samo
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
        message.put(Constants.TIME, new Date());
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
            stari_broj = count;
            if(kraj_chata == false || (kraj_chata == true && cont == true)){ // ispisat ce novu poruku samo ako nije kraj chata ili je bio kraj chata ali si ti napisaio ili dobio novu
                for (DocumentChange documentChange : value.getDocumentChanges()){ // broji promjene od zadnjeg updatea
                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                        ++broj_poruka;
                        Message message = new Message();
                        message.sender = documentChange.getDocument().getString(Constants.SENDER);
                        message.receiver = documentChange.getDocument().getString(Constants.RECEIVER);
                        message.message = documentChange.getDocument().getString(Constants.MESSAGE);
                        message.time = documentChange.getDocument().getString(Constants.MESSAGE);
                        message.dateObject = documentChange.getDocument().getDate(Constants.TIME);
                        if(jezik_odabran.equals("org")) // ako jezik nije odabran ili je odabran original onda odmah dodaju poruku i ide dalje
                            messages.add(message);
                        else {
                            //Funkcija za prepoznavanje jezika kojom je korisnik napisao originalnu poruku
                            prepoznaj_jezik(message.message, message.sender, message.receiver, message.dateObject, broj_poruka);

                        }
                    }
                }
            }
            if(jezik_odabran.equals("org")) { // odmah ispisuje poruku ako joj ne treba prijevod
                final_broj_poruka = broj_poruka;
                Collections.sort(messages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
                if (count == 0) {
                    adapter.notifyDataSetChanged();
                } else {

                    adapter.notifyItemRangeInserted(messages.size(), messages.size());
                    binding.RecyclerView.smoothScrollToPosition(messages.size() - 1);
                }
            }
            binding.RecyclerView.setVisibility(View.VISIBLE);
        }
        if (chatId == null){
            checkForChat();
        }
    });

    private void prevedi_poruku(String message_, String jezik, String sender, String receiver, Date dateObject, int broj_poruka) {
        final String[] prevedena_poruka = new String[1];

        if(jezik.equals("und") || jezik == null) { // ako ne moze prepoznat jezik ili se slucajno dobije null u varijabli ispisuje poruku bez prijevoda
            Message message = new Message();
            message.sender = sender;
            message.receiver = receiver;
            message.message = message_;
            message.dateObject = dateObject ;
            messages.add(message);
            adapter.notifyItemRangeInserted(messages.size(), messages.size());
            binding.RecyclerView.smoothScrollToPosition(messages.size()-1);
        }


        else {
            // U slucaju da je neki jezik brokean ili firebase ne moze vise prevodit na njega ide u ovaj if
            if (jezik.equals("bg-Latn") || jezik.equals("hi-Latn") || jezik.equals("ar-Latn") || jezik.equals("zh-Latn") || jezik.equals("ig"))
                jezik = "hr";
            FirebaseTranslatorOptions options =
                    new FirebaseTranslatorOptions.Builder()
                            .setSourceLanguage(FirebaseTranslateLanguage.languageForLanguageCode(jezik)) // Jezik na kojem je poruka napisana
                            .setTargetLanguage(FirebaseTranslateLanguage.languageForLanguageCode(jezik_odabran)) // Jezik na koji se treba prevesti poruka
                            .build();
            final FirebaseTranslator prevoditelj =
                    FirebaseNaturalLanguage.getInstance().getTranslator(options);
            FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()//Ako nema alate za prijevod ili paket s tim jezikom prvo provjerava je li moze preuzeti taj paket i jesu li svi uvijeti za prijevod tu
                    .requireWifi()
                    .build();
            prevoditelj.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener( // ako su zadovoljeni svi uvijeti i download nije trebao ili je trebao pa je uspjesno izvrsen onda ide na onSucces
                            new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void v) {
                                    prevedi_final(prevoditelj, message_, sender, receiver, dateObject,broj_poruka); // ako je jezik moguce downloadovat i prevest na njega ide u ovu funkciju
                                }
                            })
                    .addOnFailureListener( // ako je prijevod pao dobijes error
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Model couldn’t be downloaded or other internal error.
                                    // ...
                                }
                            });


        }
    }

    private void prevedi_final(FirebaseTranslator prevoditelj, String message_, String sender, String receiver, Date dateObject, int broj_poruka) {
        ++broj_poruka_temp;
        kraj_chata = false;
        prevoditelj.translate(message_)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) { // AKO JE prijevod uspjesan ide u ovu funkciju

                                // Translation successful.
                                Message message = new Message();
                                message.sender = sender;
                                message.receiver = receiver;
                                message.message = translatedText; //  tekst je vadjen iz varijable koju dobijes kao parametar onSucces funkcije
                                message.dateObject = dateObject;
                                messages.add(message); // ako je uspijesan prijevod odmah prevede i ažurira
                                Collections.sort(messages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject)); //Sortiranje poruka

                                adapter.notifyItemRangeInserted(messages.size(), messages.size());
                                binding.RecyclerView.smoothScrollToPosition(messages.size()-1);

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });}



    private void prepoznaj_jezik(String message_, String sender, String receiver, Date dateObject, int broj_poruka) {
        final String[] jezik_ = {new String()};

        FirebaseLanguageIdentification languageIdentifier =
                FirebaseNaturalLanguage.getInstance().getLanguageIdentification();
        Task<String> stringTask = languageIdentifier.identifyLanguage(message_)
                .addOnSuccessListener(
                        languageCode -> {
                            if (languageCode != "und" && !languageCode.isEmpty() ) { // Ukoliko kod jezika nije nedefiniran i nije prazan
                                jezik_[0] = languageCode; // kod jezika kojeg je prepoznao se dobije iz parametra onSuccesslistenera
                                Message message = new Message();
                                message.sender = sender;
                                message.receiver = receiver;
                                message.message = message_;
                                message.dateObject = dateObject ;
                                prevedi_poruku(message.message, jezik_[0],sender, receiver, dateObject,broj_poruka);// Ukoliko prepozna jezik salje ga dalje na prijevod

                            }
                            else {
                                jezik_[0] = "und"; // ako je kod jezika nedefiran ili prazan onda ispisuje original
                                Message message = new Message();
                                message.sender = sender;
                                message.receiver = receiver;
                                message.message = message_;
                                message.dateObject = dateObject ;
                                messages.add(message);
                                Collections.sort(messages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));

                                adapter.notifyItemRangeInserted(messages.size(), messages.size());
                                binding.RecyclerView.smoothScrollToPosition(messages.size()-1); // ako ne moze odredit jezik ispisuje poruku u originalu
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            jezik_[0] = "und"; // ako padne jezik postavlja ga kao gresku i vraca poruku u originalu
                        });

    }
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
    private String getTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
    private void addChat(HashMap<String,Object> chat) {

        database.collection(Constants.COLLECTION_CHAT)
                .add(chat)
                .addOnSuccessListener(documentReference -> chatId = documentReference.getId());
    }
    private void updateChat(String message){
        cont = true;
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