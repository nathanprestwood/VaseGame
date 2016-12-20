package ufpe.cin.nmf2.vasegame;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import java.util.List;

import ufpe.cin.nmf2.vasegame.CloudManager.CloudManager;
import ufpe.cin.nmf2.vasegame.database.DbManager;


public class MenuFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener{
	private static final int RC_SIGN_IN = 9001;
	private static final String TAG = "MenuActivity";
	private Button mPlayHardButton;
	private Button mPlayEasyButton;
	private SignInButton mSignInButton;
	private Button mHighScoresButton;
	public static GoogleSignInAccount mAccount;
	public static String mUsername;

	private GoogleApiClient mGoogleApiClient;

	private TextView mWelcomeTextView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.menu_fragment, container, false);

		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestEmail()
				.build();

		mGoogleApiClient = new GoogleApiClient.Builder(this.getContext())
				.enableAutoManage(this.getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
				.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
				.build();

		setUpLayout(view);
		mPlayEasyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GameFragment.setUsername(mUsername);
				Intent intent = new Intent(getActivity(), EasyGameActivity.class);
				startActivity(intent);
			}
		});
		mPlayHardButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GameFragment.setUsername(mUsername);
				Intent intent = new Intent(getActivity(), HardGameActivity.class);
				startActivity(intent);

			}
		});
		mHighScoresButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), HighScoresActivity.class);
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

		return view;
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
		if (requestCode == RC_SIGN_IN) {
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			handleSignInResult(result);
		}
	}
	private void handleSignInResult(GoogleSignInResult result) {
		Log.d(TAG, "handleSignInResult:" + result.isSuccess());
		mAccount = result.getSignInAccount();
		if (result.isSuccess() && mAccount != null) {
			// Signed in successfully, show authenticated UI.
			mUsername = mAccount.getEmail();

			updateDatabase();
			updateUI();
		}
	}

	private void updateDatabase() {
		DbManager dbManager = new DbManager(getContext());
		List<Game> fromDb = dbManager.getGames();
		CloudManager cloudManager = new CloudManager(getContext());

		if(fromDb.size() > 0){
			String email = mAccount.getEmail();
			Log.d(TAG, "Email: " + email);
			List<Game> fromCloud = cloudManager.getGames(email);
			if(fromCloud.size() > fromDb.size())
				for (Game game : fromCloud)
					if (!fromDb.contains(game))
						dbManager.addGame(game);
		}
		DbManager.closeDb();
		Log.d(TAG, "updateDatabase: end");
	}

	private void updateUI() {
		if(mAccount != null) {
			mSignInButton.setVisibility(View.GONE);
			mWelcomeTextView.setVisibility(View.VISIBLE);
			mWelcomeTextView.setText(getString(R.string.greeting) + " " + mAccount.getDisplayName() + "!");
			mWelcomeTextView.setSelected(true);
			mWelcomeTextView.setSingleLine();
			Log.d(TAG, "mAccount.getDisplayName(): " + mAccount.getDisplayName());
		}
	}

	private void setUpLayout(View v){
		mPlayEasyButton = (Button) v.findViewById(R.id.play_easy_button);
		mPlayHardButton = (Button) v.findViewById(R.id.play_hard_button);
		mHighScoresButton = (Button) v.findViewById(R.id.scores_button);
		mSignInButton = (SignInButton) v.findViewById(R.id.sign_in_button);
		mWelcomeTextView = (TextView) v.findViewById(R.id.welcome_text_view);
		mWelcomeTextView.setVisibility(TextView.INVISIBLE);
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Toast.makeText(this.getContext(), getString(R.string.signin_error), Toast.LENGTH_LONG).show();
	}
	@Override
	public void onStart() {
		super.onStart();

		OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
		if (opr.isDone()) {
			// If the user's cached credentials are valid, the OptionalPendingResult will be "done"
			// and the GoogleSignInResult will be available instantly.
			Log.d(TAG, "Got cached sign-in");
			GoogleSignInResult result = opr.get();
			handleSignInResult(result);
		} else {
			// If the user has not previously signed in on this device or the sign-in has expired,
			// this asynchronous branch will attempt to sign in the user silently.  Cross-device
			// single sign-on will occur in this branch.
			opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
				@Override
				public void onResult(GoogleSignInResult googleSignInResult) {
					handleSignInResult(googleSignInResult);
				}
			});
		}
	}
}
