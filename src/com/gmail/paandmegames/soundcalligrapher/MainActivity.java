package com.gmail.paandmegames.soundcalligrapher;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Highlight;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



/**
 * Author Oleg Kilimnik.
 */


public class MainActivity extends Activity {	

	 
	private static final String APP_DIR_NAME = "SCalligrapher";
	private static final String AUDIOFILE_EXT = ".pcm";
	private static final String CHARTFILE_EXT = ".png";
	private static final int RECORDER_SAMPLERATE = 8000;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static final int RECORDER_SOURCE = MediaRecorder.AudioSource.MIC;
	private static final int BUFFER_RATIO = 4;
	
	//protected static final String TAG = MainActivity.class.getSimpleName();
	
	private File app_dir;
	private AudioRecord recorder;
	private int rBufferSize;
	private LineChart chart;
	private LineData chartData;
	private String audiofile = "";
	private String chartfile = "";
	private String recordname = "";
	private Bitmap bitmap;

	
	
	/*
	 * Load OpenCV, create chart, check sdcard,
	 * create app directory,
	 * create recorder, set thread priority
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
    	
    	// load OpenCV
    	System.loadLibrary("opencv_java");
    	
    	
    	// create main view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupChart();
    
        
        // check if sdcard is writable
        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
        	((TextView)findViewById(R.id.result_text)).setText("Impossible to write audio, may be sdcard is not mounted");
        	return;
        }
        
        
        // create app directory
        app_dir = new File(Environment.getExternalStorageDirectory(), APP_DIR_NAME);

   	   	if(!app_dir.exists()){
   	   		app_dir.mkdirs();
   	   	}
   	   	
   	   	
   	   	// create recorder
   	   	rBufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS,
   	   			RECORDER_AUDIO_ENCODING)*BUFFER_RATIO;
   	   	recorder = new AudioRecord(RECORDER_SOURCE, RECORDER_SAMPLERATE, RECORDER_CHANNELS,
   	   		RECORDER_AUDIO_ENCODING, rBufferSize);
   	   	
   	   	
   	   	// set thread priority
   	   	Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
   	   	
    }
      
    
	/*
	 *  Chart global settings
	 */
    private void setupChart() {
    	
        chart = (LineChart) findViewById(R.id.chart_amplitude);
        chart.setDrawingCacheEnabled(true);

        // no description text
        chart.setDescription("");
        chart.setNoDataTextDescription("You need to provide data for the chart.");
        
        // enable value highlighting
        chart.setHighlightEnabled(true);
        
        // enable touch gestures
        chart.setTouchEnabled(true);
        
        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);
        
        // set an alternative background color
        chart.setBackgroundColor(Color.BLACK);
        
        chartData = new LineData();
        chartData.setValueTextColor(Color.WHITE);
        
        // add empty data
        chart.setData(chartData);
        
        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
        
        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();
        
        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(LegendForm.LINE);
        l.setTypeface(tf);
        l.setTextColor(Color.WHITE);

