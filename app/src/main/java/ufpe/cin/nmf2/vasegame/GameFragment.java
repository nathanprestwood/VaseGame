package ufpe.cin.nmf2.vasegame;

import android.annotation.SuppressLint;
import android.graphics.drawable.ClipDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Locale;
import java.util.UUID;

import ufpe.cin.nmf2.vasegame.CloudManager.CloudManager;
import ufpe.cin.nmf2.vasegame.CloudManager.FileHandler;
import ufpe.cin.nmf2.vasegame.database.DbManager;

public class GameFragment extends Fragment {
	private static final String TAG = "GameFragment";
	private static final String GAMETYPE = "GAMETYPE";
	private static final String USERNAME = "USERNAME";
	private String mGameType;
	private String mUsername;

	private DbManager mDbManager;
	private boolean mTimerStarted;
	private Button mFirstButton;
	private Button mSecondButton;
	private Button mThirdButton;

	private ToggleButton mFirstToggleButton;
	private ToggleButton mSecondToggleButton;
	private ToggleButton mThirdToggleButton;

	private TextView mFirstTextView;
	private TextView mSecondTextView;
	private TextView mThirdTextView;

	private ClipDrawable mFirstDrawable;
	private ClipDrawable mSecondDrawable;
	private ClipDrawable mThirdDrawable;

	private BucketAnimator mFirstAnimator;
	private BucketAnimator mSecondAnimator;
	private BucketAnimator mThirdAnimator;

	private TextView mCongratulationsTextView;

	private TextView mTimerTextView;

	private Counter mCounter = new Counter();

	private Button mResetButton;

	private Button mSaveButton;

