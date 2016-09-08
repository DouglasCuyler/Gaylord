package com.meetingplay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by KSB1 on 3/13/2016.
 */
public class Help extends Activity {

    Button button;
    private String helpkind;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        addListenerOnButton();
        Bundle extras = getIntent().getExtras();
        helpkind = extras.getString("helpkind");

        LinearLayout  linearLayout = (LinearLayout) findViewById(R.id.linearLayoutid);

        if (helpkind.equals("help1")){
            linearLayout.setBackgroundResource(R.drawable.help1);
        }
        else
        {
            linearLayout.setBackgroundResource(R.drawable.help2);
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent(Help.this, Login.class);
        startActivity(returnIntent);
        Help.this.finish();
    }

    public void addListenerOnButton() {

        button = (Button) findViewById(R.id.btn_back);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent returnIntent = new Intent(Help.this, Login.class);
                startActivity(returnIntent);
                Help.this.finish();

            }
        });
    }
}