        XAxis xl = chart.getXAxis();
        xl.setTypeface(tf);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTypeface(tf);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaxValue(150f);
        leftAxis.setAxisMinValue(-150f);
        leftAxis.setStartAtZero(false);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
		
	}
    
    
    /*
     *  Chart dataset settings
     */
    private LineDataSet createSet(String name) {
        LineDataSet set = new LineDataSet(null, name);
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        set.setCircleSize(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(10f);
        return set;
    }


    
    /*
     *  On click record button
     */
    public void record(View view) {
    	
    	// check recorder state
    	if(recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) 
    		recorder.stop();
    	else {
    		recorder.startRecording();
    		((Button)findViewById(R.id.record_button)).setText(R.string.stop);
    		
 
    		/*
    		 * Save audiofile in async task
    		 */
    		new AsyncTask<Void, Void, Exception>() {
    	
    			@Override
    			protected Exception doInBackground(Void... params) {
	
    				recordname = app_dir + "/" + System.currentTimeMillis();
    				audiofile = recordname + AUDIOFILE_EXT;
    				byte data[] = new byte[rBufferSize];		
    				FileOutputStream fos = null;

    				try { fos = new FileOutputStream(audiofile); } 
    				catch (Exception e) { return e; }
    				
    				int read = 0;
    				while(recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){

    					read = recorder.read(data, 0, data.length);

    					try { fos.write(data, 0, read); } 
    					catch (IOException e) { return e; }
    				
    				}

    				try { fos.close(); } 
    				catch (IOException e) { return e; }
    				
    				return null;
    			}
    			
    			// if user stopped recording or any error occured
    	        @Override
    	        protected void onPostExecute(Exception result) {
    	        	
    	        	if (result != null) { //If any error occurs
    	        	
    	        		recorder.stop();
    	        		
    	        		((TextView)findViewById(R.id.result_text)).setText("" + result); //error output
    	        		((Button)findViewById(R.id.record_button)).setText(R.string.record_button);    
    	        		
    	        	} else {  //Success
    	        		
    	        		((TextView)findViewById(R.id.result_text)).setText(audiofile + " is written.");
    	        		((Button)findViewById(R.id.record_button)).setText(R.string.record_button);

    	        	}
    	        
    	        }
        		
        	}.execute();
  
       
        	
        	/*
        	 * Draw chart in async task
        	 */
    		new AsyncTask<Void, Void, Exception>() {
 
    			@Override
    			protected Exception doInBackground(Void... params) {
	
    				chartfile = recordname + CHARTFILE_EXT;
    		    	int read = 0;
    		    	FileInputStream fis = null;
    		    	byte[] fileData = new byte[1024]; 
    				try {
    					fis = new FileInputStream(audiofile);
    				} catch (FileNotFoundException e) {
    					return e;
    				}


    		        if (chartData != null) {

    		            LineDataSet set = chartData.getDataSetByIndex(0);
    		            // set.addEntry(...); // can be called as well

    		            if (set == null) {
    		                set = createSet("Amplitude");
    		                chartData.addDataSet(set);
    		            }
    		            try {  		    			
    		    			while((read = fis.read(fileData)) != -1){
    		    				for(int i=0;i<read;i++) {
    		    					chartData.addXValue("");
    		    					chartData.addEntry(new Entry((float)fileData[i], set.getEntryCount()), 0);
    		    				}
    		    			}
    		    			fis.close();
    		    		} catch (IOException e) {
    		    			return e;
    		    		}

    				
    		        }
    		        return null;
    			}
    			
    			
    	        @Override
    	        protected void onPostExecute(Exception result) {
    	        	
    	        	if (result != null) { //If any error occurs
    	        	
    	    		
    	        	} else {  //Success
    	                // let the chart know it's data has changed
    	                chart.notifyDataSetChanged();

    	                // limit the number of visible entries
    	                chart.setVisibleXRange(250);
    	                // mChart.setVisibleYRange(30, AxisDependency.LEFT);

    	                // move to the latest entry
    	                chart.moveViewToX(chartData.getXValCount() - 7);
    	                bitmap = Bitmap.createBitmap(chart.getDrawingCache());
    	                
    	                try {
							bitmap.compress(CompressFormat.PNG, 100, new FileOutputStream(chartfile));
						} catch (FileNotFoundException e) {
							
						}
    	                // this automatically refreshes the chart (calls invalidate())
    	                // mChart.moveViewTo(data.getXValCount()-7, 55f,
    	                // AxisDependency.LEFT);

    	                // redraw the chart
    	                 //chart.invalidate();
    	        	}
    	        
    	        }
        		
        	}.execute();
  
    	}
    	
    }
    
    
    /*
     *  On click play button
     */
    public void play(View view) {
    	
    	new AsyncTask<Void, Void, Exception>() {

			@Override
			protected Exception doInBackground(Void... params) {
				int read = 0;
				byte[] data = new byte[1024];
				
				FileInputStream fis = null;
				
				int bufferSize = AudioTrack.getMinBufferSize(RECORDER_SAMPLERATE,
						AudioFormat.CHANNEL_OUT_MONO, RECORDER_AUDIO_ENCODING);
				AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, RECORDER_SAMPLERATE,
						AudioFormat.CHANNEL_OUT_MONO, RECORDER_AUDIO_ENCODING, bufferSize,
						AudioTrack.MODE_STREAM);
				track.play();
				try {
					fis = new FileInputStream(audiofile);
				} catch (FileNotFoundException e) {
					return e;
				}
	        
				try {
					while((read = fis.read(data)) != -1){
						track.write(data, 0, read); 
					}
				} catch (IOException e) {
					return e;
				}
				track.stop();
				track.release();
				return null;
			}
			
	        @Override
	        protected void onPostExecute(Exception result) {
	        	
	        	if (result != null) { //If any error occurs
	        	
	    		
	        	} else {  //Success
	   
	        	}
	        
	        }
    		
    	}.execute();
    
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
        /*if (id == R.id.action_settings) {
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }



    
}
