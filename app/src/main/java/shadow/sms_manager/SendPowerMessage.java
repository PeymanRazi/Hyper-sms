package shadow.sms_manager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;


public class SendPowerMessage extends AppCompatActivity implements View.OnClickListener {

    public static EditText mainPassage;
    ArrayList<String> allTexts, codeText;
    SQLiteDatabase database;
    MyDatabase mydb;
    Cursor cu = null;
    String forCompare = "";
    Button sendButton;
    ImageView add;
    AutoCompleteTextView autoCompleteTextView;
    Handler h;
    Thread t;




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

            onBackPressed();

        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send__message);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        sendButton = (Button) findViewById(R.id.sendButton);
        mainPassage = (EditText) findViewById(R.id.textFinder);

        sendButton.setOnClickListener(this);


        //when edit text not empty the send button will show
        showSendButton();


    }

    @Override
    protected void onStart() {
        super.onStart();

        add = (ImageView) findViewById(R.id.add);
        allTexts = new ArrayList<>();
        codeText = new ArrayList<>();

        //for detect by other app
        codeText.add("*#");

        for (int i = 0; i < 100; i++) {
            codeText.add("");
        }
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);


        h = new Handler();
        t = new Thread(new Runnable() {
            @Override
            public void run() {

                h.post(new Runnable() {
                    @Override
                    public void run() {

                        ShowSampleWords();


                    }
                });
            }
        });
        t.start();


    }

    @Override
    public void onClick(View v) {

        new CompressAsyncTask(mainPassage.getText().toString(), this).execute();


    }

    private void ShowSampleWords() {


        mydb = new MyDatabase(G.context);
        database = mydb.getReadableDatabase();

        autoCompleteTextView.setThreshold(2);
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                allTexts = new ArrayList<String>();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (autoCompleteTextView.getText().toString().length() >= 1) {
                    cu = database.rawQuery("SELECT * FROM Words WHERE name like'" + s.toString() + "%'", null);
                    while (cu.moveToNext()) {

                        allTexts.add(cu.getString(2).replace("\n", ""));
                    }
                    final ArrayAdapter adapter = new ArrayAdapter
                            (G.context, android.R.layout.select_dialog_item, allTexts);
                    autoCompleteTextView.setAdapter(adapter);
                }
            }
        });

        //insert to main edit box of auto complete
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mainPassage.append(autoCompleteTextView.getText() + " ");
                autoCompleteTextView.getText().clear();

            }
        });

    }

    //when edit text not empty the send button and add button will show
    private void showSendButton() {
        mainPassage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!mainPassage.getText().toString().isEmpty()) {
                    sendButton.setVisibility(View.VISIBLE);


                } else {
                    sendButton.setVisibility(View.INVISIBLE);


                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

}



