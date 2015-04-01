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
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioRecord;
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


public class MainActivity extends Activity implements
OnChartValueSelectedListener {	

	 
	private static final String APP_DIR_NAME = "SCalligrapher";
	private static final String AUDIOFILE_EXT = ".pcm";
	private static final int RECORDER_SAMPLERATE = 44100;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static final int RECORDER_SOURCE = MediaRecorder.AudioSource.MIC;
	private static final int BUFFER_RATIO = 4;
	
	//protected static final String TAG = MainActivity.class.getSimpleName();
	
	private File app_dir;
	private AudioRecord recorder;
	private int bufferSize;
	private LineChart chart;
	private String file = "";
	
	
	/*
	 * Load OpenCV, create chart, check sdcard,
	 * create app directory,
	 * create recorder, set thread priority
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
    	
    	// LOAD OPENCV
    	System.loadLibrary("opencv_java");
    	
    	
    	// CREATE MAIN VIEW
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        // CREATE CHART
        chart = (LineChart) findViewById(R.id.chart_amplitude);
        chart.setOnChartValueSelectedListener(this);

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
        chart.setBackgroundColor(Color.LTGRAY);
        
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        
        // add empty data
        chart.setData(data);
        
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
        leftAxis.setAxisMaxValue(120f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
        
        
        // CHECK SDCARD
        if(!isExternalStorageWritable()) {
        	((TextView)findViewById(R.id.result_text)).setText("Impossible to write audio, may be sdcard is not mounted");
        	return;
        }
        
        
        // CREATE APP DIRECTORY
        app_dir = new File(Environment.getExternalStorageDirectory(), APP_DIR_NAME);

   	   	if(!app_dir.exists()){
   	   		app_dir.mkdirs();
   	   	}
   	   	
   	   	
   	   	// CREATE RECORDER
   	   	bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS,
   	   			RECORDER_AUDIO_ENCODING)*BUFFER_RATIO;
   	   	recorder = new AudioRecord(RECORDER_SOURCE, RECORDER_SAMPLERATE, RECORDER_CHANNELS,
   	   		RECORDER_AUDIO_ENCODING, bufferSize);
   	   	
   	   	
   	   	// SET THREAD PRIORITY
   	   	Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
   	   	
    }
    
    
    
    
	
    // CHECK SDCARD
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

    
    //ON CLICK RECORD BUTTON
    public void record(View view) {
    	
    	// check recorder state
    	if(recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) 
    		recorder.stop();
    	else {
    		recorder.startRecording();
    		((Button)findViewById(R.id.record_button)).setText(R.string.stop);
    		
 
    		//Save audiofile in async task
    		new AsyncTask<Void, Void, Exception>() {
    	
    			@Override
    			protected Exception doInBackground(Void... params) {
	
    				file = app_dir + "/" + System.currentTimeMillis() + AUDIOFILE_EXT;
    				byte data[] = new byte[bufferSize];		
    				FileOutputStream fos = null;

    				try { fos = new FileOutputStream(file); } 
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
    	        		
    	        		((TextView)findViewById(R.id.result_text)).setText(file + " is written.");
    	        		((Button)findViewById(R.id.record_button)).setText(R.string.record_button);

    	        	}
    	        
    	        }
        		
        	}.execute();
  
        	
        	//Draw chart in async task
    		new AsyncTask<Void, Void, Exception>() {
 
    			@Override
    			protected Exception doInBackground(Void... params) {
	
    		    	int read = 0;
    		    	FileInputStream fis = null;
    		    	byte[] fileData = new byte[1024]; 
    				try {
    					fis = new FileInputStream(file);
    				} catch (FileNotFoundException e1) {
    				
    				}

    		        LineData data = chart.getData();

    		        if (data != null) {

    		            LineDataSet set = data.getDataSetByIndex(0);
    		            // set.addEntry(...); // can be called as well

    		            if (set == null) {
    		                set = createSet();
    		                data.addDataSet(set);
    		            }

    		            // add a new x-value first
    		           // data.addXValue(mMonths[data.getXValCount() % 12] + " "
    		             //       + (year + data.getXValCount() / 12));
    		            //data.addEntry(new Entry((float) (Math.random() * 40) + 40f, set.getEntryCount()), 0);
    		    		try {
    		    			while((read = fis.read(fileData)) != -1){
    		    				for(int i=0;i<read;i++) {
    		    					data.addXValue(String.valueOf(""));
    		    					data.addEntry(new Entry((float)fileData[i], set.getEntryCount()), 0);
    		    				}
    		    			}
    		    		} catch (IOException e) {
    		    			//Instances.getInstance().resetPlayback();
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
    	               // chart.setVisibleXRange(6);
    	                // mChart.setVisibleYRange(30, AxisDependency.LEFT);

    	                // move to the latest entry
    	                //chart.moveViewToX(data.getXValCount() - 7);

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
    
    

    
    
    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
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

    
	@Override
	public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onNothingSelected() {
		// TODO Auto-generated method stub
		
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
        int id = item.getItemId();
        /*if (id == R.id.action_settings) {
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }



    
}
