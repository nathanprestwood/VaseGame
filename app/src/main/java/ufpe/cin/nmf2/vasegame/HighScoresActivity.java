package ufpe.cin.nmf2.vasegame;

import android.support.v4.app.Fragment;

public class HighScoresActivity extends SingleFragmentActivity {
	protected Fragment createFragment(){
		return new HighScoresFragment();
	}
}
