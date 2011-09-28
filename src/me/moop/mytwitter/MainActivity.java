package me.moop.mytwitter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	Button mBtnDownload;
	EditText mEtxtUsername;
	TextView mTxtvUserInfo;
	ProgressDialog mProgressDialog;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nicelayout);
        
        mBtnDownload = (Button) findViewById(R.id.btnDownload);
        mEtxtUsername = (EditText) findViewById(R.id.etxtUsername);
        mTxtvUserInfo = (TextView) findViewById(R.id.txtvUserInfo);
    }
    
    public void downloadUserInfo(View view){
    	if (view == mBtnDownload){
    		String username = mEtxtUsername.getText().toString();
    		if (username.length() > 0){
        			mProgressDialog = new ProgressDialog(this);
        			mProgressDialog.setMessage("Bezig met het ophalen van gegevens...");
        			mProgressDialog.show();
        			new DownloadUserInfoTask().execute();
    			
    		}
    		else{
    			Toast.makeText(this, "Voer een twitter gebruikersnaam in", Toast.LENGTH_LONG).show();
    		}
    	}
    }
    
	private DefaultHttpClient createHttpClient() {
    	
		HttpParams my_httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(my_httpParams, 3000);
	    SchemeRegistry registry = new SchemeRegistry();
	    registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	    ThreadSafeClientConnManager multiThreadedConnectionManager = new ThreadSafeClientConnManager(my_httpParams, registry);
	    DefaultHttpClient httpclient = new DefaultHttpClient(multiThreadedConnectionManager, my_httpParams);
		return httpclient;
	}
    
    private class DownloadUserInfoTask extends AsyncTask<Void, Void, Void> {

    	int mStatusCode = 0;
    	String mResultString;
    	Exception mConnectionException;
    	
		@Override
		protected Void doInBackground(Void... args) {
			String username = mEtxtUsername.getText().toString();
			String encodedUserName= "";
			try {
				encodedUserName= URLEncoder.encode(username, "utf-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			String fetchUrl = "http://api.twitter.com/1/users/show.json?screen_name=" + encodedUserName;

			DefaultHttpClient httpclient = createHttpClient();
			HttpGet httpget = new HttpGet(fetchUrl);
		    
		    try {
				HttpResponse response = httpclient.execute(httpget);
				StatusLine statusLine = response.getStatusLine();
				mStatusCode  = statusLine.getStatusCode();
				
				if (mStatusCode == 200){
					mResultString = EntityUtils.toString(response.getEntity());
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				mConnectionException = e;
			} catch (IOException e) {
				e.printStackTrace();
				mConnectionException = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void arg) {
			mProgressDialog.dismiss();
			if (mStatusCode  == 200){
				mTxtvUserInfo.setText(mResultString);
			}
			else if (mStatusCode  == 404){
				Toast.makeText(MainActivity.this, "De gevraagde gebruiker bestaat niet.", Toast.LENGTH_LONG).show();
			}
			else if (mStatusCode > 0){
				Toast.makeText(MainActivity.this, "Er is in verbindingsfout opgetreden met foutcode " + mStatusCode, Toast.LENGTH_LONG).show();
			}
			else {
				Toast.makeText(MainActivity.this, "Gegevens konden niet worden opgehaald. Controleer uw internetverbinding en probeer het opnieuw (" +mConnectionException.toString() + ")" , Toast.LENGTH_LONG).show();
			}
		}
    }
}