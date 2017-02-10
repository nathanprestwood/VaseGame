package ufpe.cin.nmf2.vasegame.CloudManager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import ufpe.cin.nmf2.vasegame.MenuFragment;
import ufpe.cin.nmf2.vasegame.R;
import ufpe.cin.nmf2.vasegame.database.DbManager;
import ufpe.cin.nmf2.vasegame.fiware.GameJson;

public class CloudManager {
	private static final String TAG = "CloudManager";
	private static final int CONNECTION_UP = 0;
	private static final int ERROR_LOGIN = 108;
	private static final int ERROR_INTERNET_CONNECTION = 894;
	private static final int ERROR_BAD_RESPONSE = 764;
	private static final String ERROR_RETRIEVING_DATA = "ERROR_RETRIEVING_DATA";
	private static final String MYURL = "http://148.6.80.148:1026/v2/entities/";
	private static final MediaType JSON  = MediaType.parse("application/json; charset=utf-8");

	private final Context mContext;
	private String mUsername = null;
	private boolean mSingleGame;

	private Game mGame;

	public CloudManager(Context context, boolean isSingleGame, String username) {
		mContext = context;
		mSingleGame = isSingleGame;
		mUsername = username;
	}

	private class SendGamesTask extends AsyncTask<List<Game>, Void, String> {
		@SafeVarargs
		@Override
		protected final String doInBackground(List<Game>... games) {
			// params comes from the execute() call: params[0] is the url.
			DbManager dbManager = DbManager.getInstance(mContext);
			Log.d(TAG, "doInBackground: started SendGames task!");
			try {
				String result = "OK";
				for(Game game : games[0]){
					mGame = game;
					if(game.getUsername().equals(Game.ANONYMOUS)){
						game.setUsername(mUsername);
						dbManager.updateGame(game);
					}
					String gameStr = GameJson.gameToJson(game);
					Log.d(TAG, "sendGameUtil: json: " + gameStr);

					OkHttpClient client = new OkHttpClient();

					RequestBody requestBody = RequestBody.create(JSON, gameStr);

					Request request = new Request.Builder()
							.url(MYURL)
							.addHeader("Content-Type", "application/json; charset=UTF-8")
							.post(requestBody)
							.build();

					Response response = client.newCall(request).execute();

					Log.d(TAG, "SendGame: The response is: " + response.message());
					Log.d(TAG, "doInBackground: response body: " + response.body().string());
					if(response.code() != 422 && response.code() != 201){
						String id = game.getId().toString();
						Log.d(TAG, "SendGame: doInBackground: Error sending game " + id);
						Log.d(TAG, "SendGame: doInBackground: saving game to file, error code: " + response.code());
						FileHandler.write(mContext, id, false);
					}
				}
				return result;
			} catch (IOException e) {
				e.printStackTrace();
				FileHandler.write(mContext, mGame.getId().toString(), false);
				return ERROR_RETRIEVING_DATA;
			}
		}
		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "SendGameTask: onPostExecute result: " + result);
			if( !result.equals("OK") ){
				Log.d(TAG, "sendFinish: error code: " + result);
				Log.d(TAG, "sendFinish: saving game to file...");
				Toast.makeText(mContext, mContext.getString(R.string.sync_error), Toast.LENGTH_SHORT).show();
			} else {
				Log.d(TAG, "sendFinish: games sent, result " + result);
				Toast.makeText(mContext, mContext.getString(R.string.sync_success), Toast.LENGTH_SHORT).show();
				if (!mSingleGame) FileHandler.write(mContext, "", true);
			}
		}
	}


	public void sendGames(List<Game> games){
		if (games.size() == 0) {
			Log.d(TAG, "sendGames: no games to send");
			return;
		}
		Log.d(TAG, "sendGame: started");
		int status = connectionStatus();
		if(status != CONNECTION_UP){
			Log.d(TAG, "sendGame: connectivity issue, handling code: " + status);
			handleConnectionStatus(status);
			return;
		}
		Log.d(TAG, "sendGame: connection up!");
		SendGamesTask task = new SendGamesTask();
		Log.d(TAG, "sendGame: executing");
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

	private class GetGamesTask extends AsyncTask<String, Void, ArrayList<Game> > {
		@Override
		protected ArrayList<Game> doInBackground(String... username) {

			ArrayList<Game> games = new ArrayList<>();
			String content = null;
			try {
				String url = MYURL +"?idPattern=" + username[0] + ".*&options=count&limit=100";

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
					Game g = Game.parseJson(stringObj);

					games.add(g);
				}
			}
			return games;
		}
		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(ArrayList<Game> result) {
			Log.d(TAG, "getFinish: finished getting games, result: " + result);
			if(result != null) {
				DbManager dbManager = DbManager.getInstance(mContext);
				dbManager.addGames(result);
			}
		}
	}

	public void getAndSaveGames(){
		if(mUsername == null) return;
		if(mUsername.equals(Game.ANONYMOUS) || mUsername.equals("null")){
			return;
		}
		int status = connectionStatus();
		if(status != CONNECTION_UP){
			Log.d(TAG, "sendGame: connectivity issue, handling code: " + status);
			handleConnectionStatus(status);
			return;
		}

		GetGamesTask task = new GetGamesTask();
		task.execute(mUsername);
	}
	public void logList(List<Game> list){
		Log.d(TAG, "logList: started");
		for (Game item : list) Log.d(TAG, "logList: " + GameJson.gameToJson(item));
	}
	//End of GETTING


	private int connectionStatus(){
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
		}
		Log.d(TAG, "connectionStatus: ok");

		return CONNECTION_UP;
	}
	private void handleConnectionStatus(int status){
		if(status == ERROR_BAD_RESPONSE) Toast.makeText(mContext,
				mContext.getString(R.string.network_logon_error), Toast.LENGTH_LONG).show();
		else if(status == ERROR_LOGIN) Toast.makeText(mContext, mContext.getString(R.string.login_error),
				Toast.LENGTH_LONG).show();
		else if(status == ERROR_INTERNET_CONNECTION) Toast.makeText(mContext,
				mContext.getString(R.string.connection_error), Toast.LENGTH_LONG).show();
	}
	public void setSingleGame(boolean value){
		mSingleGame = value;
	}
}