	private FragmentActivity mFragmentActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.game_fragment, container, false);
		setUpLayout(view);

		mGameType = getArguments().getString(GAMETYPE);
		mUsername = getArguments().getString(USERNAME, Game.ANONYMOUS); // if no username is found in the args,
		//mUsername receives Game.ANONYMOUS.

		Log.d(TAG, "onCreateView: mUsername: " + mUsername);
		Log.d(TAG, "onCreateView: mGameType: " + mGameType);

		mFragmentActivity = getActivity();

		mDbManager = DbManager.getInstance(getContext());

		mFirstAnimator = new BucketAnimator(mFirstDrawable);
		mSecondAnimator = new BucketAnimator(mSecondDrawable);
		mThirdAnimator = new BucketAnimator(mThirdDrawable);

		if(mGameType.equals(Game.HARD)){
			Toast.makeText(this.getContext(), R.string.hard_warning,
					Toast.LENGTH_SHORT).show();
		}

		mFirstButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkTimer();
				initializeBucket(mFirstButton, mFirstTextView, 81, mFirstAnimator);
			}
		});

		mSecondButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
				checkTimer();
				initializeBucket(mSecondButton, mSecondTextView, 51, mSecondAnimator);
			}
		});

		mThirdButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkTimer();
				initializeBucket(mThirdButton, mThirdTextView, 31, mThirdAnimator);
			}
		});

		mFirstToggleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateBucket(81);
			}
		});

		mSecondToggleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateBucket(51);
			}
		});

		mThirdToggleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateBucket(31);
			}
		});

		mResetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				reset();
				mCounter.cancel();
				mCounter = new Counter();
			}
		});

		mSaveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSaveButton.setEnabled(false);
				Game game = new Game(UUID.randomUUID(), mUsername, mGameType, mCounter.getTime());
				mDbManager.addGame(game);
				Toast.makeText(getActivity(), getString(R.string.game_saved), Toast.LENGTH_SHORT).show();
				if(!mUsername.equals(Game.ANONYMOUS)) {
					CloudManager cloudManager = new CloudManager(getContext(), true, mUsername);
					cloudManager.sendGame(game);
				}
				else {
					FileHandler.write(getContext(), game.getId().toString(), false);
					Toast.makeText(mFragmentActivity, getString(R.string.login_to_sync), Toast.LENGTH_SHORT).show();
				}
			}
		});
		reset();
		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		//mDbManager.close();
		Log.d(TAG, "onDestroyView: database closed");
	}

	private static void updateText(TextView one, TextView two, int limit){
		int first = Integer.parseInt(one.getText().toString());
		int second = Integer.parseInt(two.getText().toString());

		int sum = first + second;

		if(sum - limit >= 0){
			second = limit;
			first = sum - limit;
		} else {
			second = sum;
			first = 0;
		}

		String temp = "" + first;
		one.setText(temp);
		temp = "" + second;
		two.setText(temp);
	}
	private class Counter extends CountDownTimer {
		private int mMinutes;
		private int mSeconds;
		private int mCents;
		private Counter(){
			super((long) 3600000, (long) 10);
		}
		public void onTick(long millisUntilFinished) {
			mCents++;
			if (mCents == 100) {
				mSeconds++;
				mCents = 0;
			}
			if (mSeconds == 60){
				mMinutes++;
				mSeconds = 0;
			}
			String string = String.format(Locale.US, "%02d:%02d:%02d", mMinutes, mSeconds, mCents);
			mTimerTextView.setText(string);
		}
		@SuppressLint("SetTextI18n")
		public void onFinish() {
			mTimerTextView.setText("Time's over. You suck!");
			try {
				Thread.sleep(500);
				reset();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		private long getTime(){
			return mMinutes*60*100 + mSeconds*100 + mCents;
		}
	}
	private void checkWin(){
		if(mFirstTextView.getText().toString().equals("41") ||
				mSecondTextView.getText().toString().equals("41") ||
				mThirdTextView.getText().toString().equals("41")) {

			mCongratulationsTextView.setVisibility(TextView.VISIBLE);
			mSaveButton.setVisibility(Button.VISIBLE);
			mCounter.cancel();

			//deactivate bucket buttons
			mFirstButton.setEnabled(false);
			mSecondButton.setEnabled(false);
			mThirdButton.setEnabled(false);

			//deactivate toggle buttons
			mFirstToggleButton.setEnabled(false);
			mSecondToggleButton.setEnabled(false);
			mThirdToggleButton.setEnabled(false);

			return;
		}
	}
	private void reset(){
		mFirstButton.setText(R.string.fulfill);
		mFirstButton.setEnabled(true);
		mFirstTextView.setText("0");

		mSecondButton.setText(R.string.fulfill);
		mSecondButton.setEnabled(true);
		mSecondTextView.setText("0");

		mThirdButton.setText(R.string.fulfill);
		mThirdButton.setEnabled(true);
		mThirdTextView.setText("0");

		mFirstDrawable.setLevel(0);
		mSecondDrawable.setLevel(0);
		mThirdDrawable.setLevel(0);

		mFirstToggleButton.setEnabled(true);
		mSecondToggleButton.setEnabled(true);
		mThirdToggleButton.setEnabled(true);

		mTimerStarted = false;
		mCounter.cancel();

		mTimerTextView.setText("");

		mCongratulationsTextView.setText(R.string.congrats);

		mCongratulationsTextView.setVisibility(TextView.INVISIBLE);

		mSaveButton.setVisibility(View.INVISIBLE);
		mSaveButton.setEnabled(true);
	}
	private void setUpLayout(View view){
		mFirstButton = (Button) view.findViewById(R.id.button_one);
		mSecondButton = (Button) view.findViewById(R.id.button_two);
		mThirdButton  = (Button) view.findViewById(R.id.button_tree);

		ImageView water = (ImageView) view.findViewById(R.id.water_one);
		mFirstDrawable = (ClipDrawable) water.getDrawable();
		water = (ImageView) view.findViewById(R.id.water_two);
		mSecondDrawable = (ClipDrawable) water.getDrawable();
		water = (ImageView) view.findViewById(R.id.water_three);
		mThirdDrawable = (ClipDrawable) water.getDrawable();

		mFirstToggleButton = (ToggleButton) view.findViewById(R.id.toggle_button_one);
		mSecondToggleButton  = (ToggleButton) view.findViewById(R.id.toggle_button_two);
		mThirdToggleButton  = (ToggleButton) view.findViewById(R.id.toggle_button_three);

		mFirstTextView  = (TextView) view.findViewById(R.id.text_view_one);
		mSecondTextView  = (TextView) view.findViewById(R.id.text_view_two);
		mThirdTextView  = (TextView) view.findViewById(R.id.text_view_three);

		mResetButton = (Button) view.findViewById(R.id.reset_button);
		mCongratulationsTextView = (TextView) view.findViewById(R.id.congrats_text_view);
		mTimerTextView = (TextView) view.findViewById(R.id.timer);
		mSaveButton = (Button) view.findViewById(R.id.save_button);

		reset();
	}
	private void checkTimer(){
		if(!mTimerStarted){
			mCounter.start();
			mTimerStarted = true;
		}
	}
	private synchronized void initializeBucket(Button button, TextView textView, int volume, BucketAnimator animator){
		if (mGameType.equals(Game.HARD)){
			button.setEnabled(false);
		}
		if (button.getText().equals(getString(R.string.fulfill))){
			animator.mHandler.removeCallbacks(animator.mRunnable);
			animator.mFulfill = true;
			animator.mFinalLevel = BucketAnimator.MAX_LEVEL;
			animator.mHandler.post(animator.mRunnable);
			if(!mGameType.equals(Game.HARD)) button.setText(R.string.empty);
			String maxVolume = "" + volume;
			textView.setText(maxVolume);
			Log.d(TAG, "initializeBucket:" + mGameType);
		} else {
			textView.setText("0");
			button.setText(R.string.fulfill);
			animator.mHandler.removeCallbacks(animator.mRunnable);
			animator.mFulfill = false;
			animator.mFinalLevel = 0;
			animator.mHandler.post(animator.mRunnable);
		}
	}
	private void updateWater(BucketAnimator down, BucketAnimator up, int downLevel, int upLevel){
		down.mHandler.removeCallbacks(down.mRunnable);
		down.mFulfill = false;
		down.mFinalLevel = downLevel*100;
		Log.d(TAG, "updateWater: downLevel: " + down.mFinalLevel);
		down.mHandler.post(down.mRunnable);

		up.mHandler.removeCallbacks(up.mRunnable);
		up.mFulfill = true;
		up.mFinalLevel = upLevel*100;
		Log.d(TAG, "updateWater: upLevel: " + up.mFinalLevel);
		up.mHandler.post(up.mRunnable);
	}
	private void updateBucket(int limit){
		if (limit == 81) {
			if (mSecondToggleButton.isChecked()) {
				int volume1 = Integer.parseInt(mSecondTextView.getText().toString());
				int volume2 = Integer.parseInt(mFirstTextView.getText().toString());
				int volumeToTransfer = (volume1 <= limit - volume2) ? volume1 : limit - volume2; //if the volume of the bucket to be emptied is lower than the volume the second bucket can take
				//take the fist option, else, take the second
				updateText(mSecondTextView, mFirstTextView, 81);
				updateWater(mSecondAnimator, mFirstAnimator, ((volume1 - volumeToTransfer)*100/51), ((volume2 + volumeToTransfer)*100/81));
				if (mSecondTextView.getText().equals("0")) mSecondButton.setText(R.string.fulfill);
				if (mFirstTextView.getText().equals(limit + "")) if(!mGameType.equals(Game.HARD)) mFirstButton.setText(R.string.empty);
				mFirstToggleButton.setChecked(false);
				mSecondToggleButton.setChecked(false);
				checkWin();
			} else if (mThirdToggleButton.isChecked()) {
				int volume1 = Integer.parseInt(mThirdTextView.getText().toString());
				int volume2 = Integer.parseInt(mFirstTextView.getText().toString());
				int volumeToTransfer = (volume1 <= limit - volume2) ? volume1 : limit - volume2; //if the volume of the bucket to be emptied is lower than the volume the second bucket can take
				//take the fist option, else, take the second
				updateText(mThirdTextView, mFirstTextView, 81);
				updateWater(mThirdAnimator, mFirstAnimator, (volume1 - volumeToTransfer)*100/31, (volume2 + volumeToTransfer)*100/81);
				if (mThirdTextView.getText().equals("0")) mThirdButton.setText(R.string.fulfill);
				if (mFirstTextView.getText().equals(limit + "")) if(!mGameType.equals(Game.HARD)) mFirstButton.setText(R.string.empty);
				mFirstToggleButton.setChecked(false);
				mThirdToggleButton.setChecked(false);
				checkWin();
			}
		} else if (limit == 51){
			if(mFirstToggleButton.isChecked()){
				int volume1 = Integer.parseInt(mFirstTextView.getText().toString());
				int volume2 = Integer.parseInt(mSecondTextView.getText().toString());
				int volumeToTransfer = (volume1 <= limit - volume2) ? volume1 : limit - volume2; //if the volume of the bucket to be emptied is lower than the volume the second bucket can take
				//take the fist option, else, take the second
				updateText(mFirstTextView, mSecondTextView, 51);
				updateWater(mFirstAnimator, mSecondAnimator, (volume1 - volumeToTransfer)*100/81, (volume2 + volumeToTransfer)*100/51);
				if (mFirstTextView.getText().equals("0"))mFirstButton.setText(R.string.fulfill);
				if (mSecondTextView.getText().equals(limit + "")) if(!mGameType.equals(Game.HARD)) mSecondButton.setText(R.string.empty);
				Log.d(TAG, "FBtn:" + mFirstButton.getText());
				mFirstToggleButton.setChecked(false);
				mSecondToggleButton.setChecked(false);
				checkWin();
			}
			else if(mThirdToggleButton.isChecked()){
				int volume1 = Integer.parseInt(mThirdTextView.getText().toString());
				int volume2 = Integer.parseInt(mSecondTextView.getText().toString());
				int volumeToTransfer = (volume1 <= limit - volume2) ? volume1 : limit - volume2; //if the volume of the bucket to be emptied is lower than the volume the second bucket can take
				updateText(mThirdTextView, mSecondTextView, 51);
				updateWater(mThirdAnimator, mSecondAnimator, (volume1 - volumeToTransfer)*100/31, (volume2 + volumeToTransfer)*100/51);
				if (mThirdTextView.getText().equals("0")) mThirdButton.setText(R.string.fulfill);
				if (mSecondTextView.getText().equals(limit + "")) if(!mGameType.equals(Game.HARD)) mSecondButton.setText(R.string.empty);
				mSecondToggleButton.setChecked(false);
				mThirdToggleButton.setChecked(false);
				checkWin();
			}
		} else {
			if(mFirstToggleButton.isChecked()){
				int volume1 = Integer.parseInt(mFirstTextView.getText().toString());
				int volume2 = Integer.parseInt(mThirdTextView.getText().toString());
				int volumeToTransfer = (volume1 <= limit - volume2) ? volume1 : limit - volume2; //if the volume of the bucket to be emptied is lower than the volume the second bucket can take
				updateText(mFirstTextView, mThirdTextView, 31);
				updateWater(mFirstAnimator, mThirdAnimator, (volume1 - volumeToTransfer)*100/81, (volume2 + volumeToTransfer)*100/31);
				if (mFirstTextView.getText().equals("0")) mFirstButton.setText(R.string.fulfill);
				if (mThirdTextView.getText().equals(limit + "")) if(!mGameType.equals(Game.HARD)) mThirdButton.setText(R.string.empty);
				mFirstToggleButton.setChecked(false);
				mThirdToggleButton.setChecked(false);
				checkWin();
			}
			if(mSecondToggleButton.isChecked()){
				int volume1 = Integer.parseInt(mSecondTextView.getText().toString());
				int volume2 = Integer.parseInt(mThirdTextView.getText().toString());
				int volumeToTransfer = (volume1 <= limit - volume2) ? volume1 : limit - volume2; //if the volume of the bucket to be emptied is lower than the volume the second bucket can take
				updateText(mSecondTextView, mThirdTextView, 31);
				updateWater(mSecondAnimator, mThirdAnimator, (volume1 - volumeToTransfer)*100/51, (volume2 + volumeToTransfer)*100/31);
				if (mSecondTextView.getText().equals("0")) mSecondButton.setText(R.string.fulfill);
				if (mThirdTextView.getText().equals(limit + "")) if(!mGameType.equals(Game.HARD)) mThirdButton.setText(R.string.empty);
				mThirdToggleButton.setChecked(false);
				mSecondToggleButton.setChecked(false);
				checkWin();
			}
		}
	}

	public class BucketAnimator {

		private static final int MAX_LEVEL = 10000;
		private final static int DELAY = 5;
		private final static int LEVEL_DIFF = 500;
		private ClipDrawable mDrawable;
		private int mFinalLevel;
		private int mLevel;
		private boolean mFulfill;
		private final Handler mHandler = new Handler();
		private final Runnable mRunnable = new Runnable() {
			@Override
			public void run() {
				mLevel = mDrawable.getLevel();
				doTheAnimation(mFinalLevel);
			}
		};

		private BucketAnimator(ClipDrawable drawable) {
			mDrawable = drawable;
		}

		public BucketAnimator() {

		}

		private synchronized void doTheAnimation(int toLevel) {
			if(mFulfill){
				if (mLevel < toLevel) {
					mLevel += LEVEL_DIFF;
					changeBucketLevel(mDrawable, mLevel);
					mHandler.postDelayed(mRunnable, DELAY);
				} else {
					mHandler.removeCallbacks(mRunnable);
				}
			}
			else{
				if (mLevel > toLevel) {
					mLevel -= LEVEL_DIFF;
					changeBucketLevel(mDrawable, mLevel);
					mHandler.postDelayed(mRunnable, DELAY);
				} else {
					mHandler.removeCallbacks(mRunnable);
				}
			}
		}
	}


	public synchronized void changeBucketLevel(final ClipDrawable mDrawable, final int mFinalLevel){
		mFragmentActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mDrawable.setLevel(mFinalLevel);
			}
		});
	}
}

