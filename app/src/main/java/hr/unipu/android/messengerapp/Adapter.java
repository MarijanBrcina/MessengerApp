package hr.unipu.android.messengerapp;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hr.unipu.android.messengerapp.databinding.MessageSentBinding;
import hr.unipu.android.messengerapp.databinding.MessageReceivedBinding;

public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Bitmap ProfilePicture;
    private final List<Message>messages;
    private final String sender;

    public static final int SENT = 1;
    public static final int RECEIVED = 2;

    public Adapter(Bitmap profilePicture, List<Message> messages, String sender) {
        ProfilePicture = profilePicture;
        this.messages = messages;
        this.sender = sender;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENT) {
            return new SentMessage(
                    MessageSentBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else {
            return new SentMessage.ReceivedMessage(
                    MessageReceivedBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position)==SENT){
            ((SentMessage)holder).Data(messages.get(position));
        } else {
            ((SentMessage.ReceivedMessage)holder).Data(messages.get(position), ProfilePicture);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).sender.equals(sender)){
            return SENT;
        } else {
            return RECEIVED;
        }
    }

    static class SentMessage extends RecyclerView.ViewHolder{
        private final MessageSentBinding binding;

        SentMessage(@NonNull MessageSentBinding messageContainerBinding){
            super(messageContainerBinding.getRoot());
            binding = messageContainerBinding;
        }
        void Data(Message message){
            binding.messageText.setText(message.message);
        }
        static class ReceivedMessage extends RecyclerView.ViewHolder{
            private final MessageReceivedBinding binding;
            ReceivedMessage (MessageReceivedBinding messageReceivedBinding) {
                super(messageReceivedBinding.getRoot());
                binding = messageReceivedBinding;
            }
            void Data(Message message, Bitmap ProfilePicture) {
                binding.message.setText(message.message);
                binding.profilePicture.setImageBitmap(ProfilePicture);
            }
        }
    }
}
