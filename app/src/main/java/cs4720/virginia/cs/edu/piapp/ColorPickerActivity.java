package cs4720.virginia.cs.edu.piapp;

/**
 * Created by emilyseibert on 11/4/14.
 */
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class ColorPickerActivity extends Activity implements
        ColorPicker.OnColorChangedListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.activity = this;
    }

    @Override
    public void colorChanged(int color) {
        ColorPickerActivity.this.findViewById(android.R.id.content)
                .setBackgroundColor(color);
    }

    Activity activity;

    public void getColor(View v) {
        new ColorPicker(activity, ColorPickerActivity.this, Color.WHITE)
                .show();
    }
}