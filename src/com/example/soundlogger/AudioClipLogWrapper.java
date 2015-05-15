/*
 * Copyright 2012 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.soundlogger;


import com.example.sensorapplication.SensorDisplayFragment;
import com.example.sensorapplication.StoreSensorsData;

import android.app.Activity;
import android.widget.TextView;

/**
 * Helps log information during {@link ClapperPlay}
 * @author Greg Milette &#60;<a
 *         href="mailto:gregorym@gmail.com">gregorym@gmail.com</a>&#62;
 */
public class AudioClipLogWrapper implements AudioClipListener{
		
    private TextView log;
    private String mode;

    private Activity context;
    
    
//    StoreSensorsData sensordata = new StoreSensorsData("RECORD");

    public AudioClipLogWrapper(TextView log, Activity context, String mode)
    {
        this.log = log;
        this.context = context;
        this.mode = mode;
    }

    @Override
    public boolean heard(short[] audioData, int sampleRate)
    {
        final double zero = ZeroCrossing.calculate(sampleRate, audioData);
        final double volume = AudioUtil.rootMeanSquared(audioData);
        final StringBuilder message = new StringBuilder();
        
        message.append((int)volume).append(",").append((int)zero);


        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                AudioTaskUtil.appendToStartOfLog(log, message.toString());
                SensorDisplayFragment.sensordata.recordsensordata(message.toString(),3,mode);
            }
        });
        return false;
        
    }
}
