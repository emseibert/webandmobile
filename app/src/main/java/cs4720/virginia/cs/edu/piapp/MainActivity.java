package cs4720.virginia.cs.edu.piapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hmkcode.http.HttpHandler;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import static cs4720.virginia.cs.edu.piapp.FeedReaderContract.FeedEntry.COLUMN_NAME_JSON;
import static cs4720.virginia.cs.edu.piapp.FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE;
import static cs4720.virginia.cs.edu.piapp.FeedReaderContract.FeedEntry.TABLE_NAME;


public class MainActivity extends Activity  {
    Activity activity;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.activity = this;
        setButtonListeners();
        populateList();
    }

    /*
        Should be used in Kristen's Add Light Show
    private void addToDB(String json, String name) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_JSON, json);
        values.put(COLUMN_NAME_TITLE, name);

        db.insert(TABLE_NAME, null,values);
    }
    */

    private void populateList() {
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getBaseContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String[] projection = {COLUMN_NAME_TITLE, COLUMN_NAME_JSON};
        String sortOrder =COLUMN_NAME_TITLE + " DESC";
        Cursor c = db.query(TABLE_NAME, projection, null, null, null, null, sortOrder);
        final ListView listView = (ListView) findViewById(R.id.list);
        int numOfRows = c.getCount();
        String[] list_values = getNames(numOfRows, c);
        c.close();
        db.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, list_values);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPosition     = position;
                String  itemValue    = (String) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), "Now Showing: " + itemValue, Toast.LENGTH_LONG)
                        .show();
                FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getBaseContext());
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                String[] projection = {COLUMN_NAME_TITLE, COLUMN_NAME_JSON};
                String sortOrder =COLUMN_NAME_TITLE + " DESC";
                Cursor c = db.query(TABLE_NAME, projection, null, null, null, null, sortOrder);
                JSONObject json;
                if (c.getCount() > 0) {
                    c.moveToPosition(position);
                    json = parseJson(c.getString(1));
                } else {
                    json = new JSONObject();
                    try {
                        json.put("test", "one");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                makePostRequest(json);

                c.close();
                db.close();
            }
        });
    }

    public JSONObject parseJson(String json) {
        return new JSONObject();
    }

    public void makePostRequest(JSONObject json) {
        new HttpHandler() {
            @Override
            public HttpUriRequest getHttpRequestMethod() {
                TextView tx = (TextView) findViewById(R.id.textView);
                String url = tx.getText().toString().split("Current Ip Address: ")[1];
                HttpPost p = new HttpPost("http://" + url + "/rpi");
                p.addHeader("Content-type", "application/json");

                try {

                    JSONObject lightId = new JSONObject();
                    lightId.put("lightId", 1);
                    lightId.put("red", Color.red(255));
                    lightId.put("green", Color.green(0));
                    lightId.put("blue", Color.blue(0));
                    lightId.put("intensity", 30);
                    JSONArray lights = new JSONArray();
                    lights.put(lightId);
                    JSONObject main = new JSONObject();
                    main.put("lights", lights);
                    main.put("propagate", true);
                    StringEntity se = new StringEntity(main.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
                    p.setEntity(se);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return p;
            }

            @Override
            public void onResponse(String s) {

            }
        }.execute();

    }

    private String[] getNames(int numOfRows, Cursor c) {
        ArrayList<String> names = new ArrayList<String>();

        if (numOfRows == 0) {
            String[] fake_data = {"Light Show 1", "Light Show 2"};
            return fake_data;
        } else {
            for (int i = 0; i < numOfRows; i++) {
                c.moveToPosition(0);
                names.add(c.getString(0));
            }

            return names.toArray(new String[names.size()]);
        }
    }
//      Possibly use something like this when deleting a light show
//      will clear database if necessary
//
//    private void clearDB() {
//        db.delete(TABLE_NAME, null, null);
//    }

    private void setButtonListeners() {
        final Button updateIpButton = (Button) findViewById(R.id.popupbutton);
        final Button addNewLightShow = (Button) findViewById(R.id.button_add_new_light);

        updateIpButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                openIpDialog(v);
            }
        });

        addNewLightShow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addLightShow(v);
            }
        });
    }

    private void addLightShow(View v) {
        //add connection to kristen's add light show activity
    }


    private void openIpDialog(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        final EditText input = new EditText(this);
        alertDialogBuilder.setView(input);
        alertDialogBuilder.setTitle("Update IP Address");

        alertDialogBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString().trim();
                TextView tx = (TextView) findViewById(R.id.textView);
                tx.setText("Current IP Address: " + value);
                Toast.makeText(getApplicationContext(), value, Toast.LENGTH_SHORT).show();
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }
}