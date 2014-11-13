package cs4720.virginia.cs.edu.piapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hmkcode.http.HttpHandler;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

import static cs4720.virginia.cs.edu.piapp.FeedReaderContract.FeedEntry.COLUMN_NAME_JSON;
import static cs4720.virginia.cs.edu.piapp.FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE;
import static cs4720.virginia.cs.edu.piapp.FeedReaderContract.FeedEntry.TABLE_NAME;


public class MainActivity extends Activity {
    Activity activity;
    private ShakeDetector mShakeDetector;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    ImageView current_img;

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
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake() {
                FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getBaseContext());
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                String[] projection = {COLUMN_NAME_TITLE, COLUMN_NAME_JSON};
                String sortOrder =COLUMN_NAME_TITLE + " DESC";
                Cursor c = db.query(TABLE_NAME, projection, null, null, null, null, sortOrder);
                int numOfRows = c.getCount();

                if (numOfRows > 0) {
                    Random rand = new Random();
                    int randomListPosition = rand.nextInt(numOfRows);
                    c.moveToPosition(randomListPosition);

                    String json = c.getString(1);
                    makePostRequest(json);
                    Toast.makeText(getApplicationContext(), "Now Showing: " + c.getString(0), Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(), "Now", Toast.LENGTH_LONG).show();
                }
                c.close();
                db.close();
            }
        });

    }


    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    private void populateList() {

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
        LightShowArrayAdapter adapter = new LightShowArrayAdapter(this, list_values);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
                }

                makePostRequest(json);
                //view.findViewById()
                if (current_img != null) {
                    current_img.setVisibility(View.INVISIBLE);
                }

                current_img = (ImageView) view.findViewById(R.id.light_bulb);
                current_img.setVisibility(View.VISIBLE);

                c.close();
                db.close();
            }
        });
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
                String url = tx.getText().toString().split("Current IP Address: ")[0];
                //HttpPost p = new HttpPost("http://" + url + "/rpi");
                HttpPost p = new HttpPost("http://" + url + "/rpi");
                //HttpPost p = new HttpPost("http://requestb.in/yxficvyx");

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
//                ImageView img = (ImageView) findViewById(R.id.light_bulb);
//                img.setVisibility(View.VISIBLE);
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

//    private void clearDB() {
//        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getBaseContext());
//        SQLiteDatabase db = mDbHelper.getWritableDatabase();
//        db.delete(TABLE_NAME, null, null);
//        db.close();
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
        Intent someName = new Intent(this, AddLightShow.class);
        startActivity(someName);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        populateList();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
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
                String url = tx.getText().toString().replace("Current IP Address: ","");
                url = "http://" + url + "/rpi";
                Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT).show();
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
            final TextView textView = (TextView) rowView.findViewById(R.id.label);
            ImageButton btn_cancel = (ImageButton) rowView.findViewById(R.id.btn_cancel);
            textView.setText(values[position]);
            final String name = values[position];
            // change the icon for Windows and iPhone
            String s = values[position];
            btn_cancel.setFocusable(false);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getBaseContext());
                    SQLiteDatabase db = mDbHelper.getWritableDatabase();
                    String[] projection = {COLUMN_NAME_TITLE, COLUMN_NAME_JSON};
                    String sortOrder =COLUMN_NAME_TITLE + " DESC";

                    // Define 'where' part of query.
                    String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " LIKE ?";
                    // Specify arguments in placeholder order.
                    String val = textView.getText().toString();
                    String[] selectionArgs = { String.valueOf(val) };

                    db.delete(TABLE_NAME, selection, selectionArgs);

                    db.close();

                    populateList();
                }
            });
            return rowView;
        }
    }
}
