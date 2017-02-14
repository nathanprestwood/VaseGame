package ufpe.cin.nmf2.vasegame.CloudManager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ufpe.cin.nmf2.vasegame.Game;
import ufpe.cin.nmf2.vasegame.GetGameFinish;
import ufpe.cin.nmf2.vasegame.HighScoresFragment;
import ufpe.cin.nmf2.vasegame.MenuFragment;
import ufpe.cin.nmf2.vasegame.R;
import ufpe.cin.nmf2.vasegame.database.DbManager;
import ufpe.cin.nmf2.vasegame.fiware.GameJson;

public class CloudManager {
	private static final String TAG = "CloudManager";
	private static final int CONNECTION_UP = 0;
	private static final int SYNCED = 0;
	private static final int ERROR_LOGIN = 108;
	private static final int ERROR_INTERNET_CONNECTION = 894;
	private static final int ERROR_BAD_RESPONSE = 764;
	private static final int ERROR_RETRIEVING_DATA = 62;
	private static final int ERROR_SENDING_DATA = 147;
	private static final String GETTING_URL = "http://148.6.80.148:1026/v2/entities/";
	private static final String SENDING_URL = "http://148.6.80.148:1026/v2/op/update";
	private static final MediaType JSON  = MediaType.parse("application/json; charset=utf-8");

	private final Context mContext;
	private String mUsername = null;
	private boolean mSingleGame;

	public CloudManager(Context context, boolean isSingleGame, String username) {
		mContext = context;
		mSingleGame = isSingleGame;
		mUsername = username;
	}

	private class SendGamesTask extends AsyncTask<List<Game>, Void, Integer> {

		@SafeVarargs
		@Override
		protected final Integer doInBackground(List<Game>... games) {
			// params comes from the execute() call: params[0] is the url.
			Log.d(TAG, "doInBackground: started");

			int status = connectionStatus(); //get the connection status

			if(status == CONNECTION_UP){// if the connections is up
				Log.d(TAG, "doInBackground: connection up!");

				DbManager dbManager = DbManager.getInstance(mContext); //get database instance
				String gamesStr = "{" +
						"  \"actionType\": \"APPEND\"," +
						"  \"entities\": [ "; //"header" for batch operation with FIRWARE ngsi v2
				if (games[0] == null) games[0] = getGamesInFile(); //get the games in the file
				if (games[0].size() > 0) { // if there are any games in the file
					for (Game game : games[0]) {
						if (game.getUsername().equals(Game.ANONYMOUS)) { //update username if it is Anonymoys
							game.setUsername(mUsername);
							dbManager.updateGame(game);
						}
						gamesStr += GameJson.gameToJson(game) + ","; // add entities to the payload
					}

					gamesStr = gamesStr.substring(0, gamesStr.length() - 1); // take out the last ',' or ' '

					gamesStr += "]}"; // finish the payload

					Log.d(TAG, "doInBackground: games string: " + gamesStr); //log it

					try {//send it to FIWARE
						OkHttpClient client = new OkHttpClient();

						RequestBody requestBody = RequestBody.create(JSON, gamesStr);

						Request request = new Request.Builder()
								.url(SENDING_URL)
								.addHeader("Content-Type", "application/json; charset=UTF-8")
								.post(requestBody)
								.build();

						Response response = client.newCall(request).execute();

						Log.d(TAG, "doInBackground: The response is: " + response.message());
						Log.d(TAG, "doInBackground: response body: " + response.body().string());

						if (response.code() != 422 && response.code() != 201 && response.code() != 204) { //if it was not successful
							status = ERROR_SENDING_DATA; //failed to retrieve data
						}
					} catch (IOException e) {
						e.printStackTrace();
						status =  ERROR_SENDING_DATA;
					}
				}
			}
			return status;
		}
		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(Integer result) {
			Log.d(TAG, "SendGameTask: onPostExecute result: " + result);
			if(result == SYNCED){
				Toast.makeText(mContext, mContext.getString(R.string.sync_success), Toast.LENGTH_SHORT).show();
				if (!mSingleGame) FileHandler.write(mContext, "", true);
			}
			else if(result == ERROR_INTERNET_CONNECTION)
				Toast.makeText(mContext, mContext.getString(R.string.connection_error), Toast.LENGTH_LONG).show();

			else if(result == ERROR_LOGIN)
				Toast.makeText(mContext, mContext.getString(R.string.login_error), Toast.LENGTH_LONG).show();

			else if(result == ERROR_BAD_RESPONSE)
				Toast.makeText(mContext,mContext.getString(R.string.network_logon_error), Toast.LENGTH_LONG).show();
			else
				Toast.makeText(mContext, mContext.getString(R.string.sync_error), Toast.LENGTH_SHORT).show();
		}
	}


