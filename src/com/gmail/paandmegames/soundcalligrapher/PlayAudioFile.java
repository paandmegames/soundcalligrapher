package com.gmail.paandmegames.soundcalligrapher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.os.Environment;
//import android.util.Log;

public class PlayAudioFile implements Runnable {
	
	//private static final String TAG = PlayAudioFile.class.getSimpleName();
	private boolean reverseMode = false;

	public PlayAudioFile(boolean mode) {
		this.reverseMode = mode;
	}


	@Override
	public void run() {
		
/*		while(!Instances.getInstance().isPlaying) {
			
		}
		int read = 0;
		byte[] data = new byte[Constants.BUFFER_SIZE];
		
		//обратное воспроизведение
		if(reverseMode) {
			RandomAccessFile raf = null;
			File file = new File(getFilename());
			
			try {
				raf = new RandomAccessFile(file, "r");
				
			} catch (FileNotFoundException e2) {
				Instances.getInstance().resetPlayback();
			}	
		
			for(int i=1; i*Constants.BUFFER_SIZE<file.length(); i++) {
				try {   
					raf.seek(file.length() - i*Constants.BUFFER_SIZE-1);
					read = raf.read(data);
				    byte[] rData = reverse(data);
			   	    Instances.getInstance().track.write(rData, 0, read); 
			 	 
	    	    } catch (IOException e) {
	    	        	Instances.getInstance().resetPlayback();
	    	    }
			}
			
		//прямое воспроизведение
		} else {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(getFilename());
			} catch (FileNotFoundException e1) {
				Instances.getInstance().resetPlayback();
			}
        
			try {
				while((read = fis.read(data)) != -1 && Instances.getInstance().isPlaying){
					Instances.getInstance().track.write(data, 0, read); 
					//Log.d(TAG, String.valueOf(read));
				}
			} catch (IOException e) {
				Instances.getInstance().resetPlayback();
			}
		}
		Instances.getInstance().resetPlayback();
		*/
	}

	//переворачиваем массив аудиоданных
	/*private byte[] reverse(byte[] data) {
		
		byte[] rData = new byte[Constants.BUFFER_SIZE];
		for(int i = 0; i<data.length; i++) {
			rData[data.length-1-i] = data[i];
		}
		return rData;
	}

	//путь к файлу
	private String getFilename(){
	   	   String filepath = Environment.getExternalStorageDirectory().getPath();
	   	   File file = new File(filepath, Constants.AUDIO_RECORDER_FOLDER);
	   	   return (file.getAbsolutePath() + "/" + Constants.AUDIO_RECORDER_FILE);
	}
*/
}
