package shadow.sms_manager;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.lifeofcoding.cacheutlislibrary.CacheUtils;

import java.util.ArrayList;
import java.util.HashMap;


public class SearchChat extends AppCompatActivity implements SearchView.OnQueryTextListener,SearchView.OnCloseListener {

    SearchView searchView;
    ImageView backOfList2;
    ListView listView;
    ArrayList<HashMap<String, String>> smsList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> tmpList = new ArrayList<HashMap<String, String>>();
    InboxAdapter adapter, tmpadapter;
    LoadSms loadsmsTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_chat);

        backOfList2=(ImageView)findViewById(R.id.backOfList2);
        backOfList2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        listView = (ListView) findViewById(R.id.listView2);

        searchView = (SearchView) findViewById(R.id.search2);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);



    }

    /**
     * Called when the user submits the query. This could be due to a key press on the
     * keyboard or due to pressing a submit button.
     * The listener can override the standard behavior by returning true
     * to indicate that it has handled the submit request. Otherwise return false to
     * let the SearchView handle the submission by launching any associated intent.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {


        return false;
    }

    /**
     * Called when the query text is changed by the user.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {

        if (!newText.isEmpty()){

            init();
            loadsmsTask = new SearchChat.LoadSms(newText);
            loadsmsTask.execute();
        }

        return false;
    }

    /**
     * The user is attempting to close the SearchView.
     *
     * @return true if the listener wants to override the default behavior of clearing the
     * text field and dismissing it, false otherwise.
     */
    @Override
    public boolean onClose() {

        if (!smsList.isEmpty()){

            smsList.clear();
            adapter.notifyDataSetChanged();
        }
        return false;
    }





    public void init() {
        smsList.clear();
        try {
            tmpList = (ArrayList<HashMap<String, String>>) Function.readCachedFile(G.context, "smsapp");
            tmpadapter = new InboxAdapter(SearchChat.this, tmpList);
            listView.setAdapter(tmpadapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id) {

                    loadsmsTask.cancel(true);
                    Intent intent = new Intent(SearchChat.this, Chat.class);
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
        String find;

        public LoadSms(String find) {
            this.find = find;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            smsList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            try {
                Uri uriInbox = Uri.parse("content://sms/inbox");

                Cursor inbox = getContentResolver().query(uriInbox, null, "body like ? OR address like ?", new String[]{"%"+find+"%", "%"+find+"%"}, null); // 2nd null = "address IS NOT NULL) GROUP BY (address"
                Uri uriSent = Uri.parse("content://sms/sent");
                Cursor sent = getContentResolver().query(uriSent, null, "body like ? OR address like ?", new String[]{"%"+find+"%", "%"+find+"%"}, null); // 2nd null = "address IS NOT NULL) GROUP BY (address"
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


                        name = CacheUtils.readFile(thread_id);
                        if (name == null) {
                            name = Function.getContactbyPhoneNumber(getApplicationContext(), c.getString(c.getColumnIndexOrThrow("address")));
                            CacheUtils.writeFile(thread_id, name);
                        }


                        smsList.add(Function.mappingInbox(_id, thread_id, name, phone, msg, type, timestamp, Function.converToTime(timestamp),read));
                        c.moveToNext();
                    }
                }
                c.close();

            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {


            if (!tmpList.equals(smsList)) {
                adapter = new InboxAdapter(SearchChat.this, smsList);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            final int position, long id) {


                        Intent intent = new Intent(SearchChat.this, Chat.class);
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


class InboxAdapter2 extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;

    public InboxAdapter2(Activity a, ArrayList<HashMap<String, String>> d) {
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

            holder.inbox_thumb = (ImageView) convertView.findViewById(R.id.inbox_thumb);
            holder.inbox_user = (TextView) convertView.findViewById(R.id.inbox_user);
            holder.inbox_msg = (TextView) convertView.findViewById(R.id.inbox_msg);
            holder.inbox_date = (TextView) convertView.findViewById(R.id.inbox_date);

            convertView.setTag(holder);
        } else {
            holder = (InboxViewHolder) convertView.getTag();
        }
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

class InboxViewHolder2 {
    ImageView inbox_thumb;
    TextView inbox_user, inbox_msg, inbox_date;

}