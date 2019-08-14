package shadow.sms_manager;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by Peyman Razi on 17/04/2019.
 */

public class MyDatabase extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "TextMessages.db";
    public static final int DATABASE_VERSION = 1;


    public MyDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
