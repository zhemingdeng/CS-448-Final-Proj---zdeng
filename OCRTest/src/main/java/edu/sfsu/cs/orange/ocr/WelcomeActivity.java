package edu.sfsu.cs.orange.ocr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by MICHAEL DENG on 3/27/2016.
 */
public class WelcomeActivity extends Activity {
    private Button mScanButton;
    private Button mTypeInButton;
    private EditText mTypeInEditTextView;
    private static String startpoint;
    //private static final String START_POINT="edu.sfsu.cs.orange.ocr.start_point";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mScanButton=(Button)findViewById(R.id.scan_button);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(WelcomeActivity.this, CaptureActivity.class);
                startActivity(i);
            }
        });

        mTypeInEditTextView=(EditText)findViewById(R.id.edit_text_view);
        mTypeInEditTextView.setEnabled(true);

        mTypeInButton=(Button)findViewById(R.id.type_in_location_button);
        mTypeInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startpoint=mTypeInEditTextView.getText().toString();
                Intent i = NavigationActivity.newIntent(WelcomeActivity.this, startpoint);//turn to Navigation Activity
                startActivity(i);
            }
        });
    }
}
