package ufpe.cin.nmf2.vasegame;

import android.support.v4.app.Fragment;

import ufpe.cin.nmf2.vasegame.database.DbManager;

public class EasyGameActivity extends SingleFragmentActivity {
	public Fragment createFragment(){
		GameFragment.setGameType(Game.EASY);
		return new GameFragment();
	}
	@Override
	protected void onPause(){
		super.onPause();
		DbManager.closeDb();
	}
}
