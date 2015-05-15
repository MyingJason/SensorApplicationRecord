package com.example.sensorapplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

public class StoreSensorsData {
	
	
	private String filename = null;
	private String Header = "X-Acc,Y-Acc,Z-Acc,X-Ang,Y-Ang,Z-Ang,Speed,Volume,Frequency,Mode";
	File sdCardDir = Environment.getExternalStorageDirectory();
	File file;
	private String[] Cachedata = new String[4];
	private String mode = null;
	
	public StoreSensorsData(String filename){
		
		Time time = new Time();
		time.setToNow();
		this.filename = filename+time.format("%Y%m%d%H%M")+".csv";
		this.file = new File(this.sdCardDir+"/datarecord", this.filename);
		try {
			FileWriter writer = new FileWriter(file, true);
			writer.write(Header+"\n");
			writer.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		TimerTask task = new TimerTask(){  
  	      public void run() {  
  	    	  writedatatofile();
  	      }  
        };
        
        Timer timer = new Timer(true);
        timer.schedule(task,0,2 * 1000);
	}
	public void recordsensordata(String data, int label, String mode){
		

//		if(!file.getParentFile().exists()){
//            file.getParentFile().mkdirs();
//        }
		if(label == 0)
			this.mode = mode;
		Cachedata[label] = data;

	}
	private void writedatatofile(){
		
		
		try {
			FileWriter writer = new FileWriter(file, true);
			String tmp = Cachedata[0]+","+
					 Cachedata[1]+","+
					 Cachedata[2]+","+
					 Cachedata[3]+","+mode+"\n";
			
			writer.write(tmp.replaceAll("null", ""));
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	


}
