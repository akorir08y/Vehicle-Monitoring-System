package com.example.bus.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.bus.R;
import com.example.bus.models.User;

import java.util.ArrayList;

public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.ViewHolder>{

    private ArrayList<User> mUsers;
    private UserListRecyclerClickListener mClickListener;


    public UserRecyclerAdapter(ArrayList<User> users, UserListRecyclerClickListener clickListener) {
        this.mUsers = users;
        this.mClickListener = clickListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_list_item, parent, false);
        return new ViewHolder(view,mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.username.setText(mUsers.get(position).getUsername());
        holder.email.setText(mUsers.get(position).getEmail());
        holder.phone_number.setText(mUsers.get(position).getPhone_number());
        holder.full_name.setText(mUsers.get(position).getFull_name());
        holder.number_plate.setText(mUsers.get(position).getNumber_plate());
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView username, email,number_plate,full_name,phone_number;
        UserListRecyclerClickListener mClickListener;

        public ViewHolder(View itemView, UserListRecyclerClickListener clickListener) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            email = itemView.findViewById(R.id.email);
            number_plate = itemView.findViewById(R.id.number_plate);
            full_name = itemView.findViewById(R.id.full_name);
            phone_number = itemView.findViewById(R.id.phone_number);


            mClickListener = clickListener;
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            mClickListener.onUserClicked(getAdapterPosition());
        }
    }

    public interface UserListRecyclerClickListener{
        void onUserClicked(int position);
    }

}
















