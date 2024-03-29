package nmargie.logintest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity which displays a login screen to the user.
 */
public class LoginActivity extends Activity {
	private static final String TAG = "LoginActivity";
    private static final boolean D = true;
	// REQUEST CODE returned by MainActivity to determine if user logged out or is still logged in
	protected static final int REQUEST_CODE_LOGIN_STATUS = 0;
	private final Class<MainActivity> DESTINATION_ACTIVITY = MainActivity.class;
	
	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */
	private static final String[] DUMMY_CREDENTIALS = new String[] {
			"foo@example.com:hello", "bar@example.com:world" };

	/**
	 * The default email to populate the email field with.
	 */
	//public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	
	// shared prefs
	public static SharedPreferences sharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		// get shared prefs (to remember login status)
		sharedPreferences = getPreferences(MODE_PRIVATE);
		
		super.onCreate(savedInstanceState);
		
		// check if user is logged in already
		if (sharedPreferences.getBoolean("user_logged_in", false)){
			// user is logged in already, move on to MainActivity
			Intent i = new Intent(this, DESTINATION_ACTIVITY);
			//startActivityForResult(i, REQUEST_CODE_LOGIN_STATUS);
			startActivity(i);
		}
		
		setContentView(R.layout.activity_login);

		// Set up the login form.
		//mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;
		if (D) Log.d(TAG, "+++ checking for valid password... ");
		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}
		
		if (D) Log.d(TAG, "+++ checking for valid email... ");
		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} 
		/*
		else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}
		*/

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			if (D) Log.d(TAG, "+++ login in background... ");
			// create UserLoginTask instance and pass it the email and password as string args
			mAuthTask = new UserLoginTask();
			mAuthTask.execute(mEmail, mPassword);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<String, Void, Boolean> {
		
		//private int id = -1;
		private final Class<MainActivity> DESTINATION_ACTIVITY = MainActivity.class;
		
		@Override
		protected Boolean doInBackground(String... args) {
			// TODO: attempt authentication against a network service.
			if(D)Log.d(TAG, "+++ doInBackground +++");
			String json = "{\"name\":\""+ args[0]+ "\",\"pass\":\""+args[1]+ "\"}";
			int responseCode = 0;
			String theString ="";
			String readLine ="";
		
			
			try {
				// Simulate network access.
				Thread.sleep(2000);
				/*
				HttpClient client = new DefaultHttpClient();
				// INSERT PHP LOGIN URL HERE:
				HttpPost httppost = new HttpPost("http://www.sharpic.ca/userlog.php");
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("emailAddress", args[0]));
				nameValuePairs.add(new BasicNameValuePair("password", args[1]));
				try {
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				} 
				catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				
				int executeCount = 0;
				HttpResponse response = null;
				
				do{
					executeCount++;
					try {
						if(D)Log.d(TAG, "+++ client.execute(httppost) "+executeCount+" +++");
						response = client.execute(httppost);
					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					responseCode = response.getStatusLine().getStatusCode();
					// to see response code in log (debugging):
					if (D) Log.d(TAG, "statusCode: " +responseCode);
				}
				// try to execute 5 times
				while (executeCount <5 && responseCode == 401);
				
				// TRY TO EXTRACT CONTENT FROM HTTP RESPONSE
				InputStream is = response.getEntity().getContent();
				//String myString = IOUtils.toString(is, "UTF-8");
				StringWriter writer = new StringWriter();
				IOUtils.copy(is, writer, "UTF-8");
				theString = writer.toString();
				*/
				
				URL url = new URL("http://www.sharpic.ca/userlog.php?user=" + json);
				URLConnection conn = url.openConnection();
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				readLine = rd.readLine();
				if (readLine != null){
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.connect();
					responseCode = connection.getResponseCode();
					
				}
				else responseCode = -1;
				
			}// end try
				
				/*
				BufferedReader rd;
				try {
					 rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				
				
				String line;
				
				while ( (line = rd.readLine()) != null)
				{
					result = line.trim();
					
				}
				id = Integer.parseInt(result);
				*/
			
			
				catch (InterruptedException e) {
					e.printStackTrace();
					return false; // result passed to onPostExecute
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				} 
				
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
		
			
			// 202 = success, else it failed.
			if (responseCode == 202){
				// get encryptKey
				return true;
			}
			else {
				// to see response code in log (debugging):
				if (D) Log.d(TAG, "statusCode failed FINAL: " +responseCode);
				//if (D) Log.d(TAG, "Final ID: " + id);
				//if (D) Log.d(TAG, "The response.getEntity().getContent() String ::::: " + theString);
				return false;
			}
			
			// FOR TESTING PURPOSES ONLY:
			/*
			for (String credential : DUMMY_CREDENTIALS) {
				String[] pieces = credential.split(":");
				if (pieces[0].equals(mEmail)) {
					// Account exists, return true if the password matches.
					return pieces[1].equals(mPassword);
				}
			}
			
			// TODO: register the new account here.
			return true; // result passed to onPostExecute
			*/
		
		}// end doInBackground

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				if (D) Log.d(TAG, "--- success onPostExecute... ---");
				//Successful login, go to main activity
				//startActivityForResult(i, REQUEST_CODE_LOGIN_STATUS);
				
				// send encryptKey to MainActivity
				
				
				startActivity(new Intent(LoginActivity.this, MainActivity.class));
				finish();
			} else {
				if (D) Log.d(TAG, "--- failure onPostExecute... ---");
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}// end userlogin task
}
