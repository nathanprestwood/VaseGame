package ufpe.cin.nmf2.vasegame.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ufpe.cin.nmf2.vasegame.Game;
import ufpe.cin.nmf2.vasegame.database.GamesDbSchema.GameTable;

@SuppressWarnings("SameParameterValue")
public class DbManager {
	private static final String TAG = "DbManager";
	//private final Context mContext;
	private SQLiteDatabase mDatabase;
	@SuppressLint("StaticFieldLeak")
	private static DbManager instance;


	private DbManager(Context context){
	//	mContext = context.getApplicationContext();
		if(mDatabase == null) mDatabase = new GameBaseHelper(context.getApplicationContext()).getWritableDatabase();
	}
	public static synchronized DbManager getInstance(Context context){
		if (instance == null) instance = new DbManager(context);
		return instance;
	}

	private synchronized static ContentValues getContentValues(Game game){
		ContentValues values = new ContentValues();
		values.put(GameTable.Cols.UUID, game.getId().toString());
		values.put(GameTable.Cols.USERNAME, game.getUsername());
		values.put(GameTable.Cols.TYPE, game.getGameType());
		values.put(GameTable.Cols.DATE, game.getDate());
		values.put(GameTable.Cols.DURATION, game.getDuration());
		return values;
	}
	public synchronized void addGame(Game game){
		ContentValues values = getContentValues(game);
		mDatabase.insertWithOnConflict(GameTable.NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
	}
	public synchronized void addGames(List<Game> games){
		List<Game> stored = getGames();
		for (int i = 0; i < stored.size(); i++){//removing duplicates // CONFLICT_IGNORE doesn't work
			games.remove(stored.get(i));
		}
		for(Game game : games){
			addGame(game);
		}
	}

	public synchronized void updateGame(Game game) {
		String uuidString = game.getId().toString();
		ContentValues values = getContentValues(game);
		int numberOfRows = mDatabase.update(GameTable.NAME, values,
				GameTable.Cols.UUID + " = ?",
				new String[] { uuidString });
		//Log.d(TAG, "DbManager: updateGame: uuid: " + uuidString);
		//Log.d(TAG, "updateGame: number of games updated: " + numberOfRows);
	}
	private synchronized GameCursorWrapper queryGames(String whereClause, String[] whereArgs) {
		@SuppressLint("Recycle") Cursor cursor = mDatabase.query(
				GameTable.NAME,
				null, // Columns - null selects all columns
				whereClause,
				whereArgs,
				null, // groupBy
				null, // having
				GameTable.Cols.DURATION + " ASC" // orderBy
		);
		return new GameCursorWrapper(cursor);
	}
	private class GameCursorWrapper extends CursorWrapper {
		private GameCursorWrapper(Cursor cursor) {
			super(cursor);
			cursor.moveToFirst();
		}
		public Game getGame(){
			String uuidString = getString(getColumnIndex(GameTable.Cols.UUID));
			String username = getString(getColumnIndex(GameTable.Cols.USERNAME));
			String gameType = getString(getColumnIndex(GameTable.Cols.TYPE));
			String date = getString(getColumnIndex(GameTable.Cols.DATE));
			Log.d(TAG, "getGame: date in the database: " + date);
			long duration = getLong(getColumnIndex(GameTable.Cols.DURATION));

			return new Game(UUID.fromString(uuidString), username, gameType, duration, date);// add the date here
		}
	}
	@SuppressWarnings("TryFinallyCanBeTryWithResources")
	public synchronized List<Game> getGames() {
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

		return games;
	}
	public synchronized List<Game> getGamesWithTheseIds(List<String> ids) {
		List<Game> games = new ArrayList<>();
		GameCursorWrapper cursor = queryGames(null, null);
		Game game = null;
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				game = cursor.getGame();
				if(ids.contains(game.getId().toString())) {
					games.add(game);
					ids.remove(game.getId().toString());
				}
				cursor.moveToNext();
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		cursor.close();

		return games;
	}
}
