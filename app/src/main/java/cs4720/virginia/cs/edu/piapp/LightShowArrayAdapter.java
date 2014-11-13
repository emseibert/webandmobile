package cs4720.virginia.cs.edu.piapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import java.util.List;
import java.util.ArrayList;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
/**
 * Created by kristen on 11/12/14.
 */
public class LightShowArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    public LightShowArrayAdapter(Context context, String[] values) {
        super(context, R.layout.list_view, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_view, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        ImageButton btn_cancel = (ImageButton) rowView.findViewById(R.id.btn_cancel);
        textView.setText(values[position]);
        // change the icon for Windows and iPhone
        String s = values[position];
        btn_cancel.setFocusable(false);

        return rowView;
    }
}