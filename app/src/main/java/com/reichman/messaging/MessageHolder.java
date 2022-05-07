package com.reichman.messaging;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

// Message Holder Class
class MessageHolder extends RecyclerView.ViewHolder {

    // Constructor Value Declaration For Others
    public CardView card;
    public ImageView image;
    public TextView name;
    public TextView message;
    public TextView time;

    // Constructor Value Declaration For Current User
    public CardView cu_card;
    public ImageView cu_image;
    public TextView cu_name;
    public TextView cu_message;
    public TextView cu_time;

    public MessageHolder(@NonNull View itemView) {
        super(itemView);

        // Assign Constructor Values For Others
        card = itemView.findViewById(R.id.chat_message_card_others);
        image = itemView.findViewById(R.id.chat_image_others);
        name = itemView.findViewById(R.id.chat_user_others);
        message = itemView.findViewById(R.id.chat_message_others);
        time = itemView.findViewById(R.id.chat_time_stamp_others);

        // Assign Constructor Values For Current User
        cu_card = itemView.findViewById(R.id.chat_message_card);
        cu_image = itemView.findViewById(R.id.chat_image);
        cu_name = itemView.findViewById(R.id.chat_user);
        cu_message = itemView.findViewById(R.id.chat_message);
        cu_time = itemView.findViewById(R.id.chat_time_stamp);
    }
}