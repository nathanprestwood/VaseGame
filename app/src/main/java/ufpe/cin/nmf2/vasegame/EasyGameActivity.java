package ufpe.cin.nmf2.vasegame;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

public class EasyGameActivity extends SingleFragmentActivity {
	private String mUsername = null;
	private static final String GAMETYPE = "GAMETYPE";
	private static final String USERNAME = "USERNAME";
	private static final String TAG = "EasyGameActivity";
	public Fragment createFragment() {

		Bundle args = new Bundle();
		args.putString(GAMETYPE, Game.EASY);
		if(mUsername != null) args.putString(USERNAME, mUsername);

		GameFragment gameFragment = new GameFragment();
		gameFragment.setArguments(args);

		return gameFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		mUsername = getIntent().getStringExtra(USERNAME);
		Log.d(TAG, "onCreate: username: " + mUsername);
		super.onCreate(savedInstanceState);
	}
}
