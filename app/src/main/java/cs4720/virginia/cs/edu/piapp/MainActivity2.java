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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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


public class MainActivity2 extends Activity {
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
                    if (current_img != null) {
                        current_img.setVisibility(View.INVISIBLE);
                    }

                    final ListView listView = (ListView) findViewById(R.id.list);

                    current_img = (ImageView) listView.getChildAt(randomListPosition).findViewById(R.id.light_bulb);
                    current_img.setVisibility(View.VISIBLE);

                } else {
                    Toast.makeText(getApplicationContext(), "Add Light Shows!", Toast.LENGTH_LONG).show();
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

    public void makePostRequest(final String json) {
        new HttpHandler() {
            @Override
            public HttpUriRequest getHttpRequestMethod() {
                TextView tx = (TextView) findViewById(R.id.textView);
                String url = tx.getText().toString().trim();
                HttpPost p = new HttpPost("http://" + url + "/rpi");
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
            String[] fake_data = {};
            return fake_data;
        } else {
            for (int i = 0; i < numOfRows; i++) {
                c.moveToPosition(i);
                names.add(c.getString(0));
            }

            return names.toArray(new String[names.size()]);
        }
    }

    private void setButtonListeners() {
        final Button addNewLightShow = (Button) findViewById(R.id.button_add_new_light);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    private void openIpDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity2.this);
        final EditText input = new EditText(this);
        alertDialogBuilder.setView(input);
        TextView tx = (TextView) findViewById(R.id.textView);
        alertDialogBuilder.setTitle("IP Address");
        alertDialogBuilder.setMessage("Current IP Address: " + tx.getText() + "\n \nNew IP Address:");

        alertDialogBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString().trim();
                TextView tx = (TextView) findViewById(R.id.textView);
                tx.setText(value);
                String url = tx.getText().toString().trim();
                url = "You updated your ip address to: \n" + url;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_settings).setVisible(true);
        menu.findItem(R.id.action_settings).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                openIpDialog();
                return true;
            }
        });
        return true;
    }

}