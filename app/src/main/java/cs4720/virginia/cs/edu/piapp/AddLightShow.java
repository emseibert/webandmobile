package cs4720.virginia.cs.edu.piapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import java.util.ArrayList;
import android.view.View;
import android.widget.Button;


import org.apmem.tools.layouts.FlowLayout;

public class AddLightShow extends Activity implements ColorPicker.OnColorChangedListener {
    private int num_colors = 3;

    Activity activity;
    ColorPicker p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_light_show);
        this.activity = this;

        Button b = (Button) findViewById(R.id.btn_add_color);
        p = new ColorPicker(this.activity, AddLightShow.this, Color.WHITE);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p.show();
            }
        });

        FlowLayout layout = (FlowLayout)this.findViewById(R.id.flowlayout);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_light_show, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void colorChanged(int color) {
        //Get
        FlowLayout fl = (FlowLayout)this.findViewById(R.id.flowlayout);

        //Remove 'Add' button from view
        Button addButton = (Button)findViewById(R.id.btn_add_color);
        fl.removeView(addButton);

        //Create new color button
        Button myButton = new Button(this);
        myButton.setText("");
        myButton.setId(num_colors + 1);
        myButton.setBackgroundColor(color);
        myButton.setHeight(90);
        myButton.setWidth(90);

        //Add new color button to view
        fl.addView(myButton);

        //Re-add 'Add' button to view
        fl.addView(addButton);

        //Increment color counter
        num_colors = num_colors+1;
    }
}
