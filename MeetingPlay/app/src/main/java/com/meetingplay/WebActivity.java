package com.meetingplay;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gimbal.android.BeaconEventListener;
import com.gimbal.android.BeaconManager;
import com.gimbal.android.BeaconSighting;
import com.gimbal.android.CommunicationManager;
import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Visit;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.meetingplay.api.APIClient;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;

public class WebActivity extends AppCompatActivity {

    private static final String TAG = WebActivity.class.getSimpleName();
    private static final int FILECHOOSER_RESULTCODE = 1;
    private static final int FILECHOOSER_RESULTCODE_NATIVE = 2;
    private static final int SCANNER = 3;

    private WebView mWebView;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessage2;

    private String mUserIdNativePhoto;
    private String weburl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Bundle extras = getIntent().getExtras();
        weburl = extras.getString("domain");

        //WEBVIEW
        mWebView = (WebView) findViewById(R.id.webView);

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Url : " + url);
                }

                if (url.contains("meetingplay://photo")
                        || url.contains("photo-upload")
                        ) {
                    mUserIdNativePhoto = Uri.parse(url).getQueryParameter("userID");
                    Log.i(TAG, "User " + mUserIdNativePhoto + " active");

                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("*/*");
                    startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE_NATIVE);

                    return true;
                } else if (url.equals("meetingplay://scanner")
                        || url.equals("hp2015://scanner")) {
                    //startActivityForResult(new Intent(WebActivity.this, ScannerActivity.class), SCANNER);
                    IntentIntegrator integrator = new IntentIntegrator(WebActivity.this);
                    integrator.setOrientationLocked(false).setPrompt(getString(R.string.scan_prompt_str)).initiateScan();
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             WebChromeClient.FileChooserParams fileChooserParams) {
                mUploadMessage2 = filePathCallback;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);

                return true;
            }

            //For Android 4.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }
        });

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.loadUrl(weburl);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        Log.e(TAG, "IntentIntegrator.parseActivityResult(requestCode, resultCode, intent)--->" + requestCode +" : " + resultCode +" : " + intent +" : " );
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (mUploadMessage != null) {
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            } else if (mUploadMessage2 != null) {

                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                if (result != null) {
                    mUploadMessage2.onReceiveValue(new Uri[]{result});
                } else {
                    mUploadMessage2.onReceiveValue(null);
                }
                mUploadMessage2 = null;
            }
        } else if (requestCode == SCANNER) {
            if (resultCode == Activity.RESULT_OK) {
                mWebView.loadUrl(intent.getData().toString());
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE_NATIVE) {

            final Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();

            Log.e(TAG, "Uri result" + result );

            if (result != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Log.e(TAG, "send Image: sdfgsdgdfgdfg");
                        sendImage(ContentProviderUtils.getPath(WebActivity.this, result));
                    }
                }).start();
            }
        }

        IntentResult scanresult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        Log.e(TAG, "IntentIntegrator.parseActivityResult(requestCode, resultCode, intent)--->" + scanresult );
        if (scanresult != null) {
            if (scanresult.getContents() == null) {
                Toast.makeText(WebActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                String contents = scanresult.getContents();
                String formatName = scanresult.getFormatName();
                Log.e(TAG, "result.getContents():" + contents );

                if (resultCode == Activity.RESULT_OK) {
                    Log.e(TAG, "sstart url load");
                    mWebView.loadUrl(contents);
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void sendImage(String imageUri) {

        URL connectURL;
        try {
            connectURL = new URL(getString(R.string.url) + "user/" + mUserIdNativePhoto + "/photos");
            //connectURL = new URL("http://ccoc15api.meetingplay.com/user/" + mUserIdNativePhoto + "/photos");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(WebActivity.this, "Failed to upload photo. [1]", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        String iFileName;
        final String LINE_END = "\r\n";
        final String TWO_HYPHENS = "--";
        final String boundary = "*****";
        try {
            File sourceFile = new File(imageUri);
            iFileName = sourceFile.getName();

            // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);

            // Open a HTTP connection to the URL
            HttpURLConnection conn = (HttpURLConnection) connectURL.openConnection();

            // Allow Inputs
            conn.setDoInput(true);

            // Allow Outputs
            conn.setDoOutput(true);

            // Don't use a cached copy.
            conn.setUseCaches(false);

            // Use a post method.
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("X-Authentication-Token", "Sm9lLCBkb24ndCByZWJvb3QgdGhlIHNlcnZlciE=");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(TWO_HYPHENS + boundary + LINE_END);
            dos.writeBytes("Content-Disposition: form-data; name=\"photo\";filename=\"" + iFileName + "\"" + LINE_END);
            dos.writeBytes(LINE_END);

            // create a buffer of maximum size
            int bytesAvailable = fileInputStream.available();

            int maxBufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];

            // read file and write it into form...
            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            dos.writeBytes(LINE_END);
            dos.writeBytes(TWO_HYPHENS + boundary + TWO_HYPHENS + LINE_END);

            // close streams
            fileInputStream.close();

            dos.flush();

            final int responseCode = conn.getResponseCode();
            Log.i(TAG, "File Sent, Response: " + responseCode);

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(WebActivity.this, "Failed to upload photo [" + responseCode + "]",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(WebActivity.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
                }
            });

            InputStream is = conn.getInputStream();

            // retrieve the response from server
            int ch;

            StringBuffer b = new StringBuffer();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
            String s = b.toString();
            Log.i("Response", s);
            dos.close();
        } catch (MalformedURLException ex) {
            Log.e(TAG, "URL error: " + ex.getMessage(), ex);
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(WebActivity.this, "Failed to upload photo. [2]", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException ioe) {
            Log.e(TAG, "IO error: " + ioe.getMessage(), ioe);
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(WebActivity.this, "Failed to upload photo. [3]", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //POST callback
    private Callback<JsonObject> sendCallback = new Callback<JsonObject>() {
        @Override
        public void failure(RetrofitError error) {
            //API callback failed here
            Log.e("Send Failed", error.toString());
            String message = String.format("API call failed");
            //Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        }

        @Override
        public void success(JsonObject resultJson,
                            retrofit.client.Response response) {
            //API callback sucess here
            Log.e("Send Success", resultJson.toString());
            String message = String.format("API call success:\n\n %s", resultJson.toString());
            //Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
