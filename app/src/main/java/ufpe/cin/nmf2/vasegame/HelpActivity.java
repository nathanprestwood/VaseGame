package ufpe.cin.nmf2.vasegame;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by nmf2 on 02/02/2017.
 * Activity to display a help text for the game
 */

public class HelpActivity extends Activity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_layout);
	}
}
