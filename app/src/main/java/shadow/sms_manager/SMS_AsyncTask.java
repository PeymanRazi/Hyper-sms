package shadow.sms_manager;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import static shadow.sms_manager.CompressAsyncTask.submitInbox;
import static shadow.sms_manager.MainActivity.array;

/**
 * Created by Peyman Razi on 05/03/2019.
 */

public class SMS_AsyncTask extends AsyncTask {

    Thread thread;
    Handler handler;
    PendingIntent sentPI, deliveredPI;
    SmsManager sms;
    Activity activity;
    String passageMessage;
    int finalNumber = 0;
    String onceAddress = null;

    public SMS_AsyncTask(Activity activity, String x, @Nullable String onceAddress) {

        this.onceAddress = onceAddress;
        this.activity = activity;
        this.sms = SmsManager.getDefault();
        this.passageMessage = x;
        handler = new Handler();

    }


    @Override
    protected Object doInBackground(Object[] params) {

        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        //for detecting sms delivered
                        SmsDeliver();
                    }
                });
            }
        });
        thread.start();
        return "";
    }


    //for detecting sms delivered
    private void SmsDeliver() {

        try {
            String SENT = "SMS_SENT";
            String DELIVERED = "SMS_DELIVERED";

            sentPI = PendingIntent.getBroadcast(activity, 0,
                    new Intent(SENT), 0);

            deliveredPI = PendingIntent.getBroadcast(activity, 0,
                    new Intent(DELIVERED), 0);

            //---when the SMS has been sent---
            activity.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {

                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Toast.makeText(activity.getBaseContext(), "شکست عمومی",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Toast.makeText(activity.getBaseContext(), "بدون سرویس",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Toast.makeText(activity.getBaseContext(), "خالیست PDU",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Toast.makeText(activity.getBaseContext(), "ارتباط بر قرار نیست",
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }, new IntentFilter(SENT));

            //---when the SMS has been delivered---
            activity.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Toast.makeText(activity.getBaseContext(), " تحویل داداه شد ",
                                    Toast.LENGTH_SHORT).show();


                            break;
                        case Activity.RESULT_CANCELED:
                            Toast.makeText(activity.getBaseContext(), " تحویل داداه نشد ",
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }, new IntentFilter(DELIVERED));

        } catch (Exception e) {
            e.printStackTrace();
        }

        //sms sending operations
        SendSMS();


    }


    //---sends an SMS message to another device---
    private void SendSMS() {


        try {
            ArrayList<String> msgArray = sms.divideMessage(passageMessage);
            ArrayList<PendingIntent> x = new ArrayList<>();
            ArrayList<PendingIntent> y = new ArrayList<>();

            x.add(sentPI);
            y.add(deliveredPI);


            if (onceAddress.isEmpty()) {

                News info;
                for (int i = 0; i < array.size(); i++) {

                    info = array.get(i);

                    if (info.check) {

                        finalNumber++;
                        sms.sendMultipartTextMessage(info.phone, null, msgArray, x, y);

                        //insert to inbox message after take the sms
                        ContentValues values = new ContentValues();
                        values.put("body", submitInbox);
                        values.put("address", info.phone);
                        G.context.getContentResolver().insert(Uri.parse("content://sms/sent/"), values);


                    }

                }

            } else {
                sms.sendMultipartTextMessage(onceAddress, null, msgArray, x, y);

                //insert to inbox message after take the sms
                ContentValues values = new ContentValues();
                values.put("body", passageMessage);
                values.put("address", onceAddress);
                G.context.getContentResolver().insert(Uri.parse("content://sms/sent/"), values);


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
