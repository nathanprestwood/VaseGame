package ufpe.cin.nmf2.vasegame;

import android.support.v4.app.Fragment;

import ufpe.cin.nmf2.vasegame.database.DbManager;

public class HighScoresActivity extends SingleFragmentActivity {
	protected Fragment createFragment(){
		return new HighScoresFragment();
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
		DbManager.closeDb();
	}
}
