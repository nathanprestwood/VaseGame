package ufpe.cin.nmf2.vasegame;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;


public class MenuFragment extends Fragment{
	private static final int RC_SIGN_IN = 9001;
	private static final String TAG = "MenuActivity";
	private static final String USERNAME = "USERNAME";
	private Button mPlayHardButton;
	private Button mPlayEasyButton;
	private Button mHelp;
	private SignInButton mSignInButton;
	private Button mHighScoresButton;
	public GoogleSignInAccount mAccount;
	public String mUsername = null;

	private GoogleApiClient mGoogleApiClient;

	private TextView mWelcomeTextView;
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.menu_fragment, container, false);

		setUpLayout(view);

		mPlayEasyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), EasyGameActivity.class);
				if (mUsername != null) intent.putExtra(USERNAME, mUsername);
				startActivity(intent);
			}
		});
		mPlayHardButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), HardGameActivity.class);
				if (mUsername != null) intent.putExtra(USERNAME, mUsername);
				startActivity(intent);
			}
		});
		mHelp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), HelpActivity.class);
				startActivity(intent);
			}
		});
		mHighScoresButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), HighScoresActivity.class);
				if (mUsername != null) intent.putExtra(USERNAME, mUsername);
				startActivity(intent);
			}
		});
		mSignInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
				startActivityForResult(signInIntent, RC_SIGN_IN);
			} else {
				Toast.makeText(getContext(), getString(R.string.connection_error), Toast.LENGTH_LONG).show();
			}
			}
		});

		//set up Google sign in resources
		SetUpSignInTask task = new SetUpSignInTask();
		task.execute(new Void[1]);

		return view;
	}
	private class SetUpSignInTask extends AsyncTask<Void, Void, Void> implements  GoogleApiClient.OnConnectionFailedListener{
		@Override
		protected Void doInBackground(Void... params) {
			GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
					.requestEmail()
					.build();

			mGoogleApiClient = new GoogleApiClient.Builder(getContext())
					.enableAutoManage(getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
					.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
					.build();
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
		}

		@Override
		public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
			Log.d(TAG, "onConnectionFailed: login connection failed");
			mSignInButton.setVisibility(View.VISIBLE);
			mWelcomeTextView.setVisibility(View.INVISIBLE);
			Toast.makeText(getContext(), getString(R.string.signin_error), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//Log.d(TAG, "onActivityResult: resultCode: " + resultCode + " requestCode: " + requestCode);
		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
		if (requestCode == RC_SIGN_IN) {
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			handleSignInResult(result);
		}
	}
	private void handleSignInResult(GoogleSignInResult result) {
		mAccount = result.getSignInAccount();
		Log.d(TAG, "handleSignInResult: " + result.isSuccess() + " mAccount: " + mAccount);

		if (result.isSuccess() && mAccount != null) {
			// Signed in successfully, show authenticated UI.
			mUsername = mAccount.getEmail();

			Log.d(TAG, "handleSignInResult: username: " + mUsername);
			String displayName = mAccount.getDisplayName();
			Log.d(TAG, "handleSignInResult: displayName: " + displayName);

			if(mUsername != null && displayName != null) {
				mSignInButton.setVisibility(View.INVISIBLE);
				mWelcomeTextView.setVisibility(View.VISIBLE);
				mWelcomeTextView.setText(getString(R.string.greeting) + " " + displayName + "!");
				mWelcomeTextView.setSelected(true);
			} else {
				mUsername = null;
			}
		} else {
			mSignInButton.setVisibility(View.VISIBLE);
			mWelcomeTextView.setVisibility(View.INVISIBLE);
		}
	}

	private void setUpLayout(View v){
		mPlayEasyButton = (Button) v.findViewById(R.id.play_easy_button);
		mPlayHardButton = (Button) v.findViewById(R.id.play_hard_button);
		mHighScoresButton = (Button) v.findViewById(R.id.scores_button);
		mSignInButton = (SignInButton) v.findViewById(R.id.sign_in_button);
		mWelcomeTextView = (TextView) v.findViewById(R.id.welcome_text_view);
		mWelcomeTextView.setVisibility(TextView.INVISIBLE);
		mWelcomeTextView.setSelected(true);
		mWelcomeTextView.setSingleLine();
		mHelp = (Button) v.findViewById(R.id.help_button);
	}

	@Override
	public void onStart() {
		super.onStart();

		if(mGoogleApiClient != null) {
			OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
			if (opr.isDone()) {
				// If the user's cached credentials are valid, the OptionalPendingResult will be "done"
				// and the GoogleSignInResult will be available instantly.
				//Log.d(TAG, "Got cached sign-in");
				GoogleSignInResult result = opr.get();
				handleSignInResult(result);
			} else {
				// If the user has not previously signed in on this device or the sign-in has expired,
				// this asynchronous branch will attempt to sign in the user silently.  Cross-device
				// single sign-on will occur in this branch.
				opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
					@Override
					public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
						handleSignInResult(googleSignInResult);
					}
				});
			}
		}
	}
}
