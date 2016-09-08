package com.meetingplay;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by KSB1 on 3/13/2016.
 */
public class Login extends AppCompatActivity {

    private Button buttonHelp1, buttonHelp2, buttonQR, buttonApp;
    private EditText etEmail, etAppCode;
    private ProgressDialog prgDialog;

    private static final String TAG = Login.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        etEmail = (EditText)findViewById(R.id.etEmail);
        etAppCode = (EditText)findViewById(R.id.etAppCode);
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false);

        buttonHelp1 = (Button) findViewById(R.id.btHelp1);
        buttonHelp1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent helpIntent = new Intent(Login.this, Help.class);
                helpIntent.putExtra("helpkind", "help1");
                startActivity(helpIntent);
                Login.this.finish();
            }
        });

        buttonHelp2 = (Button) findViewById(R.id.btHelp2);
        buttonHelp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent helpIntent = new Intent(Login.this, Help.class);
                helpIntent.putExtra("helpkind", "help2");
                startActivity(helpIntent);
                Login.this.finish();
            }
        });

        buttonQR = (Button) findViewById(R.id.btScanQrcode);
        buttonQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                IntentIntegrator integrator = new IntentIntegrator(Login.this);
                integrator.setOrientationLocked(false).setPrompt(getString(R.string.scan_prompt_str)).initiateScan();
            }
        });

        buttonApp = (Button) findViewById(R.id.btYourApp);
        buttonApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AppLoad();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanresult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        Log.e(TAG, "IntentIntegrator.parseActivityResult(requestCode, resultCode, intent)--->" + scanresult);
        if (scanresult != null) {
            if (scanresult.getContents() == null) {
                Toast.makeText(Login.this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                String contents = scanresult.getContents();
                String formatName = scanresult.getFormatName();
                Log.e(TAG, "result.getContents():" + contents );

                if (resultCode == Activity.RESULT_OK) {
                    Log.e(TAG, "start url load");
                    webviewQR(contents);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefUser = getSharedPreferences("user_info", MODE_PRIVATE);
        String email = prefUser.getString("email", null);
        String appCode = prefUser.getString("appCode", null);
        etEmail.setText(email);
        etAppCode.setText(appCode);
    }

    private void AppLoad() {
        String email = etEmail.getText().toString();
        String appCode = etAppCode.getText().toString();
        String baseurl = getString(R.string.baseurl);
        String APIurl;

        if(Utility.isNotNull(email) && Utility.isNotNull(appCode) ){
            APIurl = baseurl + "email/" + email + "/pin/" + appCode;
            invokeWS(APIurl);
        }
        else{
            Toast.makeText(getApplicationContext(), "Please enter all fields", Toast.LENGTH_LONG).show();
        }
    }

    public void invokeWS(String APIurl){
        prgDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(APIurl ,null ,new AsyncHttpResponseHandler() {
            public void onSuccess(String response) {
                prgDialog.hide();
                try
                {
                    JSONObject obj = new JSONObject(response);
                    if(obj.getString("success").equals("true")){
                        SharedPreferences.Editor editor = getSharedPreferences("user_info", MODE_PRIVATE).edit();
                        editor.putString("email", etEmail.getText().toString());
                        editor.putString("appCode", etAppCode.getText().toString());
                        editor.putBoolean("flag_info", true);
                        editor.commit();

                        Intent intent = new Intent(Login.this, WebActivity.class);
                        intent.putExtra("domain", obj.getString("url"));
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "We're sorry, that email and pin combination did not work! Please try again.", Toast.LENGTH_LONG).show();
                    }
                }
                catch (JSONException e)
                {
                    Toast.makeText(getApplicationContext(), "We're sorry, that email and pin combination did not work! Please try again.", Toast.LENGTH_LONG).show();
                }
            }
            public void onFailure(int statusCode, Throwable error, String content) {
                prgDialog.hide();
                Toast.makeText(getApplicationContext(), "Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void webviewQR(String contents){

        Intent intent = new Intent(Login.this, WebActivity.class);
        intent.putExtra("domain", contents);
        startActivity(intent);

    }
}
