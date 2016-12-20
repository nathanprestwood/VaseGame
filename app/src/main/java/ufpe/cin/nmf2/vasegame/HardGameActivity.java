package ufpe.cin.nmf2.vasegame;

import android.support.v4.app.Fragment;

public class HardGameActivity extends SingleFragmentActivity {
	public Fragment createFragment(){
		GameFragment.setGameType(Game.HARD);
		return new GameFragment();
	}
}
