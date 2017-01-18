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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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

public class CloudManager implements SendGameResponse, GetGameResponse {
	private static final String TAG = "CloudManager Activity";
	private static final int CONNECTION_UP = 0;
	private static final int ERROR_LOGIN = 108;
	private static final int ERROR_INTERNET_CONNECTION = 894;
	private static final int ERROR_BAD_RESPONSE = 764;
	private static final String ERROR_RETRIEVING_DATA = "ERROR_RETRIEVING_DATA";
	private static final String MYURL = "http://148.6.80.148:1026/v2/entities/";
	public static final MediaType JSON  = MediaType.parse("application/json; charset=utf-8");

	private Context mContext;
	private Game mGame;

	public CloudManager(Context context) {
		mContext = context;
	}

	private class SendGamesTask extends AsyncTask<List<Game>, Void, String> {
		SendGameResponse delegate;
		@Override
		protected String doInBackground(List<Game>... games) {
			// params comes from the execute() call: params[0] is the url.
			Log.d(TAG, "doInBackground: started SendGames task!");
			try {
				String result = "OK";
				for(Game game : games[0]){
					mGame = game;
					String gameStr = GameJson.gameToJson(mGame);
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
					if(response.code() != 422 && response.code() != 201){
						String id = mGame.getId().toString();
						result = "ERROR";
						Log.d(TAG, "SendGame: doInBackground: Error sending game " + id);
						Log.d(TAG, "SendGame: doInBackground: saving game to file, error code: " + response.code());
						FileHandler.write(mContext, id, false);
					}
				}

				return result;
			} catch (IOException e) {
				e.printStackTrace();
				return ERROR_RETRIEVING_DATA;
			}
		}
		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "SendGameTaks: onPostExecute result: " + result);
			delegate.sendFinish(result);
		}
	}

	public void sendGames(List<Game> games){
		if (games.size() == 0) return;
		FileHandler.write(mContext,"",false);
		sendGame(games);
	}

	public boolean sendGame(List<Game> games){
		int status = connectionStatus();
		if(games.get(0).getUsername().equals(Game.ANONYMOUS) || games.get(0).getUsername().equals("null")){
			return false;
		}
		if(status != CONNECTION_UP){
			Log.d(TAG, "sendGame: connectivity issue, handling code: " + status);
			handleConnectionStatus(status);
			return false;
		}
		Log.d(TAG, "sendGame: conection up!");
		SendGamesTask task = new SendGamesTask();
		task.delegate = this;
		task.execute(games);

		return true;
	}
	public boolean sendGame(Game game){
		//method used to send a single game to the cloud, when the used just played it
		List<Game> games = new ArrayList<>(1);
		games.add(game);
		return sendGame(games);
	}

	@Override
	public void sendFinish(String result) {
		//continue here, check the the response code and display a toast or something
		if( result.equals("ERROR") ){
			Log.d(TAG, "sendFinish: error code: " + result);
			Log.d(TAG, "sendFinish: saving game to file...");
			Toast.makeText(mContext, "Couldn't sync a few games, try again please", Toast.LENGTH_SHORT).show();
		} else {
			Log.d(TAG, "sendFinish: games sent, result " + result);
			Log.d(TAG, "sendFinish: erasing id files content: " + result);
			Toast.makeText(mContext, "All synced up!", Toast.LENGTH_SHORT).show();
			FileHandler.write(mContext, "", true);
		}
	}
	//##END OF SENDING

	private class GetGamesTask extends AsyncTask<String, Void, ArrayList<Game> > {
		GetGameResponse delegate = null;
		@Override
		protected ArrayList<Game> doInBackground(String... username) {

			ArrayList<Game> games = new ArrayList<>();
			String content = null;
			try {
				String url = MYURL +"?idPattern=" + username[0] + ".*&options=count";

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
			delegate.getFinish(result);
		}
	}

	public ArrayList<Game> getGames(String username){
		ArrayList<Game> games = new ArrayList<>();
		if(username == null) return null;
		if(username.equals(Game.ANONYMOUS) || username.equals("null")){
			return games;
		}
		int status = connectionStatus();
		if(status != CONNECTION_UP){
			Log.d(TAG, "sendGame: connectivity issue, handling code: " + status);
			handleConnectionStatus(status);
			return games;
		}

		GetGamesTask task = new GetGamesTask();
		task.delegate = this;
		task.execute(username);

		return games;
	}
	@Override
	public void getFinish(List<Game> games) {
		Log.d(TAG, "getFinish: finished getting games");
		DbManager dbManager = new DbManager(mContext);
		dbManager.addGames(games);
		DbManager.closeDb();
	}

	//End of GETTING


	public int connectionStatus(){
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
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.d(TAG, "connectionStatus: ok");

		return CONNECTION_UP;
	}
	public void handleConnectionStatus(int status){
		if(status == ERROR_BAD_RESPONSE) Toast.makeText(mContext,
				mContext.getString(R.string.network_logon_error), Toast.LENGTH_LONG).show();
		else if(status == ERROR_LOGIN) Toast.makeText(mContext, mContext.getString(R.string.login_error),
				Toast.LENGTH_LONG).show();
		else if(status == ERROR_INTERNET_CONNECTION) Toast.makeText(mContext,
				mContext.getString(R.string.connection_error), Toast.LENGTH_LONG).show();

	}


	public String readIt(InputStream stream, int len) throws IOException {
		Reader reader = null;
		reader = new InputStreamReader(stream, "UTF-8");
		char[] buffer = new char[len];
		reader.read(buffer);
		return new String(buffer);
	}
}
