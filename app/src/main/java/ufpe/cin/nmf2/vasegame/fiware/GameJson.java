package ufpe.cin.nmf2.vasegame.fiware;

import android.util.Log;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ufpe.cin.nmf2.vasegame.Game;

@SuppressWarnings("ALL")
public class GameJson {
	private static final String TYPE = "GetMe41Game";
	private static final String TAG = "GameJson";
	@SuppressWarnings({"SameParameterValue", "CanBeFinal"})
	public class Id{
		String value;
		String type;
		Id(String v, String t){
			value = v;
			type = t;
		}
		@Override
		public String toString(){
			return value;
		}
	}
	@SuppressWarnings({"SameParameterValue", "CanBeFinal"})
	public class Username{
		String value;
		String type;
		Username(String v, String t){
			value = v;
			type = t;
		}
		@Override
		public String toString(){
			return value;
		}
	}
	@SuppressWarnings({"SameParameterValue", "CanBeFinal"})
	public class GameType{
		String value;
		String type;
		GameType(String v, String t){
			value = v;
			type = t;
		}
		@Override
		public String toString(){
			return value;
		}
	}
	@SuppressWarnings({"SameParameterValue", "CanBeFinal"})
	public class Duration{
		String value;
		String type;
		Duration(Long v, String t){
			value = Long.toString(v);
			type = t;
		}
		@Override
		public String toString(){
			return value;
		}
	}
	@SuppressWarnings({"SameParameterValue", "CanBeFinal"})
	public class MyDate{
		String value;
		String type;
		MyDate(String v, String t){
			value = v;
			type = t;
		}
		@Override
		public String toString(){
			return value;
		}
	}
	private String id;
	private String type;

	private Id gameId;
	private Username gameUsername;
	private GameType gameType;
	private Duration gameDuration;
	private MyDate gameDate;

	private GameJson(){

	}

	private GameJson(String id, String type, Game g){

		this.id = id;
		this.type = type;
		gameId = new Id(g.getId().toString(), "UUID");
		gameUsername = new Username(g.getUsername(), "String");
		gameType = new GameType(g.getGameType(), "String");
		gameDuration = new Duration(g.getDuration(), "Long");
		gameDate = new MyDate(getDateFromGame(g), "Date");
	}
	private GameJson(Game g){
		this.id = g.getUsername() + g.getId();
		this.type = TYPE;

		gameId = new Id(g.getId().toString(), "UUID");
		gameUsername = new Username(g.getUsername(), "String");
		gameType = new GameType(g.getGameType(), "String");
		gameDuration = new Duration(g.getDuration(), "Long");
		gameDate = new MyDate(getDateFromGame(g), "Date");
	}
	private String getDateFromGame(Game g){
		DateFormat targetFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
		return targetFormat.format(g.getDateInstance());
	}
	public Date getDateForGame(){
		DateFormat originalFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
		Date date = null;
		try {
			date = originalFormat.parse(this.gameDate.toString());

		} catch (ParseException e) {
			e.printStackTrace();
			Log.d(TAG, "getDateForGame: " + gameDate.toString());
			date = new Date();
		}
		return date;
	}
	public static String gameToJson(Game game){
		GameJson gameJson = new GameJson(game);
		Gson gson = new Gson();
		return gson.toJson(gameJson);
	}
	@Override
	public String toString(){
		return new Gson().toJson(this);
	}


	public MyDate getGameDate() {
		return gameDate;
	}

	public void setGameDate(MyDate gameDate) {
		this.gameDate = gameDate;
	}

	public Duration getGameDuration() {
		return gameDuration;
	}

	public void setGameDuration(Duration gameDuration) {
		this.gameDuration = gameDuration;
	}

	public GameType getGameType() {
		return gameType;
	}

	public void setGameType(GameType gameType) {
		this.gameType = gameType;
	}

	public Username getGameUsername() {
		return gameUsername;
	}

	public void setGameUsername(Username gameUsername) {
		this.gameUsername = gameUsername;
	}

	public Id getGameId() {
		return gameId;
	}

	public void setGameId(Id gameId) {
		this.gameId = gameId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}

