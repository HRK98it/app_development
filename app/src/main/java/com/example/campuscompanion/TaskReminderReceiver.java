package com.example.campuscompanion;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class TaskReminderReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID="task_reminders";
    @Override public void onReceive(Context context, Intent intent){
        String title=intent.getStringExtra("title"); String subject=intent.getStringExtra("subject");
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26) nm.createNotificationChannel(new NotificationChannel(CHANNEL_ID,"Task Reminders",NotificationManager.IMPORTANCE_HIGH));
        Intent open=new Intent(context,MainActivity.class); open.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi=PendingIntent.getActivity(context,(int)System.currentTimeMillis(),open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b=new NotificationCompat.Builder(context,CHANNEL_ID).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Campus Companion Reminder").setContentText(title+(subject==null||subject.isEmpty()?"":" • "+subject)).setAutoCancel(true).setContentIntent(pi).setPriority(NotificationCompat.PRIORITY_HIGH);
        nm.notify((int)(System.currentTimeMillis()%100000),b.build());
    }
}
