package com.ft_hangouts.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ft_hangouts.models.Contact;
import com.ft_hangouts.R;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.util.List;

public class ContactAdapter extends ArrayAdapter<Contact> {

    private final Context context;
    private final List<Contact> contacts;

    public ContactAdapter(Context context, List<Contact> contacts) {
        super(context, R.layout.item_contact, contacts);
        this.context = context;
        this.contacts = contacts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);

            holder = new ViewHolder();
            holder.nameTextView = convertView.findViewById(R.id.nameTextView);
            holder.phoneTextView = convertView.findViewById(R.id.phoneTextView);
            holder.profileImageView = convertView.findViewById(R.id.profileImageView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contact contact = contacts.get(position);

        holder.nameTextView.setText(contact.getName());
        holder.phoneTextView.setText(contact.getPhoneNumber());
        
        // Apply dark mode theme to the item
        SharedPreferences prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        
        if (isDarkMode) {
            convertView.setBackgroundColor(Color.parseColor("#2B2B2B"));
            holder.nameTextView.setTextColor(Color.WHITE);
            holder.phoneTextView.setTextColor(Color.LTGRAY);
        } else {
            convertView.setBackgroundColor(Color.WHITE);
            holder.nameTextView.setTextColor(Color.BLACK);
            holder.phoneTextView.setTextColor(Color.GRAY);
        }
        
        if (contact.getPhotoPath() != null && !contact.getPhotoPath().isEmpty()) {
            File imgFile = new File(contact.getPhotoPath());
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                holder.profileImageView.setImageBitmap(bitmap);
            } else {
                holder.profileImageView.setImageResource(android.R.drawable.ic_menu_camera);
            }
        } else {
            holder.profileImageView.setImageResource(android.R.drawable.ic_menu_camera);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView nameTextView;
        TextView phoneTextView;
        ImageView profileImageView;
    }
}