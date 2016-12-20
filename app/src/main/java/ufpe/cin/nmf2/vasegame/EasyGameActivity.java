package ufpe.cin.nmf2.vasegame;

import android.support.v4.app.Fragment;

public class EasyGameActivity extends SingleFragmentActivity {
	public Fragment createFragment(){
		GameFragment.setGameType(Game.EASY);

		return new GameFragment();
	}
}
