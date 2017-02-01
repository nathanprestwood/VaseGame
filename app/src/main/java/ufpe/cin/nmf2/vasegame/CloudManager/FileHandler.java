package ufpe.cin.nmf2.vasegame.CloudManager;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nmf2 on 21/11/2016.
 * This class is used to read and write to the queued games' file. The games contained in the file
 * haven't been sent to the cloud.
 */

public abstract class FileHandler {
	private static final String FILENAME = "queued_games.txt";
	private static final String TAG = "FileHandler";
	public static void write(Context context, String gameId, boolean eraseContent){

		try{
			FileOutputStream fOutStream = context.openFileOutput(FILENAME,
					eraseContent ? Context.MODE_PRIVATE : Context.MODE_APPEND);
			//if erase content is true, the file will be cleared before writing
			OutputStreamWriter writer = new OutputStreamWriter(fOutStream);
			writer.write(gameId + "\n");
			writer.flush();
			writer.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	public static List<String> getIds(Context context){ // get ids stored in the file
		List<String> output = new ArrayList<>();

		try{
			FileInputStream fInStream = context.openFileInput(FILENAME);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fInStream));
			String line;
			while((line = reader.readLine()) != null){
				output.add(line);
			}
			fInStream.close();
		} catch (IOException e){
			e.printStackTrace();
		}

		return output;
	}
}
