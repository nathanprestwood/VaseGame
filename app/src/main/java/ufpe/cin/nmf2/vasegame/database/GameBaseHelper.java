package ufpe.cin.nmf2.vasegame.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ufpe.cin.nmf2.vasegame.database.GamesDbSchema.GameTable;

public class GameBaseHelper extends SQLiteOpenHelper {
	private static final int VERSION = 2;
	private static final String DATABASENAME= "gameBase.db";

	public GameBaseHelper(Context context){
		super(context, DATABASENAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table " + GameTable.NAME + "(" +
				" _id integer primary key autoincrement, "+
				GameTable.Cols.UUID + ", " +
				GameTable.Cols.USERNAME + ", " +
				GameTable.Cols.TYPE + ", " +
				GameTable.Cols.DATE + ", " +
				GameTable.Cols.DURATION +
				")"
		);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
