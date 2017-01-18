package ufpe.cin.nmf2.vasegame;

import android.graphics.drawable.ClipDrawable;

/**
 * Created by nmf2 on 16/01/2017.
 */

public class AnimatorWrapper{
	ClipDrawable mDrawable;
	int mFinalLevel;

	public AnimatorWrapper (){
	}
	public AnimatorWrapper(ClipDrawable drawable, int finalLevel) {
		mDrawable = drawable;
		mFinalLevel = finalLevel;
	}
	public AnimatorWrapper(ClipDrawable drawable) {
		mDrawable = drawable;
	}
}