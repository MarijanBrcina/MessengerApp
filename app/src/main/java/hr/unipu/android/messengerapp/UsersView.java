package hr.unipu.android.messengerapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hr.unipu.android.messengerapp.databinding.UserBinding;

public class UsersView extends RecyclerView.Adapter<UsersView.UserView> {

    private List<User> users;

    public UsersView(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UserView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UserBinding userBinding = UserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserView(userBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserView holder, int position) {
        holder.UserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserView extends RecyclerView.ViewHolder {
        UserBinding binding;
        UserView(UserBinding userBinding){
            super(userBinding.getRoot());
            binding = userBinding;
        }
        void UserData(User user) {
            binding.name.setText(user.name);
            binding.profilePicture.setImageBitmap(UserPicture(user.picture));
        }
    }

    private Bitmap UserPicture(String encodedImage) {
       byte [] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
       return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
}
