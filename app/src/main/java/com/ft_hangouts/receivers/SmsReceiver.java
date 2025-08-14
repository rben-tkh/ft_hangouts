package com.ft_hangouts.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.ft_hangouts.R;

import java.util.List;
import com.ft_hangouts.activities.MessageActivity;
import com.ft_hangouts.database.DatabaseHelper;
import com.ft_hangouts.models.Contact;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    public static final String ACTION_SMS_RECEIVED = "com.ft_hangouts.SMS_RECEIVED";
    public static final String ACTION_CONTACT_CREATED = "com.ft_hangouts.CONTACT_CREATED";
    private static final String CHANNEL_ID = "SMS_NOTIFICATIONS";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            
            if (bundle != null) {
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    String format = bundle.getString("format");
                    
                    if (pdus != null) {
                        StringBuilder fullMessage = new StringBuilder();
                        String sender = null;
                        
                        for (Object pdu : pdus) {
                            SmsMessage smsMessage;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
                            } else {
                                smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                            }
                            
                            if (smsMessage != null) {
                                if (sender == null) {
                                    sender = smsMessage.getDisplayOriginatingAddress();
                                }
                                fullMessage.append(smsMessage.getDisplayMessageBody());
                            }
                        }
                        
                        if (sender != null && fullMessage.length() > 0) {
                            String completeMessage = fullMessage.toString();
                            Contact savedContact = saveReceivedMessage(context, sender, completeMessage);
                            
                            if (savedContact != null) {
                                notifyMessageReceived(context, savedContact.getId(), sender, completeMessage);
                                
                                if (!isMessageActivityVisible(context, savedContact.getId())) {
                                    createNotification(context, savedContact, completeMessage);
                                }
                            }
                        }
                        
                        abortBroadcast();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erreur lors du traitement du SMS: " + e.getMessage());
                }
            }
        }
    }
    
    private Contact saveReceivedMessage(Context context, String phoneNumber, String messageText) {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            
            Contact contact = findContactByPhoneNumber(dbHelper, phoneNumber);
            
            if (contact == null) {
                contact = new Contact(phoneNumber, phoneNumber, "", "", "", null);
                long contactId = dbHelper.addContact(contact);
                
                if (contactId > 0) {
                    contact.setId((int) contactId);
                    Log.d(TAG, "Nouveau contact créé automatiquement pour: " + phoneNumber);
                    
                    Intent broadcastIntent = new Intent(ACTION_CONTACT_CREATED);
                    broadcastIntent.putExtra("CONTACT_ID", (int) contactId);
                    broadcastIntent.putExtra("PHONE_NUMBER", phoneNumber);
                    context.sendBroadcast(broadcastIntent);
                } else {
                    Log.e(TAG, "Erreur lors de la création du contact pour: " + phoneNumber);
                    return null;
                }
            }
            
            dbHelper.addMessage(contact.getId(), messageText, false);
            Log.d(TAG, "Message sauvé pour le contact: " + contact.getName());
            return contact;
            
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la sauvegarde du message: " + e.getMessage());
        }
        
        return null;
    }

    private void notifyMessageReceived(Context context, int contactId, String sender, String messageBody) {
        try {
            Intent broadcastIntent = new Intent(ACTION_SMS_RECEIVED);
            broadcastIntent.putExtra("CONTACT_ID", contactId);
            broadcastIntent.putExtra("SENDER", sender);
            broadcastIntent.putExtra("MESSAGE_BODY", messageBody);
            
            context.sendBroadcast(broadcastIntent);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'envoi de la notification: " + e.getMessage());
        }
    }

    private boolean isMessageActivityVisible(Context context, int contactId) {
        try {
            return MessageActivity.isVisibleForContact(contactId);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la vérification de l'activité: " + e.getMessage());
        }
        return false;
    }

    private void createNotification(Context context, Contact contact, String messageBody) {
        try {
            NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            createNotificationChannel(notificationManager);
            
            Intent notificationIntent = new Intent(context, MessageActivity.class);
            notificationIntent.putExtra("CONTACT_ID", contact.getId());
            notificationIntent.putExtra("CONTACT_NAME", contact.getName());
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                contact.getId(), 
                notificationIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            Notification.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder = new Notification.Builder(context, CHANNEL_ID);
            } else {
                builder = new Notification.Builder(context);
            }
            
            builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(context.getString(R.string.new_message_from) + " " + contact.getName())
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 300, 100, 300})
                .setLights(0xFF0000FF, 1000, 1000);

            builder.setStyle(new Notification.BigTextStyle().bigText(messageBody));
            builder.setPriority(Notification.PRIORITY_HIGH);
            builder.setCategory(Notification.CATEGORY_MESSAGE);

            notificationManager.notify(contact.getId(), builder.build());
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la création de la notification: " + e.getMessage());
        }
    }
    
    private void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Messages SMS";
            String description = "Notifications pour les nouveaux messages SMS";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 300, 100, 300});
            channel.enableLights(true);
            channel.setLightColor(0xFF0000FF);
            
            notificationManager.createNotificationChannel(channel);
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;
        
        String normalized = phoneNumber.replaceAll("[\\s\\-\\.]", "");
        
        if (normalized.startsWith("+33")) {
            normalized = "0" + normalized.substring(3);
        }
        else if (normalized.startsWith("33") && normalized.length() == 11) {
            normalized = "0" + normalized.substring(2);
        }
        
        return normalized;
    }
    
    private Contact findContactByPhoneNumber(DatabaseHelper dbHelper, String phoneNumber) {
        try {
            return dbHelper.getContactByPhoneNumber(phoneNumber);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la recherche de contact: " + e.getMessage());
            return null;
        }
    }
}