	public void sendGames(List<Game> games){
		SendGamesTask task = new SendGamesTask();
		//noinspection unchecked
		task.execute(games);
	}
	public void sendGame(Game game){
		//method used to send a single game to the cloud, when the used just played it
		List<Game> games = new ArrayList<>(1);
		games.add(game);
		sendGames(games);
	}
	//##END OF SENDING

	private class GetGamesTask extends AsyncTask<String, Void, Boolean> {
		private GetGameFinish delegate;
		@Override
		protected Boolean doInBackground(String... username) {
			int status = connectionStatus(); //get the connection status

			if(status != CONNECTION_UP) return false;

			ArrayList<Game> games = new ArrayList<>();
			String content = null;
			try {
				String url = GETTING_URL + "?idPattern=" + username[0] + ".*&options=count&limit=1000&orderBy=!gameType,gameDuration";

				OkHttpClient client = new OkHttpClient();
				Request request = new Request.Builder()
						.url(url)
						.build();

				Response response = client.newCall(request).execute();
				content = response.body().string();
			} catch (IOException e) {
				e.printStackTrace();
			}

			JSONParser parser = new JSONParser();
			JSONArray array = null;
			try {
				if(content != null) array = (JSONArray) parser.parse(content);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(array != null){
				for(int i = 0; i < array.size(); i++){
					JSONObject obj = (JSONObject) array.get(i);
					String stringObj = obj.toJSONString();
					Log.d(TAG, "doInBackground: " + stringObj);
					Game g = Game.parseJson(stringObj);

					games.add(g);
				}
			}
			DbManager dbManager = DbManager.getInstance(mContext);
			dbManager.addGames(games);

			return true;
		}
		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(Boolean successful) {
			Log.d(TAG, "getFinished: finished getting games, result: " + successful);
			if(successful) {
				delegate.getFinished(); // this updates the HighScore Fragment UI
			}
		}
	}

	public void getAndSaveGames(HighScoresFragment listener){
		if(mUsername == null) return;

		if(mUsername.equals(Game.ANONYMOUS) || mUsername.equals("null")){
			return;
		}

		GetGamesTask task = new GetGamesTask();
		task.delegate = listener;
		task.execute(mUsername);
	}
	public void logList(List<Game> list){
		Log.d(TAG, "logList: started");
		for (Game item : list) Log.d(TAG, "logList: " + GameJson.gameToJson(item));
	}
	//End of GETTING


	private synchronized int connectionStatus(){
		ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo == null || !networkInfo.isConnected()) {
			return ERROR_INTERNET_CONNECTION;
		}

		if(MenuFragment.mAccount == null){
			return ERROR_LOGIN;
		}
		try {
			OkHttpClient client = new OkHttpClient();

			Request request = new Request.Builder()
					.url("http://148.6.80.148:1026/version")
					.build();

			Response response = client.newCall(request).execute();

			if (response.isRedirect()) {
				Log.d(TAG, "connectionStatus: redirect");
				return ERROR_BAD_RESPONSE;
			}
			else if(!response.isSuccessful()){
				Log.d(TAG, "connectionStatus: SERVER DOWN");
				return ERROR_BAD_RESPONSE;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return ERROR_INTERNET_CONNECTION;
		}
		Log.d(TAG, "connectionStatus: ok");

		return CONNECTION_UP;
	}
	@NonNull private List<Game> getGamesInFile(){

		DbManager dbManager = DbManager.getInstance(mContext);
		List<String> ids = FileHandler.getIds(mContext);

		return dbManager.getGamesWithTheseIds(ids);
	}
}
