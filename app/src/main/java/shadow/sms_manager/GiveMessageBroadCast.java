package shadow.sms_manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import static shadow.sms_manager.IconCounterMessage.plus;

/**
 * Created by Peyman Razi on 30/03/2019.
 */

public class GiveMessageBroadCast extends BroadcastReceiver {
    Bundle bundle;
    Object object[];
    SmsMessage smsMessage;
    public static String messageText, messageNumber;

    @Override
    public void onReceive(Context context, Intent intent) {


        bundle = intent.getExtras();
        if (bundle != null) {

            object = (Object[]) bundle.get("pdus");


            messageText = "";
            for (int i = 0; i < object.length; i++) {

                smsMessage = SmsMessage.createFromPdu((byte[]) object[i]);
                messageText += smsMessage.getDisplayMessageBody();
                messageNumber = smsMessage.getDisplayOriginatingAddress();
            }

            //plus a message count to display on icon notification counter when received sms
            plus();

            new RegisterSms(messageText,messageNumber).execute();
        }


    }

}
