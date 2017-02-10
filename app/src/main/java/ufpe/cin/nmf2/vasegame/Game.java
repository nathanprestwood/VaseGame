package ufpe.cin.nmf2.vasegame;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import ufpe.cin.nmf2.vasegame.fiware.GameJson;


public class Game {
	public static final String HARD = "HARD";
	public static final String EASY = "EASY";
	public static final String ANONYMOUS = "Anonymous";
	private static final String TAG = "Game";
	private UUID mId;
	private String mUsername;
	private String mGameType;
	private long mDuration;
	private Date mDate;

	public Game(UUID id, String username, String gameType, long duration) {
		mId = id;
		mDuration = duration;
		if (username != null) mUsername = username;
		else mUsername = Game.ANONYMOUS;
		mGameType = gameType;
		mDate = new Date();
	}

	public Game(UUID id, String username, String gameType, long duration, String date) {
		this(id, username, gameType, duration);
		setDate(date);
	}

	public Game(UUID id, String username, String gameType, long duration, Date date) {
		this(id, username, gameType, duration);
		mDate = date;
	}

	public String getDateForHighScores() {
		return (new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()))
				.format(mDate);
	}
	public String getDate() {
		return (new SimpleDateFormat("yyyy/MM/dd", Locale.US))
				.format(mDate);
	}

	public void setDate(String date) {
		DateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
		try {
			mDate = format.parse(date);
		} catch (Exception e) {
			Log.d(TAG, "setDate: " + date);
			e.printStackTrace();
			mDate = new Date();
		}
	}

	public String getTime() {
		int minutes = (int) mDuration / 6000;
		int seconds = (int) (mDuration - minutes * 6000) / 100;
		int centis = (int) (mDuration) % 100;
		return String.format(Locale.getDefault(), "%02d:%02d:%02d", minutes, seconds, centis);
	}

	public String getJsonId() {
		return this.getUsername() + this.getId();
	}

	public static Game parseJson(String json) {
		Gson gson = new Gson();
		Type gameJsonType = new TypeToken<GameJson>() {
		}.getType();
		GameJson gameJson = gson.fromJson(json, gameJsonType);
		UUID uuid = UUID.fromString(gameJson.getGameId().toString());
		String username = gameJson.getGameUsername().toString();
		String gameType = gameJson.getGameType().toString();
		long duration = Long.parseLong(gameJson.getGameDuration().toString());
		Date date = gameJson.getDateForGame();
		return new Game(uuid, username, gameType, duration, date);
	}


	public UUID getId() {
		return mId;
	}

	public Date getDateInstance(){
		return mDate != null ? mDate : new Date();
	}

	public void setId(UUID mId) {
		this.mId = mId;
	}

	public long getDuration() {
		return mDuration;
	}

	public void setDuration(long duration) {
		mDuration = duration;
	}

	public String getUsername() {
		return mUsername;
	}

	public void setUsername(String username) {
		mUsername = username;
	}

	public String getGameType() {
		return mGameType;
	}

	public void setGameType(String gameType) {
		mGameType = gameType;
	}

	public boolean isHard(){
		return mGameType.equals(Game.HARD);
	}
}
