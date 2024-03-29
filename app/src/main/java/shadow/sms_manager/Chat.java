package shadow.sms_manager;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static shadow.sms_manager.Function.getContactbyPhoneNumber;
import static shadow.sms_manager.G.context;
import static shadow.sms_manager.IconCounterMessage.mines;

/**
 * Created by SHAJIB on 7/13/2017.
 */

public class Chat extends AppCompatActivity {

    ListView listView;
    ChatAdapter adapter;
    LoadSms loadsmsTask;
    String name;
    String address;
    EditText new_message;
    ImageView send_message, backOfList3;
    TextView contactAddress;
    int thread_id_main;
    private Handler handler = new Handler();
    Thread t;
    int sendType = 0;
    ArrayList<HashMap<String, String>> smsList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> customList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> tmpList = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        backOfList3 = (ImageView) findViewById(R.id.backOfList3);
        listView = (ListView) findViewById(R.id.listView);
        new_message = (EditText) findViewById(R.id.new_message);
        send_message = (ImageView) findViewById(R.id.send_message);
        contactAddress = (TextView) findViewById(R.id.contactAddress);


        //get data by intent to process and display on actionbar
        IntentProcessing();

        backOfList3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        send_message.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String text = new_message.getText().toString();


                if (text.length() > 0) {
                    String tmp_msg = text;
                    new_message.setText("در حال ارسال...");
                    new_message.setEnabled(false);


                    switch (sendType) {
                        case 1:
                            if (Function.sendSMS(address, tmp_msg)) {
                                new_message.setText("");
                                new_message.setEnabled(true);
                                // Creating a custom list for newly added sms
                                customList.clear();
                                customList.addAll(smsList);
                                customList.add(Function.mappingInbox(null, null, null, null, tmp_msg, "2", null, "Sending...", null));
                                adapter = new ChatAdapter(Chat.this, customList);
                                listView.setAdapter(adapter);
                                //=========================
                            } else {
                                new_message.setText(tmp_msg);
                                new_message.setEnabled(true);
                            }
                            break;

                        case 2:
                            new SMS_AsyncTask(Chat.this, tmp_msg, address).execute();
                            break;
                        case 0:
                            new SMS_AsyncTask(Chat.this, tmp_msg, "").execute();
                            break;

                    }


                }
            }
        });


    }

    class LoadSms extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tmpList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            try {
                Uri uriInbox = Uri.parse("content://sms/inbox");
                Cursor inbox = getContentResolver().query(uriInbox, null, "thread_id=" + thread_id_main, null, null);
                Uri uriSent = Uri.parse("content://sms/sent");
                Cursor sent = getContentResolver().query(uriSent, null, "thread_id=" + thread_id_main, null, null);
                Cursor c = new MergeCursor(new Cursor[]{inbox, sent}); // Attaching inbox and sent sms


                if (c.moveToFirst()) {
                    for (int i = 0; i < c.getCount(); i++) {
                        String phone = "";
                        String _id = c.getString(c.getColumnIndexOrThrow("_id"));
                        String thread_id = c.getString(c.getColumnIndexOrThrow("thread_id"));
                        String msg = c.getString(c.getColumnIndexOrThrow("body"));
                        String type = c.getString(c.getColumnIndexOrThrow("type"));
                        String timestamp = c.getString(c.getColumnIndexOrThrow("date"));
                        String read = c.getString(c.getColumnIndexOrThrow("read"));
                        phone = c.getString(c.getColumnIndexOrThrow("address"));


                        //this method used when the sms seen
                        seenSms(read);

                        tmpList.add(Function.mappingInbox(_id, thread_id, name, phone, msg, type, timestamp, Function.converToTime(timestamp), read));
                        c.moveToNext();
                    }
                }
                c.close();

            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Collections.sort(tmpList, new MapComparator(Function.KEY_TIMESTAMP, "asc"));

            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            if (!tmpList.equals(smsList)) {
                smsList.clear();
                smsList.addAll(tmpList);
                adapter = new ChatAdapter(Chat.this, smsList);
                listView.setAdapter(adapter);

            }

        }
    }


    public void startLoadingSms() {
        final Runnable r = new Runnable() {
            public void run() {

                loadsmsTask = new LoadSms();
                loadsmsTask.execute();

                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(r, 0);
    }

    //this method used when the sms seen
    private boolean seenSms(String x) {

        //mines a message of icon notification counter that be seen
        mines();

        if (x.equals("0")) {
            try {

                ContentValues values = new ContentValues();
                values.put("read", "1");
                Uri uriInbox = Uri.parse("content://sms/inbox");
                getContentResolver().update(uriInbox, values, "thread_id=?", new String[]{String.valueOf(thread_id_main)});

            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else
            return false;
    }

    //get data by intent to process and display on actionbar
    private void IntentProcessing() {

        try {
            Intent intent = getIntent();

            //when one sms has been sent
            if (!intent.getStringExtra("thread_id").isEmpty() && !intent.getStringExtra("thread_id").equals("receive")) {
                sendType = 1;
                name = intent.getStringExtra("name");
                address = intent.getStringExtra("address");
                thread_id_main = Integer.parseInt(intent.getStringExtra("thread_id"));
                contactAddress.setText(getContactbyPhoneNumber(G.context, address));
                startLoadingSms();

            }
            //this mean get only address when sms received
            else if (intent.getStringExtra("thread_id").equals("receive")) {
                Uri uriInbox = Uri.parse("content://sms");
                String[] projection = new String[]{"_id", "address", "person", "body", "date", "type", "thread_id"};
                Cursor c = G.context.getContentResolver().query(uriInbox, projection, "address like ?", new String[]{intent.getStringExtra("address").replace(" ", "")}, null);
                c.moveToFirst();
                name = intent.getStringExtra("name");
                address = intent.getStringExtra("address");
                thread_id_main = Integer.valueOf(c.getString(c.getColumnIndexOrThrow("thread_id")));
                contactAddress.setText(getContactbyPhoneNumber(G.context, address));
                startLoadingSms();
            }
            //when sms send to more than one contact
            else {
                ArrayList<String> getContactInfo;
                getContactInfo = intent.getStringArrayListExtra("multiContactOrNewMessage");

                if (getContactInfo.size() == 1) {
                    sendType = 2;
                    address = getContactInfo.get(0);
                    contactAddress.setText(getContactbyPhoneNumber(G.context, getContactInfo.get(0)));
                }
                if (getContactInfo.size() == 2) {
                    contactAddress.setText(getContactbyPhoneNumber(G.context, getContactInfo.get(0)) + " و " + getContactbyPhoneNumber(G.context, getContactInfo.get(1)));
                }
                if (getContactInfo.size() > 2) {
                    contactAddress.setText(getContactbyPhoneNumber(G.context, getContactInfo.get(0)) + " و " + getContactbyPhoneNumber(G.context, getContactInfo.get(1)) + " و " + (getContactInfo.size() - 2) + " نفر دیگر ");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}


class ChatAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;

    public ChatAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data = d;
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ChatViewHolder holder = null;
        if (convertView == null) {
            holder = new ChatViewHolder();
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.chat_item, parent, false);


            holder.txtMsgYou = (TextView) convertView.findViewById(R.id.txtMsgYou);
            holder.lblMsgYou = (TextView) convertView.findViewById(R.id.lblMsgYou);
            holder.timeMsgYou = (TextView) convertView.findViewById(R.id.timeMsgYou);
            holder.lblMsgFrom = (TextView) convertView.findViewById(R.id.lblMsgFrom);
            holder.timeMsgFrom = (TextView) convertView.findViewById(R.id.timeMsgFrom);
            holder.txtMsgFrom = (TextView) convertView.findViewById(R.id.txtMsgFrom);
            holder.msgFrom = (LinearLayout) convertView.findViewById(R.id.msgFrom);
            holder.msgYou = (LinearLayout) convertView.findViewById(R.id.msgYou);

            convertView.setTag(holder);
        } else {
            holder = (ChatViewHolder) convertView.getTag();
        }
        holder.txtMsgYou.setId(position);
        holder.lblMsgYou.setId(position);
        holder.timeMsgYou.setId(position);
        holder.lblMsgFrom.setId(position);
        holder.timeMsgFrom.setId(position);
        holder.txtMsgFrom.setId(position);
        holder.msgFrom.setId(position);
        holder.msgYou.setId(position);

        HashMap<String, String> song = new HashMap<String, String>();
        song = data.get(position);
        try {


            if (song.get(Function.KEY_TYPE).contentEquals("1")) {
                holder.lblMsgFrom.setText(song.get(Function.KEY_NAME));
                holder.txtMsgFrom.setText(song.get(Function.KEY_MSG));
                holder.timeMsgFrom.setText(song.get(Function.KEY_TIME));
                holder.msgFrom.setVisibility(View.VISIBLE);
                holder.msgYou.setVisibility(View.GONE);
            } else {
                holder.lblMsgYou.setText("You");
                holder.txtMsgYou.setText(song.get(Function.KEY_MSG));
                holder.timeMsgYou.setText(song.get(Function.KEY_TIME));
                holder.msgFrom.setVisibility(View.GONE);
                holder.msgYou.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
        }
        return convertView;
    }
}


class ChatViewHolder {
    LinearLayout msgFrom, msgYou;
    TextView txtMsgYou, lblMsgYou, timeMsgYou, lblMsgFrom, txtMsgFrom, timeMsgFrom;
}

