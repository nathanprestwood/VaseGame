package ufpe.cin.nmf2.vasegame.database;

public class GamesDbSchema {
	public static final class GameTable{
		public static final String NAME = "games";
		public static final class Cols {
			public static final String UUID = "uuid";
			public static final String USERNAME = "username";
			public static final String DURATION = "time";
			public static final String DATE = "date";
			public static final String TYPE = "type";
		}
	}
}
