package shadow.sms_manager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import static shadow.sms_manager.GiveMessageBroadCast.messageNumber;
import static shadow.sms_manager.GiveMessageBroadCast.messageText;

/**
 * Created by Peyman Razi on 05/05/2019.
 */

public class RegisterSms extends AsyncTask {
    public static SQLiteDatabase database;
    public static Cursor cu = null;
    public static String text = "";
    public static boolean isOfSmsManager = false;
    MyDatabase mydb;
    String flag = "*#";
    String textCode, phone;
    String container = "";

    public RegisterSms(String textCode, String phone) {
        this.textCode = textCode;
        this.phone = phone;
    }

    private void finder(String x, String y, String z) {

        try {
            cu = database.rawQuery("SELECT * FROM Words WHERE code='" + x + "'", null);
            while (cu.moveToNext()) {

                text += cu.getString(2);
                text = text.replace("\n", "");

                if (!y.isEmpty()) {
                    if (y.equals("ঔ")) {
                        text += "ی";
                    } else if (y.equals("ঐ")) {
                        text += "م";
                    } else if (y.equals("আ")) {
                        text += "ای";
                    } else if (y.equals("ঈ")) {
                        text += "ست";
                    } else if (y.equals("ঊ")) {
                        text += "ه";
                    } else if (y.equals("ভ")) {
                        text += "ها";
                    } else if (y.equals("ঙ")) {
                        text += "ان";
                    } else if (y.equals("ঘ")) {
                        text += "؟";
                    } else if (y.equals("ধ")) {
                        text += "!";
                    } else if (y.equals("ঝ")) {
                        text += "،";
                    } else if (y.equals("ও")) {
                        text += ":";
                    } else if (y.equals("এ")) {
                        text += "؛";
                    } else if (y.equals("অ")) {
                        text += ".";
                    } else if (y.equals("ফ")) {
                        text += "\\";
                    } else if (y.equals("উ")) {
                        text += "/";
                    }

                }
                if (!z.isEmpty()) {

                    if (z.equals("ঘ")) {
                        text += "؟";
                    } else if (z.equals("ধ")) {
                        text += "!";
                    } else if (z.equals("ঝ")) {
                        text += "،";
                    } else if (z.equals("ও")) {
                        text += ":";
                    } else if (z.equals("এ")) {
                        text += "؛";
                    } else if (z.equals("অ")) {
                        text += ".";
                    } else if (z.equals("ফ")) {
                        text += "\\";
                    } else if (z.equals("উ")) {
                        text += "/";
                    }

                }

                text += " ";


            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected Object doInBackground(Object[] params) {


        try {
            mydb = new MyDatabase(G.context);
            database = mydb.getReadableDatabase();
            //this line check the first two letters for detect sms is of other app sms_manager
            container = String.valueOf(textCode.charAt(0)) + String.valueOf(textCode.charAt(1));
            if (container.equals(flag)) {
                isOfSmsManager = true;

                for (int i = 2; i < textCode.length(); i++) {

                    //store text that not exist in db
                    if (textCode.charAt(i) == '*') {
                        i++;
                        while (textCode.charAt(i) != '*') {

                            text += textCode.charAt(i);
                            i++;

                        }
                        text += " ";
                    } else if (Character.UnicodeBlock.of(textCode.charAt(i)) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                        container = String.valueOf(textCode.charAt(i));

                        if ((i + 2) < textCode.length())
                            finder(container, String.valueOf(textCode.charAt(i+1)), String.valueOf(textCode.charAt(i + 2)));
                        else if ((i + 1) < textCode.length())
                            finder(container, String.valueOf(textCode.charAt(i)+1), "");
                        else
                            finder(container, "", "");
//                    i = i + 1;
                    } else if (Character.UnicodeBlock.of(textCode.charAt(i)) == Character.UnicodeBlock.ARABIC &&
                            Character.UnicodeBlock.of(textCode.charAt(i + 1)) == Character.UnicodeBlock.ARABIC) {

                        container = String.valueOf(textCode.charAt(i)) + String.valueOf(textCode.charAt(i + 1));

                        if ((i + 3) < textCode.length())
                            finder(container, String.valueOf(textCode.charAt(i+2)), String.valueOf(textCode.charAt(i + 3)));
                        else if ((i + 2) < textCode.length())
                            finder(container, String.valueOf(textCode.charAt(i + 2)), "");
                        else
                            finder(container, "", "");
                        i = i + 1;

                    } else if (Character.UnicodeBlock.of(textCode.charAt(i)) == Character.UnicodeBlock.ARABIC &&
                            Character.UnicodeBlock.of(textCode.charAt(i + 1)) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                        container = String.valueOf(textCode.charAt(i)) + String.valueOf(textCode.charAt(i + 1));

                        if ((i + 3) < textCode.length())
                            finder(container, String.valueOf(textCode.charAt(i+2)), String.valueOf(textCode.charAt(i + 3)));
                        else if ((i + 2) < textCode.length())
                            finder(container, String.valueOf(textCode.charAt(i + 2)), "");
                        else
                            finder(container, "", "");
                        i = i + 1;
                    } else if (Character.UnicodeBlock.of(textCode.charAt(i)) == Character.UnicodeBlock.CYRILLIC) {
                        container = String.valueOf(textCode.charAt(i)) + String.valueOf(textCode.charAt(i + 1) + String.valueOf(textCode.charAt(i + 2)));

                        if ((i + 4) < textCode.length())
                            finder(container, String.valueOf(textCode.charAt(i+3)), String.valueOf(textCode.charAt(i + 4)));
                        else if ((i + 3) < textCode.length())
                            finder(container, String.valueOf(textCode.charAt(i + 3)), "");
                        else
                            finder(container, "", "");
                        i = i + 2;
                    } else if (Character.UnicodeBlock.of(textCode.charAt(i)) == Character.UnicodeBlock.GREEK) {
                        container = String.valueOf(textCode.charAt(i)) + String.valueOf(textCode.charAt(i + 1) + String.valueOf(textCode.charAt(i + 2)));

                        if ((i + 4) < textCode.length())
                            finder(container, String.valueOf(textCode.charAt(i+3)), String.valueOf(textCode.charAt(i + 4)));
                        else if ((i + 3) < textCode.length())
                            finder(container, String.valueOf(textCode.charAt(i + 3)), "");
                        else
                            finder(container, "", "");
                        i = i + 2;
                    } else if (Character.UnicodeBlock.of(textCode.charAt(i)) == Character.UnicodeBlock.DEVANAGARI) {
                        container = String.valueOf(textCode.charAt(i)) + String.valueOf(textCode.charAt(i + 1) + String.valueOf(textCode.charAt(i + 2)));

                        if ((i + 4) < textCode.length())
                            finder(container, String.valueOf(textCode.charAt(i+3)), String.valueOf(textCode.charAt(i + 4)));
                        else if ((i + 3) < textCode.length())
                            finder(container, String.valueOf(textCode.charAt(i + 3)), "");
                        else
                            finder(container, "", "");
                        i = i + 2;
                    } else if (Character.UnicodeBlock.of(textCode.charAt(i)) == Character.UnicodeBlock.HANGUL_SYLLABLES
                            || Character.UnicodeBlock.of(textCode.charAt(i)) == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) {
                        container = String.valueOf(textCode.charAt(i)) + String.valueOf(textCode.charAt(i + 1) + String.valueOf(textCode.charAt(i + 2) + String.valueOf(textCode.charAt(i + 3))));

                        if ((i + 4) < textCode.length())
                            finder(container, String.valueOf(textCode.charAt(i+3)), String.valueOf(textCode.charAt(i + 4)));
                        else if ((i + 3) < textCode.length())
                            finder(container, String.valueOf(textCode.charAt(i + 3)), "");
                        else
                            finder(container, "", "");
                        i = i + 3;
                    }

                }


            } else {
                isOfSmsManager = false;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        if (isOfSmsManager) {

            //insert to inbox message after take the sms
            ContentValues values = new ContentValues();
            values.put("body", text);
            values.put("address", phone);
            G.context.getContentResolver().insert(Uri.parse("content://sms/inbox/"), values);
            //notify the sms
            NotificationCompat.Builder notificationCompat = (NotificationCompat.Builder) new NotificationCompat.Builder(G.context)
                    .setSmallIcon(android.R.drawable.sym_action_chat)
                    .setLargeIcon(BitmapFactory.decodeResource(G.context.getApplicationContext().getResources(), R.drawable.smslogo))
                    .setContentTitle(Function.getContactbyPhoneNumber(G.context,messageNumber))
                    .setContentText(text)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

            //intent to chat
            Intent intent = new Intent(G.context, Chat.class);
            intent.putExtra("name", "");
            intent.putExtra("address", phone);

            TaskStackBuilder stackBuilder=TaskStackBuilder.create(G.context);
            stackBuilder.addParentStack(Chat.class);
            stackBuilder.addNextIntent(intent);

            PendingIntent pendingIntent=stackBuilder.getPendingIntent(1,PendingIntent.FLAG_CANCEL_CURRENT);
            notificationCompat.setContentIntent(pendingIntent);
            notificationCompat.setAutoCancel(true);

            //intent to chat

            NotificationManager manager = (NotificationManager) G.context.getSystemService(G.context.NOTIFICATION_SERVICE);
            manager.notify(101, notificationCompat.build());


        } else {

            //insert to inbox message after take the sms out of our app
            ContentValues values = new ContentValues();
            values.put("body", textCode);
            values.put("address", phone);
            G.context.getContentResolver().insert(Uri.parse("content://sms/inbox/"), values);
            //notify the sms if the sms not sent of other our app
            NotificationCompat.Builder notificationCompat = (NotificationCompat.Builder) new NotificationCompat.Builder(G.context)
                    .setSmallIcon(android.R.drawable.sym_action_chat)
                    .setLargeIcon(BitmapFactory.decodeResource(G.context.getApplicationContext().getResources(), R.drawable.smslogo))
                    .setContentTitle(Function.getContactbyPhoneNumber(G.context,messageNumber))
                    .setContentText(messageText)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

            //intent to chat
            Intent intent = new Intent(G.context, Chat.class);
            intent.putExtra("name", "");
            intent.putExtra("address", phone);
            intent.putExtra("thread_id", "receive");

            TaskStackBuilder stackBuilder=TaskStackBuilder.create(G.context);
            stackBuilder.addParentStack(Chat.class);
            stackBuilder.addNextIntent(intent);

            PendingIntent pendingIntent=stackBuilder.getPendingIntent(1,PendingIntent.FLAG_CANCEL_CURRENT);
            notificationCompat.setContentIntent(pendingIntent);
            notificationCompat.setAutoCancel(true);

            //intent to chat

            NotificationManager manager = (NotificationManager) G.context.getSystemService(G.context.NOTIFICATION_SERVICE);
            manager.notify(101, notificationCompat.build());
        }

        text = "";


    }
}
