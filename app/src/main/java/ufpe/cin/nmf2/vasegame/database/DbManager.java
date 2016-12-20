package ufpe.cin.nmf2.vasegame.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ufpe.cin.nmf2.vasegame.Game;
import ufpe.cin.nmf2.vasegame.database.GamesDbSchema.GameTable;

public class DbManager {
	private static final String TAG = "DbManager";
	private Context mContext;
	private static SQLiteDatabase mDatabase;

	public DbManager(Context context){
		mContext = context.getApplicationContext();
		mDatabase = new GameBaseHelper(mContext).getWritableDatabase();

	}
	public static void closeDb(){
		if(mDatabase != null){
			mDatabase.close();
		}
	}
	private static ContentValues getContentValues(Game game){
		ContentValues values = new ContentValues();
		values.put(GameTable.Cols.UUID, game.getId().toString());
		values.put(GameTable.Cols.USERNAME, game.getUsername());
		values.put(GameTable.Cols.TYPE, game.getGameType());
		values.put(GameTable.Cols.DATE, game.getDate());
		values.put(GameTable.Cols.DURATION, game.getDuration());
		return values;
	}
	public void addGame(Game game){
		ContentValues values = getContentValues(game);
		mDatabase.insert(GameTable.NAME, null, values);
		//mDatabase.setVersion(mDatabase.getVersion() + 1);
	}
	public void updateGame(Game game) {
		String uuidString = game.getId().toString();
		ContentValues values = getContentValues(game);
		mDatabase.update(GameTable.NAME, values,
				GameTable.Cols.UUID + " = ?",
				new String[] { uuidString });
	}
	private GameCursorWrapper queryGames(String whereClause, String[] whereArgs) {
		Cursor cursor = mDatabase.query(
				GameTable.NAME,
				null, // Columns - null selects all columns
				whereClause,
				whereArgs,
				null, // groupBy
				null, // having
				null // orderBy
		);
		//cursor.close();
		return new GameCursorWrapper(cursor);
	}
	public class GameCursorWrapper extends CursorWrapper {
		public GameCursorWrapper(Cursor cursor) {
			super(cursor);
			cursor.moveToFirst();
		}
		public Game getGame(){
			String uuidString = getString(getColumnIndex(GameTable.Cols.UUID));
			String username = getString(getColumnIndex(GameTable.Cols.USERNAME));
			String gameType = getString(getColumnIndex(GameTable.Cols.TYPE));
			String date = getString(getColumnIndex(GameTable.Cols.DATE));
			long duration = getLong(getColumnIndex(GameTable.Cols.DURATION));

			return new Game(UUID.fromString(uuidString), username, gameType, duration, date);// add the date here
		}
	}
	@SuppressWarnings("TryFinallyCanBeTryWithResources")
	public List<Game> getGames() {
		List<Game> games = new ArrayList<>();
		GameCursorWrapper cursor = queryGames(null, null);

		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				games.add(cursor.getGame());
				cursor.moveToNext();
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		cursor.close();
		//g.d("Mine", "getGames array size: " + games.size());
		return games;
	}
	public List<Game> getHardGames(){
		List<Game> allGames = getGames();
		List<Game> games = new ArrayList<>();
		for (Game game : allGames){
			if(game.getGameType().equals(Game.HARD)){
				games.add(game);
			}
		}
		return games;
	}
	public List<Game> getEasyGames(){
		List<Game> allGames = getGames();
		List<Game> games = new ArrayList<>();

		for (Game game : allGames){
			if(game.getGameType().equals(Game.EASY)){
				games.add(game);
			}
		}
		return games;
	}

}
