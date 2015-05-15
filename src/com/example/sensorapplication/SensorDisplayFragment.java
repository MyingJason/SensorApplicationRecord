package com.example.sensorapplication;

import com.example.soundlogger.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class SensorDisplayFragment extends Activity implements SensorEventListener, LocationListener
{
    private static final String TAG = "SensorDisplayFragment";
    private static final String THETA = "\u0398";
    private static final String ACCELERATION_UNITS = "m/s\u00B2";
	private static final float ALPHA = 0.8f;
    
    private SensorManager sensorManager;
    private Sensor sensor;

    private TextView accuracy;
    private TextView timestampLabel;
    private TextView timestamp;
    private TextView timestampUnits;
    private TextView dataLabel;
    private TextView dataUnits;
    private TextView xAxis;	
    private TextView xAxisLabel;
    private TextView yAxis;
    private TextView yAxisLabel;
    private TextView zAxis;
    private TextView zAxisLabel;
    private TextView singleValue;

    
    private RadioButton tram;
    private RadioButton train;
    private RadioButton bus;
    private RadioButton car;
    private RadioButton bike;
    private RadioButton walk;
    
	private boolean recordacceleroter = false;
	private boolean recordgyroscope = false;
	private boolean recordspeed = false;

    private double lastx;
    private double lasty;
    private double lastz;
	protected String mode;
	
	WakeLock wakelock = null;

	
	//CurrentLocation
	
	protected LocationManager currentloca;

	private LocationManager locationManager;
	private TextView latitudeValue;
	private TextView longitudeValue;
	private TextView providerValue;
	private TextView accuracyValue;
	private TextView timeToFixValue;
	private TextView enabledProvidersValue;
	private TextView speedValue; 
	private long uptimeAtResume;
	private List<String> enabledProviders;
	
	//Restore Data
	private float[] hp_acc;
	private float[] hp_ang;
    public static StoreSensorsData sensordata = new StoreSensorsData("RECORD");
    int Acc = 0;
    int Ang = 1;
    int Speed = 2;
    int Sound = 3;
    private boolean recordflag = true;
	
    //Sound Data
    private TextView log;
    private RecordAudioTask recordAudioTask; 
    
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		acquirewakelock();
		
		
		setContentView(R.layout.sensor_view);
        
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        

        accuracy = (TextView) findViewById(R.id.accuracy);
        timestampLabel = (TextView) findViewById(R.id.timestampLabel);
        timestamp = (TextView) findViewById(R.id.timestamp);
        timestampUnits = (TextView) findViewById(R.id.timestampUnits);
        dataLabel = (TextView) findViewById(R.id.dataLabel);
        dataUnits = (TextView) findViewById(R.id.dataUnits);
        xAxis = (TextView) findViewById(R.id.xAxis);
        xAxisLabel = (TextView) findViewById(R.id.xAxisLabel);
        yAxis = (TextView) findViewById(R.id.yAxis);
        yAxisLabel = (TextView) findViewById(R.id.yAxisLabel);
        zAxis = (TextView) findViewById(R.id.zAxis);
        zAxisLabel = (TextView) findViewById(R.id.zAxisLabel);
        singleValue = (TextView) findViewById(R.id.singleValue);

        tram = (RadioButton) findViewById(R.id.tram);
        train = (RadioButton) findViewById(R.id.train);
        bus = (RadioButton) findViewById(R.id.bus);
        car = (RadioButton) findViewById(R.id.car);
        bike = (RadioButton) findViewById(R.id.bike);
        walk = (RadioButton) findViewById(R.id.walk);
        
        
        
        //CurrentLocation
        
        latitudeValue = (TextView) findViewById(R.id.latitudeValue);
		longitudeValue = (TextView) findViewById(R.id.longitudeValue);
		providerValue = (TextView) findViewById(R.id.providerValue);
		accuracyValue = (TextView) findViewById(R.id.accuracyValue);
		timeToFixValue = (TextView) findViewById(R.id.timeToFixValue);
		enabledProvidersValue = (TextView) findViewById(R.id.enabledProvidersValue);
		speedValue = (TextView) findViewById(R.id.speedValue);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
        if(null != location)
        	this.onLocationChanged(location);
        
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 10, this);

		
        
        
        
        findViewById(R.id.delayFastest).setOnClickListener(new OnClickListener()
        {
        	@Override
            public void onClick(View v)
            {
                sensorManager.unregisterListener(SensorDisplayFragment.this);
                sensorManager.registerListener(SensorDisplayFragment.this,
                        sensorManager.getDefaultSensor(sensor.TYPE_LINEAR_ACCELERATION), 
                        SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(SensorDisplayFragment.this,
                		sensorManager.getDefaultSensor(sensor.TYPE_GYROSCOPE),
                		SensorManager.SENSOR_DELAY_FASTEST);
                
                startTask(createAudioLogger(), "Audio Logger");
            }
        });
        
        //Sound Sensor Record
        
        log = (TextView)findViewById(R.id.tv_resultlog);
        
        
        tram.setOnCheckedChangeListener(listener);  
        train.setOnCheckedChangeListener(listener);  
        bus.setOnCheckedChangeListener(listener);  
        car.setOnCheckedChangeListener(listener);  
        bike.setOnCheckedChangeListener(listener); 
        walk.setOnCheckedChangeListener(listener);
        
        TimerTask task = new TimerTask(){  
    	      public void run() {  
    	    	  recordacceleroter = true;
    	    	  recordgyroscope = true;
    	    	  recordspeed = true;
    	      
    	      }  
          };
          
          Timer timer = new Timer(true);
          timer.schedule(task, 0,1 * 1000);
          
         hp_acc = new float[3];
         hp_ang = new float[3];

    }

	private OnCheckedChangeListener listener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			// TODO Auto-generated method stub
			switch (arg0.getId()) {
			case R.id.tram:
				if (arg1)
					mode = "tram";
				break;
			case R.id.train:
				if (arg1)
					mode = "train";
				break;
			case R.id.bus:
				if (arg1)
					mode = "bus";
				break;
			case R.id.car:
				if (arg1)
					mode = "car";
				break;
			case R.id.bike:
				if (arg1)
					mode = "bike";
				break;
			case R.id.walk:
				if (arg1)
					mode = "walk";
				break;
			}

		}
	};
	
    
   
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        
    	if(accuracy != SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
    		recordflag = false;

    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
    	
        onAccuracyChanged(event.sensor, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        if(recordflag){
        
	        timestampLabel.setVisibility(View.VISIBLE);
	        timestamp.setVisibility(View.VISIBLE);
	        timestamp.setText(String.valueOf(event.timestamp));
	        timestampUnits.setVisibility(View.VISIBLE);
	        
	        switch (event.sensor.getType())
	        {
	            case Sensor.TYPE_LINEAR_ACCELERATION:{
	                showEventData("Acceleration - gravity on axis",
	                        ACCELERATION_UNITS,
	                        event.values[0],
	                        event.values[1],
	                        event.values[2]);
	                if (this.recordacceleroter){ 
	                	float [] tmp = event.values.clone();
	                	tmp = highpass(tmp[0], tmp[1], tmp[2], Acc);
	                    sensordata.recordsensordata(tmp[0]+","+tmp[1]+","+tmp[2],Acc,mode);
	                    this.recordacceleroter = false;
	                    }
	                break;
	                }
	                
	            
	            case Sensor.TYPE_GYROSCOPE:{
	                showEventData("Angular speed around axis",
	                        "radians/sec",
	                        event.values[0],
	                        event.values[1],
	                        event.values[2]);
	                if (this.recordgyroscope){
	                	delay(2);
	                	float [] tmp = event.values.clone();
	                	tmp = highpass(tmp[0], tmp[1], tmp[2], Ang);
	                	sensordata.recordsensordata(tmp[0]+","+tmp[1]+","+tmp[2],Ang,mode);
	                    this.recordgyroscope = false;
	                    }
	                break;
	                }
	                
	
	        }
        }
    }
    
    private void showEventData(String label, String units, float x, float y, float z)
    {
    	
        dataLabel.setVisibility(View.VISIBLE);
        dataLabel.setText(label);
        
        if (units == null)
        {
            dataUnits.setVisibility(View.GONE);
        }
        else
        {
            dataUnits.setVisibility(View.VISIBLE);
            dataUnits.setText("(" + units + "):");
        }
        
        DecimalFormat formater=new DecimalFormat("#0.##");
        singleValue.setVisibility(View.GONE);
        
        xAxisLabel.setVisibility(View.VISIBLE);
        xAxisLabel.setText(R.string.xAxisLabel);
        xAxis.setVisibility(View.VISIBLE);
        xAxis.setText(String.valueOf(formater.format(x)));
        
        yAxisLabel.setVisibility(View.VISIBLE);
        yAxisLabel.setText(R.string.yAxisLabel);
        yAxis.setVisibility(View.VISIBLE);
        yAxis.setText(String.valueOf(formater.format(y)));
        
        zAxisLabel.setVisibility(View.VISIBLE);
        zAxisLabel.setText(R.string.zAxisLabel);
        zAxis.setVisibility(View.VISIBLE);
        zAxis.setText(String.valueOf(formater.format(z)));
//        delay(5);
    }
    



    /**
     * @see android.support.v4.app.Fragment#onPause()
     */
    @Override
    public void onPause()
    {
        super.onPause();
        
        if (BuildConfig.DEBUG)
        {
            Log.d(TAG, "onPause");
            Log.d(TAG, "Unregistering listener");
        }
        
//        sensorManager.unregisterListener(this);
    }
    
	public void delay(int t){
		try {
			Thread.sleep(t*100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    public boolean checkupdate(double x, double y, double z){


    if (Math.abs(lastx +lasty +lastz -x -y -z) >1) {
        lastx = x;
        lasty = y;
        lastz = z;
        return true;
    }
    else
        return false;
    }
    
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		releasewakelock();
		stopAll();
		sensorManager.unregisterListener(this);
		locationManager.removeUpdates(this);
		Log.i("i", "  MyFragment onDestroy() ");
	}
    
    private void acquirewakelock()
    {
 
    	if (null == wakelock)  
        {  
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);  
            wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "PostLocationService");  
            if (null != wakelock)  
            {  
                wakelock.acquire();  
            }  
        }  
    	
    }
  //释放设备电源锁  
    private void releasewakelock()  
    {  
        if (null != wakelock)  
        {  
            wakelock.release();  
            wakelock = null;  
        }  
    }

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if(null!= location){

			latitudeValue.setText(String.valueOf(location.getLatitude()));
			longitudeValue.setText(String.valueOf(location.getLongitude()));
			providerValue.setText(String.valueOf(location.getProvider()));
			accuracyValue.setText(String.valueOf(location.getAccuracy()));
			speedValue.setText(String.valueOf(location.getSpeed()));
	
			long timeToFix = SystemClock.uptimeMillis() - uptimeAtResume;
			
			timeToFixValue.setText(String.valueOf(timeToFix/1000));
	
	
			findViewById(R.id.timeToFixUnits).setVisibility(View.VISIBLE);
	        findViewById(R.id.accuracyUnits).setVisibility(View.VISIBLE);
	        
	        if(this.recordspeed)
	        	delay(4);
	        	sensordata.recordsensordata(location.getSpeed()+"",Speed,mode);
	        	this.recordspeed = false;
		
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}  
	
	private void stopAll()
    {


        Log.d(TAG, "stop record audio");
        shutDownTaskIfNecessary(recordAudioTask);
    }
    
    private void shutDownTaskIfNecessary(final AsyncTask task)
    {
        if ( (task != null) && (!task.isCancelled()))
        {
            if ((task.getStatus().equals(AsyncTask.Status.RUNNING))
                    || (task.getStatus()
                            .equals(AsyncTask.Status.PENDING)))
            {
                Log.d(TAG, "CANCEL " + task.getClass().getSimpleName());
                task.cancel(true);
            }
            else
            {
                Log.d(TAG, "task not running");
            }
        }
    }

    private void startTask(AudioClipListener detector, String name)
    {
        stopAll();
        
        recordAudioTask = new RecordAudioTask(log, name);
        //wrap the detector to show some output
        List<AudioClipListener> observers = new ArrayList<AudioClipListener>();
        observers.add(new AudioClipLogWrapper(log, this, mode));
        OneDetectorManyObservers wrapped = 
            new OneDetectorManyObservers(detector, observers);
        recordAudioTask.execute(wrapped);
    }



    private AudioClipListener createAudioLogger()
    {
        AudioClipListener audioLogger = new AudioClipListener()
        {
            @Override
            public boolean heard(short[] audioData, int sampleRate)
            {
                if (audioData == null || audioData.length == 0)
                {
                    return true;
                }
                
                // returning false means the recording won't be stopped
                // users have to manually stop it via the stop button
                return false;
            }
        };
        
        return audioLogger;
    }
    private float[] highpass(float x, float y, float z, int type)
    {
        float[] filteredValues = new float[3];
        
        if(type == Acc)
        {
			hp_acc[0] = ALPHA * hp_acc[0] + (1 - ALPHA) * x;
	        hp_acc[1] = ALPHA * hp_acc[1] + (1 - ALPHA) * y;
	        hp_acc[2] = ALPHA * hp_acc[2] + (1 - ALPHA) * z;
	
	        filteredValues[0] = (float)(Math.round((x - hp_acc[0])*100))/100;
	        filteredValues[1] = (float)(Math.round((y - hp_acc[1])*100))/100;
	        filteredValues[2] = (float)(Math.round((z - hp_acc[2])*100))/100;
        }
        else if(type == Ang)
        {
        	
        	hp_ang[0] = ALPHA * hp_ang[0] + (1 - ALPHA) * x;
	        hp_ang[1] = ALPHA * hp_ang[1] + (1 - ALPHA) * y;
	        hp_ang[2] = ALPHA * hp_ang[2] + (1 - ALPHA) * z;
	
	        filteredValues[0] = (float)(Math.round((x - hp_ang[0])*100))/100;
	        filteredValues[1] = (float)(Math.round((y - hp_ang[1])*100))/100;
	        filteredValues[2] = (float)(Math.round((z - hp_ang[2])*100))/100;
        	
        }
        
        return filteredValues;
    }

}
