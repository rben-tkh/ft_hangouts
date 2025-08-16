package com.ft_hangouts.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ft_hangouts.models.Message;
import com.ft_hangouts.R;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends ArrayAdapter<Message> {

    private final Context context;
    private final List<Message> messages;

    public MessageAdapter(Context context, List<Message> messages) {
        super(context, 0, messages);
        this.context = context;
        this.messages = messages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = messages.get(position);

        if (message.isSent()) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
        } else {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
        }
        
        android.widget.LinearLayout messageContainer = convertView.findViewById(R.id.messageContainer);

        TextView messageTextView = convertView.findViewById(R.id.messageTextView);
        TextView timeTextView = convertView.findViewById(R.id.timeTextView);

        messageTextView.setText(message.getMessageText());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String timeString = sdf.format(new Date(message.getTimestamp()));
        timeTextView.setText(timeString);

        SharedPreferences prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        
        if (isDarkMode) {
            if (message.isSent()) {
                if (messageContainer != null) {
                    messageContainer.setBackgroundResource(R.drawable.message_sent_background_dark);
                }
                messageTextView.setTextColor(Color.WHITE);
                timeTextView.setTextColor(Color.LTGRAY);
            } else {
                if (messageContainer != null) {
                    messageContainer.setBackgroundResource(R.drawable.message_received_background_dark);
                }
                messageTextView.setTextColor(Color.WHITE);
                timeTextView.setTextColor(Color.LTGRAY);
            }
        } else {
            if (message.isSent()) {
                if (messageContainer != null) {
                    messageContainer.setBackgroundResource(R.drawable.message_sent_background);
                }
                messageTextView.setTextColor(Color.BLACK);
                timeTextView.setTextColor(Color.DKGRAY);
            } else {
                if (messageContainer != null) {
                    messageContainer.setBackgroundResource(R.drawable.message_received_background);
                }
                messageTextView.setTextColor(Color.BLACK);
                timeTextView.setTextColor(Color.DKGRAY);
            }
        }

        return convertView;
    }
}