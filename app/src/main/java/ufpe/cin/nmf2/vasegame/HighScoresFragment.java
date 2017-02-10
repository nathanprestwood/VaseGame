package ufpe.cin.nmf2.vasegame;

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

public class HighScoresFragment extends Fragment{
	private static final String TAG = "HighScoresFragment";
	private static final String USERNAME = "USERNAME";
	private RecyclerView mHardGameRecyclerView;
	private RecyclerView mEasyGameRecyclerView;
	private GameAdapter mHardAdapter;
	private GameAdapter mEasyAdapter;
	private String mUsername;

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
		syncButton.setText(R.string.sync);

		syncButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mUsername.equals(Game.ANONYMOUS)) {
					CloudManager cloudManager = new CloudManager(getContext(), false, mUsername);
					cloudManager.getAndSaveGames(); //also adds them to the local database
					cloudManager.sendGames(getGamesInFile());
					Toast.makeText(getActivity(), getString(R.string.syncing), Toast.LENGTH_SHORT).show();
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
		private final SortedList<Game> mGames;
		private GameAdapter(List<Game> games) {
			mGames = new SortedList<>(Game.class, new SortedList.Callback<Game>(){
				@Override
				public int compare(Game g1, Game g2) {
					return (int) (g1.getDuration() - g2.getDuration());
				}
				@Override
				public void onInserted(int position, int count) {
					notifyItemRangeInserted(position, count);
				}
				@Override
				public void onRemoved(int position, int count) {
					notifyItemRangeRemoved(position, count);
				}
				@Override
				public void onMoved(int fromPosition, int toPosition) {
					notifyItemMoved(fromPosition, toPosition);
				}
				@Override
				public void onChanged(int position, int count) {
					notifyItemRangeChanged(position, count);
				}
				@Override
				public boolean areContentsTheSame(Game oldItem, Game newItem) {
					// return whether the items' visual representations are the same or not.
					return (oldItem.getDuration() == newItem.getDuration() &&
							oldItem.getDateForHighScores().equals(newItem.getDateForHighScores()) &&
							oldItem.getGameType().equals(newItem.getGameType()));
				}
				@Override
				public boolean areItemsTheSame(Game item1, Game item2) {
					return item1.getId().equals(item2.getId());
				}
			});
			mGames.addAll(games);
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
			//Log.d("GAdapter:getitemcount()", "mGames.size: " + mGames.size());
			return mGames.size();
		}
	}

	private void updateUI() {
		DbManager dbManager = DbManager.getInstance(getContext());
		List<Game> hardGames = dbManager.getHardGames();
		List<Game> easyGames = dbManager.getEasyGames();
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
	private List<Game> getGamesInFile(){
		DbManager dbManager = DbManager.getInstance(getContext());
		List<String> ids = FileHandler.getIds(getContext());
		List<Game> games = dbManager.getGames();
		List<Game> gamesInFile = new ArrayList<>();

		for (Game game : games){
			if(ids.contains(game.getId().toString())){
				gamesInFile.add(game);
			}
		}
		return gamesInFile;
	}
	public void logIds(List<String> list){
		for (String item : list) Log.d(TAG, "logList: " + item);
	}

}