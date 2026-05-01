package com.example.momentum;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
        LinearLayout container;

        public ViewHolder(View view) {
            super(view);
            msgText = view.findViewById(R.id.messageText);
            container = view.findViewById(R.id.messageContainer);
        }
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.msgText.setText(message.text);

        if (message.isUser) {
            holder.container.setGravity(Gravity.END);
            holder.msgText.setBackgroundResource(R.drawable.bg_chat_user);
        } else {
            holder.container.setGravity(Gravity.START);
            holder.msgText.setBackgroundResource(R.drawable.bg_chat_ai);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
