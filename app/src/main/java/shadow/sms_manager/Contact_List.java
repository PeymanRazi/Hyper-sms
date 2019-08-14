package shadow.sms_manager;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.database.SQLException;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static shadow.sms_manager.MainActivity.array;

public class Contact_List extends AppCompatActivity {

    ListView list;
    Contact_Adapter adapter;
    Button ignore, done;
    ImageView backOfList;
    SearchView search;
    ArrayList<News> arrayForSearch, arr;
    Dialog dialog;
    CardView simpleSms, powerSms;
    int size = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact__list);


        ArrayBackupMethod();
        ListViewMethod(array);
        BackButtonMethod();
        OperationButtonMethod();
        SearchMethod();


    }

    private void ArrayBackupMethod() {

        size = array.size();


    }

    private void BackButtonMethod() {

        backOfList = (ImageView) findViewById(R.id.backOfList);
        backOfList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void OperationButtonMethod() {

        ignore = (Button) findViewById(R.id.ingnor);
        done = (Button) findViewById(R.id.done);


        ignore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                News info;

                for (int i = 0; i <= array.size() - 1; i++) {

                    info = array.get(i);
                    if (info.check) {
                        array.set(i, new News(info.image, info.phone, info.id, info.name, false));

                        adapter.notifyDataSetChanged();
                    }
                }


            }
        });


        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                News info1;
                ArrayList<String> flag = new ArrayList<String>();
                for (int i = 0; i <= array.size() - 1; i++) {

                    info1 = array.get(i);
                    if (info1.check)
                        flag.add(info1.phone);

                }
                if (flag.size() >= 1)
                    DialogSelectSmsType(flag);
                else
                    Toast.makeText(Contact_List.this, "هنوز موردی انتخاب نشده", Toast.LENGTH_SHORT).show();


            }
        });
    }

    private void SearchMethod() {

        search = (SearchView) findViewById(R.id.search);

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                try {
                    arrayForSearch = new ArrayList<>();
                    News info;
                    for (int i = 0; i < size; i++) {

                        info = array.get(i);
                        if (info.name.contains(newText)) {
                            arrayForSearch.add(new News(info.image, info.phone, info.id, info.name, info.check));

                        }
                    }


                    ListViewMethod(arrayForSearch);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                return false;
            }


        });

        search.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {

                ListViewMethod(array);
                return false;
            }
        });
    }

    private void ListViewMethod(final ArrayList<News> arr) {


        this.arr = arr;
        list = (ListView) findViewById(R.id.listView);
        adapter = new Contact_Adapter(this, R.layout.contact_list, arr, true);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                News info = Contact_List.this.arr.get(position);

                try {

                    if (info.check) {
                        Contact_List.this.arr.set(position, new News(info.image, info.phone, info.id, info.name, false));
                        if (arr == arrayForSearch) {

                            News info2 = null;
                            for (int i = 0; i < size; i++) {
                                info2 = array.get(i);
                                if (info2.name.equals(info.name)) {
                                    array.set(i, new News(info.image, info.phone, info.id, info.name, false));
                                }
                            }
                        }
                    } else {
                        Contact_List.this.arr.set(position, new News(info.image, info.phone, info.id, info.name, true));
                        if (arr == arrayForSearch) {

                            News info2 = null;
                            for (int i = 0; i < size; i++) {
                                info2 = array.get(i);
                                if (info2.name.equals(info.name)) {
                                    array.set(i, new News(info.image, info.phone, info.id, info.name, true));
                                }
                            }
                        }

                    }

                    adapter.notifyDataSetChanged();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    private void DialogSelectSmsType(final ArrayList<String> flag) {

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_select_sms_type);
        simpleSms = (CardView) dialog.findViewById(R.id.simpleSms);
        powerSms = (CardView) dialog.findViewById(R.id.powerSms);

        simpleSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                if (flag.size() > 1) {
                    dialog.dismiss();
                    Intent intent = new Intent(Contact_List.this, Chat.class);
                    intent.putExtra("multiContactOrNewMessage", flag);
                    intent.putExtra("thread_id","");
                    startActivity(intent);

                } else {

                    //those lines used for set data for intent to chat activity when one member selected only
                    Uri uriInbox = Uri.parse("content://sms");
                    String[] projection = new String[]{"_id", "address", "person", "body", "date", "type", "thread_id"};
                    Cursor c = G.context.getContentResolver().query(uriInbox, projection, "address like ?", new String[]{flag.get(0).replace(" ", "")}, null);
                    dialog.dismiss();

                    if ((c.moveToFirst())){
                        c.moveToFirst();
                        Intent intent = new Intent(Contact_List.this, Chat.class);
                        intent.putExtra("name", "");
                        intent.putExtra("address", flag.get(0).replace(" ", ""));
                        intent.putExtra("thread_id", c.getString(c.getColumnIndexOrThrow("thread_id")));
                        startActivity(intent);
                    }else {
                        Intent intent = new Intent(Contact_List.this, Chat.class);
                        intent.putExtra("multiContactOrNewMessage", flag);
                        intent.putExtra("thread_id","");
                        startActivity(intent);
                    }
                    c.close();

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });


        powerSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Contact_List.this, SendPowerMessage.class);
                startActivity(intent);
                dialog.dismiss();

            }
        });

        dialog.show();

    }


}
