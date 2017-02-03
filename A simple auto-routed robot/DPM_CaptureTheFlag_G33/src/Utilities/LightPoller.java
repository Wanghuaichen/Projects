package Utilities;
import lejos.robotics.SampleProvider;

//
//  Control of the wall follower is applied periodically by the 
//  UltrasonicPoller thread.  The while loop at the bottom executes
//  in a loop.  Assuming that the us.fetchSample, and cont.processUSData
//  methods operate in about 20mS, and that the thread sleeps for
//  50 mS at the end of each loop, then one cycle through the loop
//  is approximately 70 mS.  This corresponds to a sampling rate
//  of 1/70mS or about 14 Hz.
//


/**
 *
 *Class used to polled the light sensor
 *
 */
public class LightPoller extends Thread{
	private SampleProvider colorSensor;
	private float[] colorData;
	public float intensity;
	public float[] RGB = {0,0,0};
	private int[] colorIDarray = new int [3];
	
	private static float [][] rps = {{29.2f, 42.0f,28.8f}, {80.8f,11.5f, 7.7f}, {58.8f, 
		41.3f, 0f}, { 37.5f, 38.8f, 23.7f}, {15.1f, 38.4f,  46.6f}}; 
	// 1.light blue, 2.red, 3.yellow, 4.white, 5.dark blue // Nothing
	
	
	
	
	public LightPoller(SampleProvider colorSensor, float[] colorData) {
		this.colorSensor = colorSensor;
		this.colorData = colorData;
	}
	
	public void run() {
		//int distance;
		while (true) {
			colorSensor.fetchSample(colorData, 0);
			
			if(colorData.length==1){
				//used for red mode
				intensity=(colorData[0]);
			}
			else{
				for(int i =0;i<colorData.length;i++){
					RGB[i]=colorData[i];
				}
			}
			try { Thread.sleep(50); } catch(Exception e){}		// Poor man's timed sampling
		}
	}
	
public int getColorID() {
		
		int closestID=0;
		float distance;
		float closestD = 0;
		float [] RGBratio = getRGBratio();
		
		RGBratio = getRGBratio();
		if ( RGBratio[0] == 100f || RGBratio[1] ==100f || RGBratio[2] == 100f){
			Log.log(Log.Sender.avoidance, "Invalid:  0,100,0");
			return 0;
		}
		//Log.log(Log.Sender.avoidance, "RGBratio: "+ Arrays.toString(RGBratio));
		
		for(int i = 0; i < rps.length; i++){
			RGBratio = getRGBratio();
			distance = (float) Math.sqrt(Math.pow(RGBratio[0] - rps[i][0], 2) + Math.pow(RGBratio[1] - rps[i][1], 2) + Math.pow(RGBratio[2] - rps[i][2], 2));
			if(i==0) {
				closestD =distance;
				closestID = i;
			}
			if(distance <= closestD) {
				closestD=distance;
				closestID = i;
			}
		}
		return closestID + 1;
	}
	
	public float [] getRGBratio() {
		float RGBratio [] = new float [3];
		float [] currentRGB = RGB;
		RGBratio[0] = (currentRGB[0] / (currentRGB[0]+currentRGB[1]+currentRGB[2])) * 100;
		RGBratio[1] = (currentRGB[1] / (currentRGB[0]+currentRGB[1]+currentRGB[2])) * 100;
		RGBratio[2] = (currentRGB[2] / (currentRGB[0]+currentRGB[1]+currentRGB[2])) * 100;
		if ((currentRGB[0]+currentRGB[1]+currentRGB[2]) == 0)
			for (int i = 0; i < 3; i++){
					RGBratio[i] = 100;
				}
		return RGBratio;
	}

}
