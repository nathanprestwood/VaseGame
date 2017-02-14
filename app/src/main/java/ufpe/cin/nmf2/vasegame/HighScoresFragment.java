package ufpe.cin.nmf2.vasegame;

import android.icu.text.TimeZoneFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ufpe.cin.nmf2.vasegame.CloudManager.CloudManager;
import ufpe.cin.nmf2.vasegame.CloudManager.FileHandler;
import ufpe.cin.nmf2.vasegame.database.DbManager;

public class HighScoresFragment extends Fragment implements GetGameFinish{
	private static final String TAG = "HighScoresFragment";
	private static final String USERNAME = "USERNAME";
	private RecyclerView mHardGameRecyclerView;
	private RecyclerView mEasyGameRecyclerView;
	private GameAdapter mHardAdapter;
	private GameAdapter mEasyAdapter;
	private String mUsername;
	private HighScoresFragment mThis = this;

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container,
	                         Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.high_scores_fragment, container, false);

		mUsername = getArguments().getString(USERNAME, Game.ANONYMOUS);

		mHardGameRecyclerView = (RecyclerView) view.findViewById(R.id.hard_game_recycler_view);
		mHardGameRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

		mEasyGameRecyclerView = (RecyclerView) view.findViewById(R.id.easy_game_recycler_view);
		mEasyGameRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

		TextView usernameTextView = (TextView) view.findViewById(R.id.high_score_username_text_view);
		if(mUsername != null){
			usernameTextView.setText(mUsername);
		} else {
			usernameTextView.setText(Game.ANONYMOUS);
		}

		Button syncButton = (Button) view.findViewById(R.id.high_score_sync_button);

		syncButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mUsername.equals(Game.ANONYMOUS)) {
					CloudManager cloudManager = new CloudManager(getContext(), false, mUsername);
					cloudManager.getAndSaveGames(mThis); //also adds them to the local database
					cloudManager.sendGames(null);
					Toast.makeText(getActivity(), getString(R.string.syncing), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getActivity(), getString(R.string.login_to_sync), Toast.LENGTH_SHORT).show();
				}
			}
		});

		updateUI();

		return view;
	}
	private class GameHolder extends RecyclerView.ViewHolder{
		private final TextView mTitleTextView;
		private final TextView mDateTextView;

		private Game mGame;

		private GameHolder(View itemView) {
			super(itemView);
			mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_game_title_text_view);
			mDateTextView = (TextView) itemView.findViewById(R.id.list_item_game_date_text_view);
		}
		private void bindGame(Game game){
			mGame = game;
			mTitleTextView.setText(mGame.getTime());
			mDateTextView.setText(mGame.getDateForHighScores());
		}
	}
	private class GameAdapter extends RecyclerView.Adapter<GameHolder> {
		private final List<Game> mGames;
		private GameAdapter(List<Game> games) {
			mGames = games;
		}
		@Override
		public GameHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
			View view = layoutInflater
					.inflate(R.layout.list_item_game, parent, false);
			return new GameHolder(view);
		}
		@Override
		public void onBindViewHolder(GameHolder holder, int position) {
			Game game = mGames.get(position);
			holder.bindGame(game);
		}
		@Override
		public int getItemCount() {
			Log.d("GAdapter:getitemcount()", "mGames.size: " + mGames.size());
			return mGames.size();
		}

	}

	private void updateUI() {
		DbManager dbManager = DbManager.getInstance(getContext());
		List<Game> gamesList = dbManager.getGames();
		List<Game> hardGames = new ArrayList<>();
		List<Game> easyGames = new ArrayList<>();
		for (Game game : gamesList){
			if(game.isHard()) hardGames.add(game);
			else easyGames.add(game);
		}
		if (mHardAdapter == null) {
			if(hardGames.size() != 0) {
				mHardAdapter = new GameAdapter(hardGames);
				mHardGameRecyclerView.setAdapter(mHardAdapter);
			}
		} else {
			mHardAdapter.notifyDataSetChanged();
		}
		if (mEasyAdapter == null) {
			if(easyGames.size() != 0) {
				mEasyAdapter = new GameAdapter(easyGames);
				mEasyGameRecyclerView.setAdapter(mEasyAdapter);
			}
		} else {
			mEasyAdapter.notifyDataSetChanged();
		}
	}
	public void logIds(List<String> list){
		for (String item : list) Log.d(TAG, "logList: " + item);
	}

	@Override
	public void getFinished() {
		updateUI();
	}
}