package shadow.sms_manager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Peyman Razi on 11/06/2019.
 */

public class CompressAsyncTask extends AsyncTask {

    ProgressDialog progressDialog;
    String word = "", sentence = "", codes = "", passage, contAINER = "*#";
    ;
    ArrayList<String> arrayList2, notConverted;
    SQLiteDatabase database;
    MyDatabase mydb;
    Cursor cu = null;
    Activity activity;
    Character symbolChar;
    AlertDialog.Builder alert;
    public static String submitInbox;

    public CompressAsyncTask(String passage, Activity activity) {

        this.passage = passage;
        this.activity = activity;
        submitInbox = passage;
        mydb = new MyDatabase(G.context);
        database = mydb.getReadableDatabase();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();


        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("در حال پردازش متن");
        progressDialog.setTitle("لطفا کمی صبرکنید");
        progressDialog.show();


    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        alert = new AlertDialog.Builder(activity);
        progressDialog.dismiss();


        for (int i = 0; i < arrayList2.size(); i++) {
            contAINER += arrayList2.get(i);
        }

        if (contAINER.length() > passage.length()) {

            alert.setMessage("متن کپی شده مجاز نیست لطفا تایپ کنید!");
            alert.setPositiveButton("باشه", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        } else {

            String editable="";
            for (int i=0;i<notConverted.size();i++){
                editable+=notConverted.get(i)+" ";
            }
            alert.setMessage("برای بهینه سازی بیشتر از کلمات موجود در پیشنهادات استفاده کنید و یا از نگارش موارد غیر ضروری صرف نظر کنید."+"\n"+editable);
            alert.setTitle(passage.length() + "حرف به " + contAINER.length() + "حرف بهینه شد ");
            alert.setPositiveButton("ارسال", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new SMS_AsyncTask(activity, contAINER, "").execute();

                }
            });
        }

