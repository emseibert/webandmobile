package cs4720.virginia.cs.edu.piapp;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.*;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hmkcode.http.HttpHandler;
import android.widget.TextView;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class MainActivity extends Activity implements ColorPicker.OnColorChangedListener {
    ColorPicker p;
    int c;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.activity = this;
        final Button b = (Button) findViewById(R.id.button);
        p = new ColorPicker(activity, MainActivity.this, Color.WHITE);
        if (isConnected()) {
            TextView tx = (TextView) findViewById(R.id.textView2);
            tx.setText("You are connected");
            tx.setBackgroundColor(Color.GREEN);
        }
        else {
            TextView tx = (TextView) findViewById(R.id.textView2);
            tx.setText("You are not connected");
            tx.setBackgroundColor(Color.RED);
        }
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p.show();
                ColorDrawable drawable = (ColorDrawable) b.getBackground();

            }
        });
    }

    @Override
    public void colorChanged(int color) {
        MainActivity.this.findViewById(android.R.id.content).setBackgroundColor(color);
        c = color;
        postData2();
    }

    Activity activity;

    public void getColor(View v) {
        new ColorPicker(activity, MainActivity.this, Color.WHITE).show();
    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    public void postData2() {
        new HttpHandler() {
            @Override
            public HttpUriRequest getHttpRequestMethod() {
                TextView tx = (TextView) findViewById(R.id.editText);
                String url = tx.getText().toString();

                HttpPost p = new HttpPost("http://" + url + "/rpi");
                p.addHeader("Content-type", "application/json");


                try {

                    JSONObject lightId = new JSONObject();
                    lightId.put("lightId", 1);
                    lightId.put("red", Color.red(c));
                    lightId.put("green", Color.green(c));
                    lightId.put("blue", Color.blue(c));
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
            public void onResponse(String result) {
                Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            }

        }.execute();

    }
}