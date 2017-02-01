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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ufpe.cin.nmf2.vasegame.CloudManager.CloudManager;
import ufpe.cin.nmf2.vasegame.CloudManager.FileHandler;
import ufpe.cin.nmf2.vasegame.database.DbManager;

public class HighScoresFragment extends Fragment {
	private static final String TAG = "HighScoresFragment";
	private RecyclerView mHardGameRecyclerView;
	private RecyclerView mEasyGameRecyclerView;
	private Button mSyncButton;
	private TextView mUsernameTextView;
	private GameAdapter mHardAdapter;
	private GameAdapter mEasyAdapter;
	private static String mUsername;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.high_scores_fragment, container, false);

		mHardGameRecyclerView = (RecyclerView) view.findViewById(R.id.hard_game_recycler_view);
		mHardGameRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

		mEasyGameRecyclerView = (RecyclerView) view.findViewById(R.id.easy_game_recycler_view);
		mEasyGameRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

		mUsernameTextView = (TextView) view.findViewById(R.id.high_score_username_text_view);
		if(mUsername != null){
			mUsernameTextView.setText(mUsername);
		} else {
			mUsernameTextView.setText(Game.ANONYMOUS);
		}

		mSyncButton = (Button) view.findViewById(R.id.high_score_sync_button);
		mSyncButton.setText(R.string.sync);

		mSyncButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CloudManager cloudManager = new CloudManager(getContext());
				//ArrayList<Game> games = cloudManager.getGames(MenuFragment.mUsername);
				//updateUI();
				cloudManager.sendGames(getGamesInFile());
			}
		});

		updateUI();

		return view;
	}
	private class GameHolder extends RecyclerView.ViewHolder{
		private TextView mTitleTextView;
		private TextView mDateTextView;

		private Game mGame;

		public GameHolder(View itemView) {
			super(itemView);
			mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_game_title_text_view);
			mDateTextView = (TextView) itemView.findViewById(R.id.list_item_game_date_text_view);
		}
		public void bindGame(Game game){
			mGame = game;
			mTitleTextView.setText(mGame.getTime());
			mDateTextView.setText(mGame.getDate());
		}
	}
	private class GameAdapter extends RecyclerView.Adapter<GameHolder> {
		private SortedList<Game> mGames;
		public GameAdapter(List<Game> games) {
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
							oldItem.getDate().equals(newItem.getDate()) &&
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
			Log.d("GAdapter:getitemcount()", "mGames.size: " + mGames.size());
			return mGames.size();
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		updateUI();
	}

	private void updateUI() {
		DbManager dbManager = new DbManager(getActivity().getApplicationContext());
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
		//DbManager.closeDb();
	}
	private List<Game> getGamesInFile(){
		DbManager dbManager = new DbManager(getActivity().getApplicationContext());
		List<String> ids = FileHandler.getIds(getContext());
		logList(ids);//show list in debug, delete later
		List<Game> games = dbManager.getGames();
		List<Game> gamesInFile = new ArrayList<>();

		for (Game game : games){
			if(ids.contains(game.getId().toString())){
				gamesInFile.add(game);
			}
		}
		//DbManager.closeDb();
		return gamesInFile;
	}
	public void logList(List<String> list){
		for (String item : list) Log.d(TAG, "logList: " + item);
	}
	public static void setUsername(String username){
		String[] shortUsername = username.split("@");
		mUsername = shortUsername[0];
	}
}