        alert.create();
        alert.show();

    }

    @Override
    protected Object doInBackground(Object[] params) {

        arrayList2 = new ArrayList<>();
        notConverted=new ArrayList<>();
        //insert space char to do ring after the passage amount has been end because condition is before of add lines
//        passage += " ";

        try {
            for (int i = 0; i < passage.length(); i++) {

                if (passage.charAt(i) == '\n' || passage.charAt(i) == ' ' || passage.charAt(i) == '؟' || passage.charAt(i) == '!' || passage.charAt(i) == '،' || passage.charAt(i) == ':' ||
                        passage.charAt(i) == '؛' || passage.charAt(i) == '.' || passage.charAt(i) == '"' || passage.charAt(i) == '\'' || passage.charAt(i) == '\\' || passage.charAt(i) == '/' || passage.charAt(i) == ')' || passage.charAt(i) == '(' || passage.charAt(i) == ',' || passage.charAt(i) == '\u200C') {


                    symbolChar = passage.charAt(i);
                    FindSandW(word, sentence, i);

                    //break ring before add space char that insert to passage manually
                    if (i == passage.length() - 1)
                        break;
                }


                word += passage.charAt(i);
                sentence += passage.charAt(i);

                if (word.equals("\n") || word.equals(" ") || word.equals("؟") || word.equals("!") || word.equals("،") || word.equals(":") ||
                        word.equals("؛") || word.equals(".") || word.equals("\"") || word.equals("'") || word.equals("\\") || word.equals("/") || word.equals(")") || word.equals("(") || word.equals(",") || word.equals("\u200C")) {

                    word = "";
                }
                if (sentence.equals("\n") || sentence.equals(" ") || sentence.equals("؟") || sentence.equals("!") || sentence.equals("،") || sentence.equals(":") ||
                        sentence.equals("؛") || sentence.equals(".") || sentence.equals("\"") || sentence.equals("'") || sentence.equals("\\") || sentence.equals("/") || sentence.equals(")") || sentence.equals("(") || sentence.equals(",") || sentence.equals("\u200C")) {

                    sentence = "";
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    public void FindSandW(String word, String sentence, int endFlag) {


        try {
            //add sentence code to array if available that in db
            cu = database.rawQuery("SELECT * FROM Words WHERE name='" + sentence + "\n" + "'", null);

            if (cu.moveToFirst()) {

                if (endFlag == passage.length() - 1)
                    //put sentence code to array if not exist words or sentence after this sentence
//                    arrayList2.add(cu.getString(1));
                    Symbol(cu.getString(1), symbolChar);

                else
                    codes = cu.getString(1) + Symbol2(symbolChar);
                this.word = "";

            } else {

                //put sentence code to array
                if (!codes.isEmpty()) {
//                    arrayList2.add(codes);
                    Symbol(codes, symbolChar);
                    codes = "";
                }

                FindBaseWord(word);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void FindBaseWord(String text) {

        try {


            //add sentence code to array if available that in db
            cu = database.rawQuery("SELECT * FROM Words WHERE name='" + text + "\n" + "'", null);

            if (cu.moveToFirst())
                //put word code to array
                //arrayList2.add(cu.getString(1));
                Symbol(cu.getString(1), symbolChar);
            else {
                String txt = text;
                if (text.charAt((text.length() - 1)) == 'ه') {

                    txt = txt.substring(0, text.length() - 1);
                    cu = database.rawQuery("SELECT * FROM Words WHERE name='" + txt + "\n" + "'", null);
                    if (cu.moveToFirst())
                        //put word code to array
//                        arrayList2.add(cu.getString(1) + "ঊ");
                        Symbol(cu.getString(1) + "ঊ", symbolChar);
                    else{
                        //put the word that not find in db
                        arrayList2.add("*" + word + "*");
                        notConverted.add(word);
                    }

                } else if (text.charAt((text.length() - 1)) == 'ی') {

                    txt = txt.substring(0, text.length() - 1);
                    cu = database.rawQuery("SELECT * FROM Words WHERE name='" + txt + "\n" + "'", null);
                    if (cu.moveToFirst())
                        //put word code to array
//                        arrayList2.add(cu.getString(1) + "ঔ");
                        Symbol(cu.getString(1) + "ঔ", symbolChar);
                    else{
                        //put the word that not find in db
                        arrayList2.add("*" + word + "*");
                        notConverted.add(word);
                    }
                } else if (text.charAt((text.length() - 1)) == 'م') {

                    txt = txt.substring(0, text.length() - 1);
                    cu = database.rawQuery("SELECT * FROM Words WHERE name='" + txt + "\n" + "'", null);
                    if (cu.moveToFirst())
                        //put word code to array
//                        arrayList2.add(cu.getString(1) + "ঐ");
                        Symbol(cu.getString(1) + "ঐ", symbolChar);
                    else{
                        //put the word that not find in db
                        arrayList2.add("*" + word + "*");
                        notConverted.add(word);
                    }
                } else if (text.charAt((text.length() - 1)) == 'ی' && text.charAt((text.length() - 2)) == 'ا') {

                    txt = txt.substring(0, text.length() - 2);
                    cu = database.rawQuery("SELECT * FROM Words WHERE name='" + txt + "\n" + "'", null);
                    if (cu.moveToFirst())
                        //put word code to array
//                        arrayList2.add(cu.getString(1) + "আ");
                        Symbol(cu.getString(1) + "আ", symbolChar);
                    else{
                        //put the word that not find in db
                        arrayList2.add("*" + word + "*");
                        notConverted.add(word);
                    }
                } else if (text.charAt((text.length() - 1)) == 'ت' && text.charAt((text.length() - 2)) == 'س') {

                    txt = txt.substring(0, text.length() - 2);
                    cu = database.rawQuery("SELECT * FROM Words WHERE name='" + txt + "\n" + "'", null);
                    if (cu.moveToFirst())
                        //put word code to array
//                        arrayList2.add(cu.getString(1) + "ঈ");
                        Symbol(cu.getString(1) + "ঈ", symbolChar);
                    else{
                        //put the word that not find in db
                        arrayList2.add("*" + word + "*");
                        notConverted.add(word);
                    }
                } else if (text.charAt((text.length() - 1)) == 'ن' && text.charAt((text.length() - 2)) == 'ا') {

                    txt = txt.substring(0, text.length() - 2);
                    cu = database.rawQuery("SELECT * FROM Words WHERE name='" + txt + "\n" + "'", null);
                    if (cu.moveToFirst())
                        //put word code to array
//                        arrayList2.add(cu.getString(1) + "ঙ");
                        Symbol(cu.getString(1) + "ঙ", symbolChar);
                    else{
                        //put the word that not find in db
                        arrayList2.add("*" + word + "*");
                        notConverted.add(word);
                    }
                } else if (text.charAt((text.length() - 1)) == 'ا' && text.charAt((text.length() - 2)) == 'ه') {

                    txt = txt.substring(0, text.length() - 2);
                    cu = database.rawQuery("SELECT * FROM Words WHERE name='" + txt + "\n" + "'", null);
                    if (cu.moveToFirst())
                        //put word code to array
//                        arrayList2.add(cu.getString(1) + "ভ");
                        Symbol(cu.getString(1) + "ভ", symbolChar);
                    else{
                        //put the word that not find in db
                        arrayList2.add("*" + word + "*");
                        notConverted.add(word);
                    }
                } else {
                    //put the word that not find in db
                    arrayList2.add("*" + word + "*");
                    notConverted.add(word);
                }
            }

            this.sentence = "";
            this.word = "";

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void Symbol(String text, Character symbol) {

        try {
            switch (symbol) {

                case '؟':
                    arrayList2.add(text + "ঘ");
                    break;
                case '!':
                    arrayList2.add(text + "ধ");
                    break;
                case '،':
                    arrayList2.add(text + "ঝ");
                    break;
                case '؛':
                    arrayList2.add(text + "এ");
                    break;
                case ':':
                    arrayList2.add(text + "ও");
                    break;
                case '.':
                    arrayList2.add(text + "অ");
                    break;
                case '/':
                    arrayList2.add(text + "উ");
                    break;
                case '\\':
                    arrayList2.add(text + "ফ");
                    break;
                default:
                    arrayList2.add(text);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String Symbol2(Character symbol) {

        String codeSymbol;

        switch (symbol) {

            case '؟':
                codeSymbol = "ঘ";
                break;
            case '!':
                codeSymbol = "ধ";
                break;
            case '،':
                codeSymbol = "ঝ";
                break;
            case '؛':
                codeSymbol = "এ";
                break;
            case ':':
                codeSymbol = "ও";
                break;
            case '.':
                codeSymbol = "অ";
                break;
            case '/':
                codeSymbol = "উ";
                break;
            case '\\':
                codeSymbol = "ফ";
                break;
            default:
                codeSymbol = "";

        }
        return codeSymbol;

    }


}
