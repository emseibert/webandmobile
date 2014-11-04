package cs4720.virginia.cs.edu.piapp;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.*;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements
    ColorPicker.OnColorChangedListener {
        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            this.activity = this;
            final Button b = (Button) findViewById(R.id.button);

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    TextView tx = (TextView) findViewById(R.id.textView);
                    ColorDrawable drawable = (ColorDrawable) b.getBackground();
                    tx.setText("" + drawable.getColor());
                }
            });
        }

        @Override
        public void colorChanged(int color) {
             MainActivity.this.findViewById(android.R.id.content)
                    .setBackgroundColor(color);
        }

        Activity activity;

    public void getColor(View v) {
        new ColorPicker(activity, MainActivity.this, Color.WHITE)
                .show();
    }
}