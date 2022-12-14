package hr.unipu.android.messengerapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hr.unipu.android.messengerapp.databinding.LastConversationBinding;

public class LastMessageAdapter extends RecyclerView.Adapter<LastMessageAdapter.ChatViewHolder> {
    private final List<Message> messages;
    private final MessagesListener messagesListener;

    public LastMessageAdapter(List<Message> messages, MessagesListener messagesListener) {
        this.messages = messages;
        this.messagesListener = messagesListener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatViewHolder(
                LastConversationBinding.inflate(
                       LayoutInflater.from(parent.getContext()),
                       parent,
                       false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull LastMessageAdapter.ChatViewHolder holder, int position) {
    holder.data(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder{

        LastConversationBinding binding;
        ChatViewHolder(LastConversationBinding lastConversationBinding){
            super(lastConversationBinding.getRoot());
            binding = lastConversationBinding;
        }
        void data(Message message){
            binding.profilePicture.setImageBitmap(getPicture(message.conPicture));
            binding.name.setText(message.conName);
            binding.lastMessage.setText(message.message);
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = message.conId;
                user.name = message.conName;
                user.picture = message.conPicture;
                messagesListener.onClicked(user);
            });
        }
    }
    private Bitmap getPicture(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
}
