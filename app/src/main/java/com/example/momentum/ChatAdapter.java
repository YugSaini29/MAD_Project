package com.example.momentum;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    List<Message> messages;

    public ChatAdapter(List<Message> messages){
        this.messages = messages;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView msgText;

        public ViewHolder (View view){
            super(view);
            msgText = view.findViewById(android.R.id.text1);
        }
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        holder.msgText.setText(messages.get(position).text);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
