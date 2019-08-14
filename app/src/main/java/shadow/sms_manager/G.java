package shadow.sms_manager;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.ListView;

import java.util.ArrayList;

import static android.os.Build.VERSION.SDK;


/**
 * Created by Peyman Razi on 25/02/2019.
 */

public class G extends Application {

    public static  SharedPreferences sharedPreferences;
    public static  SharedPreferences.Editor editor;
    public static Context context;
    public static ArrayList<News> selectedArray;


    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences=getSharedPreferences("newMessageCounter",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        context = getApplicationContext();
        selectedArray=new ArrayList<>();


    }







}
