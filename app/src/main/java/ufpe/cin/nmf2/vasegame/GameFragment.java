package ufpe.cin.nmf2.vasegame;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
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
import ufpe.cin.nmf2.vasegame.database.DbManager;

public class GameFragment extends Fragment {
	private static final String TAG = "###MenuActivity";
	private static String sGameType;
	private static String sUsername;

	private DbManager mDbManager;

	private boolean mTimerStarted;
	private Button mFirstButton;
	private Button mSecondButton;
	private Button mThirdButton;

	private ToggleButton mFirstToggleButton;
	private ToggleButton mSecondToggleButton;
	private ToggleButton mThirdToggleButton;

	private ImageView mFirstBucket;
	private ImageView mSecondBucket;
	private ImageView mThirdBucket;

	private TextView mFirstTextView;
	private TextView mSecondTextView;
	private TextView mThirdTextView;

	private TextView mCongratulationsTextView;

	private TextView mTimerTextView;

	private Counter mCounter = new Counter(3600000, 10);

	private Button mResetButton;

	private Button mSaveButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.game_fragment, container, false);
		setUpLayout(view);
		Bundle bundle = getArguments();

		if(sGameType.equals(Game.HARD)){
			Toast.makeText(this.getContext(), R.string.hard_warning,
					Toast.LENGTH_LONG).show();
		}

		mFirstButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkTimer();
				initializeBucket(mFirstButton, mFirstTextView, 81);
			}
		});

		mSecondButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
				checkTimer();
				initializeBucket(mSecondButton, mSecondTextView, 51);
			}
		});

		mThirdButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkTimer();
				initializeBucket(mThirdButton, mThirdTextView, 31);
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
				mCounter = new Counter(3600000, 10);
			}
		});

		mSaveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Game game = new Game(UUID.randomUUID(), sUsername, sGameType, mCounter.getTime());
				if(mDbManager == null) mDbManager = new DbManager(getContext().getApplicationContext());
				mDbManager.addGame(game);
				mSaveButton.setEnabled(false);
				Toast.makeText(getActivity(), "Game saved!", Toast.LENGTH_SHORT).show();
				CloudManager cloudManager = new CloudManager(getContext());
				cloudManager.sendGame(game);
				DbManager.closeDb();
			}
		});
		reset();
		return view;
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
		private Counter (long a, long b){
			super(a, b);
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
			String temp = String.format(Locale.US, "%02d:%02d:%02d", mMinutes, mSeconds, mCents);
			mTimerTextView.setText(temp);
		}
		public void onFinish() {
			mTimerTextView.setText("Time's over. You suck!");
			try {
				Thread.sleep(500);
				reset();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		public long getTime(){
			return mMinutes*60*100 + mSeconds*100 + mCents;
		}
	}
	private boolean checkWin(){
		if(mFirstTextView.getText().toString().equals("41") ||
				mSecondTextView.getText().toString().equals("41") ||
				mThirdTextView.getText().toString().equals("41")){
			mCongratulationsTextView.setVisibility(TextView.VISIBLE);
			mSaveButton.setVisibility(Button.VISIBLE);
			mCounter.cancel();
			return true;
		}
		return false;
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
	private void initializeBucket(Button button, TextView textView, int volume){
		if (button.getText().equals("Fulfill")){
			String maxVolume = "" + volume;
			button.setText(R.string.empty);
			textView.setText(maxVolume);
			Log.d(TAG, "initializeBucket:" + sGameType);
		} else {
			if (sGameType.equals(Game.HARD)){
				textView.setText("0");
				button.setEnabled(false);
			} else {
				button.setText(R.string.fulfill);
				textView.setText("0");
			}
		}
	}
	private void updateBucket(int limit){
		if (limit == 81) {
			if (mSecondToggleButton.isChecked()) {
				updateText(mSecondTextView, mFirstTextView, 81);
				if (mSecondTextView.getText().equals("0")) mSecondButton.setText(R.string.fulfill);
				if (mFirstTextView.getText().equals(limit + "")) mFirstButton.setText(R.string.empty);
				Log.d(TAG, mSecondButton.getText().toString());
				mFirstToggleButton.setChecked(false);
				mSecondToggleButton.setChecked(false);
				checkWin();
			} else if (mThirdToggleButton.isChecked()) {
				updateText(mThirdTextView, mFirstTextView, 81);
				if (mThirdTextView.getText().equals("0")) mThirdButton.setText(R.string.fulfill);
				if (mFirstTextView.getText().equals(limit + "")) mFirstButton.setText(R.string.empty);
				mFirstToggleButton.setChecked(false);
				mThirdToggleButton.setChecked(false);
				checkWin();
			}
		} else if (limit == 51){
			if(mFirstToggleButton.isChecked()){
				updateText(mFirstTextView, mSecondTextView, 51);
				if (mFirstTextView.getText().equals("0"))mFirstButton.setText(R.string.fulfill);
				if (mSecondTextView.getText().equals(limit + "")) mSecondButton.setText(R.string.empty);
				Log.d(TAG, "FBtn:" + mFirstButton.getText());
				mFirstToggleButton.setChecked(false);
				mSecondToggleButton.setChecked(false);
				checkWin();
			}
			else if(mThirdToggleButton.isChecked()){
				updateText(mThirdTextView, mSecondTextView, 51);
				if (mThirdTextView.getText().equals("0")) mThirdButton.setText(R.string.fulfill);
				if (mSecondTextView.getText().equals(limit + "")) mSecondButton.setText(R.string.empty);
				mSecondToggleButton.setChecked(false);
				mThirdToggleButton.setChecked(false);
				checkWin();
			}
		} else {
			if(mFirstToggleButton.isChecked()){
				updateText(mFirstTextView, mThirdTextView, 31);
				if (mFirstTextView.getText().equals("0")) mFirstButton.setText(R.string.fulfill);
				if (mThirdTextView.getText().equals(limit + "")) mThirdButton.setText(R.string.empty);
				mFirstToggleButton.setChecked(false);
				mThirdToggleButton.setChecked(false);
				checkWin();
			}
			if(mSecondToggleButton.isChecked()){
				updateText(mSecondTextView, mThirdTextView, 31);
				if (mSecondTextView.getText().equals("0")) mSecondButton.setText(R.string.fulfill);
				if (mThirdTextView.getText().equals(limit + "")) mThirdButton.setText(R.string.empty);
				mThirdToggleButton.setChecked(false);
				mSecondToggleButton.setChecked(false);
				checkWin();
			}
		}
	}
	public static String getGameType() {
		return sGameType;
	}

	public static void setGameType(String gameType) {
		GameFragment.sGameType = gameType;
	}
	public static void setUsername(String username) {
		GameFragment.sUsername = username;
	}
}