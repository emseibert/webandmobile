package cs4720.virginia.cs.edu.piapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

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
        if (isConnected()) {
//            TextView tx = (TextView) findViewById(R.id.textView2);
//            tx.setBackgroundColor(Color.GREEN);
        }
        populateList();
    }


        //Should be used in Kristen's Add Light Show
    private void addToDB(String json, String name) {
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getBaseContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_JSON, json);
        values.put(COLUMN_NAME_TITLE, name);

        db.insert(TABLE_NAME, null,values);
        db.close();
    }


    private void populateList() {
        //addToDB("hi there", "testing name");
        //clearDB(); //if you remove this method, 'testing name' will show instead of fake data
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
                String json;
                if (c.getCount() > 0) {
                    c.moveToPosition(position);
                    json = c.getString(1);
                } else {
                    json = "";
//                    try {
//                        //json.put("test", "one");
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                }

                makePostRequest(json);

                c.close();
                db.close();
            }
        });
    }

    public JSONObject parseJson(String json) {

        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public boolean isConnected(){
    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isConnected())
        return true;
    else
        return false;
    }

    public void makePostRequest(final String json) {
        new HttpHandler() {
            @Override
            public HttpUriRequest getHttpRequestMethod() {
                TextView tx = (TextView) findViewById(R.id.textView);
                String url = tx.getText().toString().split("Current Ip Address: ")[1];
                //HttpPost p = new HttpPost("http://" + url + "/rpi");
                HttpPost p = new HttpPost("http://requestb.in/1jfmjcw1");
                p.addHeader("Content-type", "application/json");


                try {
                    StringEntity se = new StringEntity(json);
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
                    p.setEntity(se);
                } catch (UnsupportedEncodingException e) {
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
                c.moveToPosition(i);
                names.add(c.getString(0));
            }

            return names.toArray(new String[names.size()]);
        }
    }
      //Possibly use something like this when deleting a light show
     // will clear database if necessary

    private void clearDB() {
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getBaseContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

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
        Intent someName = new Intent(this, AddLightShow.class);
        startActivity(someName);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        //Update List from Database
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
                int itemPosition = position;
                String itemValue = (String) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), "Now Showing: " + itemValue, Toast.LENGTH_LONG)
                        .show();
                FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getBaseContext());
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                String[] projection = {COLUMN_NAME_TITLE, COLUMN_NAME_JSON};
                String sortOrder =COLUMN_NAME_TITLE + " DESC";
                Cursor c = db.query(TABLE_NAME, projection, null, null, null, null, sortOrder);
                String json;
                if (c.getCount() > 0) {
                    c.moveToPosition(position);
                    json = c.getString(1);
                } else {
                    json = "";
//                    try {
//                        json.put("test", "one");
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                }

                makePostRequest(json);

                c.close();
                db.close();
            }
        });


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