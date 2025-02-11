package cs4720.virginia.cs.edu.piapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static cs4720.virginia.cs.edu.piapp.FeedReaderContract.FeedEntry.COLUMN_NAME_JSON;
import static cs4720.virginia.cs.edu.piapp.FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE;
import static cs4720.virginia.cs.edu.piapp.FeedReaderContract.FeedEntry.TABLE_NAME;

public class AddLightShow extends Activity implements ColorPicker.OnColorChangedListener {
    private int num_colors = 0;
    ArrayList<Integer> color_list = new ArrayList<Integer>(32);
    Activity activity;
    ColorPicker p;
    boolean add_button_pressed = false;
    Button changedButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_light_show);
        this.activity = this;

        final Button addColor = (Button) findViewById(R.id.btn_add_color);
        p = new ColorPicker(this.activity, AddLightShow.this, Color.WHITE);
        p.getWindow().setLayout(50,50);
        addColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_button_pressed = true;
                p.show();
            }
        });

        Button cancel = (Button) findViewById(R.id.btn_Cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button save = (Button) findViewById(R.id.btn_Save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLightShowName();
            }
        });

    }


    @Override
    public void colorChanged(int color) {
        //Get
        FlowLayout fl = (FlowLayout)this.findViewById(R.id.flowlayout);
        Button addButton = (Button) findViewById(R.id.btn_add_color);
        final Button myButton = new Button(this);
        final int col = color;
        if (add_button_pressed) {
            //Remove 'Add' button from view
            fl.removeView(addButton);

            //Create new color button
            myButton.setText("");
            myButton.setId(num_colors);
            myButton.setBackgroundColor(color);
            myButton.setHeight(90);
            myButton.setWidth(90);
            myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changedButton = myButton;
                    p.show();
                }
            });

            add_button_pressed = false;
            //Add new color button to view
            fl.addView(myButton);

            //Re-add 'Add' button to view
            fl.addView(addButton);

            //Increment color counter
            color_list.add(color);

            num_colors = num_colors+1;
        } else {
            changedButton.setBackgroundColor(color);
            color_list.set(changedButton.getId(),color);
        }
    }

    private void getLightShowName(){
        //Get Light Show Name
        AlertDialog.Builder alert = new AlertDialog.Builder(AddLightShow.this);

        alert.setTitle("Save Light Show");
        alert.setMessage("Enter name:");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                // Save light show to database
                saveLightShow(value);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();

    }

    private ArrayList<Integer> createRepeatingPattern(ArrayList<Integer> list){
        ArrayList<Integer> newList = new ArrayList<Integer>(32);
        while (newList.size() <32) {
            newList.addAll(list);
        }
        while (newList.size() !=32 ){
            newList.remove(newList.size()-1);
        }
        return newList;
    }

    private void saveLightShow(String name){

        //Add to database
        JSONObject main = new JSONObject();
        JSONArray lights = new JSONArray();
        ArrayList<Integer> new_color_list = createRepeatingPattern(color_list);
        int light_id = 1;
        for (Integer c : new_color_list){
            JSONObject lightId = new JSONObject();

            try {
                lightId.put("lightId", light_id);
                lightId.put("red", Color.red(c));
                lightId.put("green", Color.green(c));
                lightId.put("blue", Color.blue(c));
                lightId.put("intensity", 0.60);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            lights.put(lightId);
            light_id ++;
        }


        try {
            main.put("lights", lights);
            main.put("propagate", true);
            main.put("chase", true);
            //StringEntity se = new StringEntity(main.toString());
            addToDB(main.toString(),name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        finish();
    }

    private void addToDB(String json, String name) {
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getBaseContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_JSON, json);
        values.put(COLUMN_NAME_TITLE, name);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

}
