package shadow.sms_manager;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.lifeofcoding.cacheutlislibrary.CacheUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import me.leolin.shortcutbadger.ShortcutBadger;

import static shadow.sms_manager.G.context;
import static shadow.sms_manager.G.editor;
import static shadow.sms_manager.G.sharedPreferences;


public class MainActivity extends AppCompatActivity {



    int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    int img = 0;
    public static ArrayList<News> array;
    static final int REQUEST_PERMISSION_KEY = 1;
    ArrayList<HashMap<String, String>> smsList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> tmpList = new ArrayList<HashMap<String, String>>();
    static MainActivity inst;
    LoadSms loadsmsTask;
    InboxAdapter adapter, tmpadapter;
    LinearLayout searchView;
    ListView listView;
    FloatingActionButton fab_new;
    ProgressBar loader;
    int counter=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





        //for search sms chat in other activity
        searchView = (LinearLayout) findViewById(R.id.searchSms);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SearchChat.class);
                startActivity(intent);
            }
        });

        CacheUtils.configureCache(this);
        listView = (ListView) findViewById(R.id.listView);
        loader = (ProgressBar) findViewById(R.id.loader);
        fab_new = (FloatingActionButton) findViewById(R.id.fab_new);
        listView.setEmptyView(loader);


        //get permissions
        if (Build.VERSION.SDK_INT >= 22) {

            getPermissionsMethod();

        } else {
            //receive contact of system
            PerformOperations();
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_new);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(MainActivity.this, Contact_List.class);

                startActivity(intent);

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        String[] PERMISSIONS = {Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};
        if (!Function.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        } else {
            init();
            loadsmsTask = new LoadSms();
            loadsmsTask.execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //get permissions
    public void getPermissionsMethod() {


        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS,
                    Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        } else {
            //receive contact of system
            PerformOperations();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED) {


                //show all message
                PerformOperations();
                init();
                loadsmsTask = new LoadSms();
                loadsmsTask.execute();


            } else {

                Toast.makeText(this, "برنامه بدون دسترسی اجرا نمی شود!", Toast.LENGTH_SHORT).show();

                MainActivity.this.finish();
            }
        }


    }


    //doing operations after granted permissions
    private void PerformOperations() {


        //give contact of database to display that
        displayContact();

        //set app to default instead of factory app
        setDefaultApp();


    }

    //set app to default instead of factory app
    private void setDefaultApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!Telephony.Sms.getDefaultSmsPackage(getApplicationContext()).equals(getApplicationContext().getPackageName())) {
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                        getApplicationContext().getPackageName());
                startActivity(intent);
            }
        }
    }

    //give contact of database to display that
    private void displayContact() {


        array = new ArrayList<>();
        News info = null;

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        for (phones.moveToFirst(); !phones.isAfterLast(); phones.moveToNext()) {

            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

            //remove duplicate contact
            if (img > 0) {
                if (info.name.equals(name) && info.phone.equals(phoneNumber))
                    continue;

            }
            if (phoneNumber.length() < 11)
                continue;

            array.add(new News(img, phoneNumber, id, name, false));
            info = array.get(img);
            img++;

        }


        phones.close();
    }

    public void init() {
        smsList.clear();
        try {
            tmpList = (ArrayList<HashMap<String, String>>) Function.readCachedFile(MainActivity.this, "smsapp");
            tmpadapter = new InboxAdapter(MainActivity.this, tmpList);
            listView.setAdapter(tmpadapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id) {

                    loadsmsTask.cancel(true);
                    Intent intent = new Intent(MainActivity.this, Chat.class);
                    intent.putExtra("name", tmpList.get(+position).get(Function.KEY_NAME));
                    intent.putExtra("address", tmpList.get(+position).get(Function.KEY_PHONE));
                    intent.putExtra("thread_id", tmpList.get(+position).get(Function.KEY_THREAD_ID));
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
        }

    }

    class LoadSms extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            smsList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            try {
                Uri uriInbox = Uri.parse("content://sms/inbox");
                Cursor inbox = getContentResolver().query(uriInbox, null, "address IS NOT NULL) GROUP BY (thread_id", null, null); // 2nd null = "address IS NOT NULL) GROUP BY (address"

                Uri uriSent = Uri.parse("content://sms/sent");
                Cursor sent = getContentResolver().query(uriSent, null, "address IS NOT NULL) GROUP BY (thread_id", null, null); // 2nd null = "address IS NOT NULL) GROUP BY (address"

                Cursor c = new MergeCursor(new Cursor[]{inbox, sent}); // Attaching inbox and sent sms


                if (c.moveToFirst()) {
                    for (int i = 0; i < c.getCount(); i++) {
                        String name = null;
                        String phone = "";
                        String _id = c.getString(c.getColumnIndexOrThrow("_id"));
                        String thread_id = c.getString(c.getColumnIndexOrThrow("thread_id"));
                        String msg = c.getString(c.getColumnIndexOrThrow("body"));
                        String type = c.getString(c.getColumnIndexOrThrow("type"));
                        String timestamp = c.getString(c.getColumnIndexOrThrow("date"));
                        String read = c.getString(c.getColumnIndexOrThrow("read"));
                        phone = c.getString(c.getColumnIndexOrThrow("address"));

                        //this line counts the sms that did not be seen to display on app icon
                        if (read.equals("0"))
                        {
                            counter++;
                        }

                        name = CacheUtils.readFile(thread_id);
                        if (name == null) {
                            name = Function.getContactbyPhoneNumber(getApplicationContext(), c.getString(c.getColumnIndexOrThrow("address")));
                            CacheUtils.writeFile(thread_id, name);
                        }


                        smsList.add(Function.mappingInbox(_id, thread_id, name, phone, msg, type, timestamp, Function.converToTime(timestamp), read));
                        c.moveToNext();
                    }
                }
                c.close();


            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Collections.sort(smsList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging sms by timestamp decending
            ArrayList<HashMap<String, String>> purified = Function.removeDuplicates(smsList); // Removing duplicates from inbox & sent
            smsList.clear();
            smsList.addAll(purified);

            // Updating cache data
            try {
                Function.createCachedFile(MainActivity.this, "smsapp", smsList);
            } catch (Exception e) {
            }
            // Updating cache data

            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            //add count of message that unread to display on app icon
            IconCounterMessage.add(counter);


            if (!tmpList.equals(smsList)) {
                adapter = new InboxAdapter(MainActivity.this, smsList);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            final int position, long id) {


                        Intent intent = new Intent(MainActivity.this, Chat.class);
                        intent.putExtra("name", smsList.get(+position).get(Function.KEY_NAME));
                        intent.putExtra("address", tmpList.get(+position).get(Function.KEY_PHONE));
                        intent.putExtra("thread_id", smsList.get(+position).get(Function.KEY_THREAD_ID));
                        startActivity(intent);

                    }
                });
            }

        }
    }

}


class InboxAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;

    public InboxAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
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
        InboxViewHolder holder = null;
        if (convertView == null) {
            holder = new InboxViewHolder();
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.conversation_list_item, parent, false);


            holder.sms_number = (TextView) convertView.findViewById(R.id.smsNumber);
            holder.inbox_thumb = (ImageView) convertView.findViewById(R.id.inbox_thumb);
            holder.inbox_user = (TextView) convertView.findViewById(R.id.inbox_user);
            holder.inbox_msg = (TextView) convertView.findViewById(R.id.inbox_msg);
            holder.inbox_date = (TextView) convertView.findViewById(R.id.inbox_date);

            convertView.setTag(holder);
        } else {
            holder = (InboxViewHolder) convertView.getTag();
        }




        holder.sms_number.setId(position);
        holder.inbox_thumb.setId(position);
        holder.inbox_user.setId(position);
        holder.inbox_msg.setId(position);
        holder.inbox_date.setId(position);


        HashMap<String, String> song = new HashMap<String, String>();
        song = data.get(position);
        try {
            holder.inbox_user.setText(song.get(Function.KEY_NAME));
            holder.inbox_msg.setText(song.get(Function.KEY_MSG));
            holder.inbox_date.setText(song.get(Function.KEY_TIME));

            //for detecting sms that wast read
            if (song.get(Function.KEY_READ).equals("0")){
                holder.sms_number.setVisibility(View.VISIBLE);
            }

            String firstLetter = String.valueOf(song.get(Function.KEY_NAME).charAt(0));
            ColorGenerator generator = ColorGenerator.MATERIAL;
            int color = generator.getColor(getItem(position));
            TextDrawable drawable = TextDrawable.builder()
                    .buildRound(firstLetter, color);
            holder.inbox_thumb.setImageDrawable(drawable);
        } catch (Exception e) {
        }
        return convertView;
    }
}


class InboxViewHolder {
    ImageView inbox_thumb;
    TextView inbox_user, inbox_msg, inbox_date, sms_number;

}

class IconCounterMessage{

    //set number of message to icon
    public static void add(int number){

        editor.putInt("Count",number);
        editor.commit();
        ShortcutBadger.applyCount(context,sharedPreferences.getInt("Count",0));

    }

    //add number to icon counter when received new message
    public static void plus(){
        editor.putInt("Count",sharedPreferences.getInt("Count",0)+1);
        editor.commit();
        ShortcutBadger.applyCount(context,sharedPreferences.getInt("Count",0));

    }

    //add number to icon counter when received new message
    public static void mines(){
        editor.putInt("Count",sharedPreferences.getInt("Count",0)-1);
        editor.commit();
        ShortcutBadger.applyCount(context,sharedPreferences.getInt("Count",0));

    }
}








