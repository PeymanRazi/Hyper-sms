package shadow.sms_manager;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peyman Razi on 24/02/2019.
 */

public class Contact_Adapter extends ArrayAdapter {

    public Activity context;
    public ArrayList<News> arrayList;
    public int addressId;

    ImageView checkBox;
    TextView textView;
    LinearLayout linearLayout;
    News info;

    boolean selectable;


    public Contact_Adapter(@NonNull Activity context, @LayoutRes int resource, ArrayList<News> objects, boolean selectable) {
        super(context, resource, objects);

        this.context = context;
        this.addressId = resource;
        this.arrayList = objects;
        this.selectable = selectable;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        View view = convertView;
        view = this.context.getLayoutInflater().inflate(this.addressId, null);


        textView = (TextView) view.findViewById(R.id.name);
        checkBox = (ImageView) view.findViewById(R.id.contactPickerCheck);
        linearLayout = (LinearLayout) view.findViewById(R.id.item);
        info = arrayList.get(position);
        textView.setText(info.name + "\n" + info.phone);


        if (info.check)
        {
            checkBox.setVisibility(View.VISIBLE);
        }

        return view;


    }


}
