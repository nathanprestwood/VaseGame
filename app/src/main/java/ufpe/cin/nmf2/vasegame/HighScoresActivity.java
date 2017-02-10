package ufpe.cin.nmf2.vasegame;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

public class HighScoresActivity extends SingleFragmentActivity {
	private static final String USERNAME = "USERNAME";
	private static final String TAG = "HighSoresActivity";
	public String mUsername = null;
	protected Fragment createFragment(){
		HighScoresFragment highScoresFragment = new HighScoresFragment();
		Bundle args = new Bundle();
		if(mUsername != null) args.putString(USERNAME, mUsername);
		highScoresFragment.setArguments(args);
		return highScoresFragment;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mUsername = getIntent().getStringExtra(USERNAME);
		Log.d(TAG, "onCreate: username: " + mUsername);
		super.onCreate(savedInstanceState);
	}
}
