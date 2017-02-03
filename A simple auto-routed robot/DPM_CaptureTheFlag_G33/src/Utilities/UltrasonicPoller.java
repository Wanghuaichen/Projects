package Utilities;
import java.util.Arrays;

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


public class UltrasonicPoller extends Thread{
	private SampleProvider usSensor;
	private float[] usData;
	public double distance;
	public double lastdistance;
	public double[] distArray= new double[5];

	public UltrasonicPoller(SampleProvider usSensor, float[] usData) {
		this.usSensor = usSensor;
		this.usData = usData;
	}

	//  Sensors now return floats using a uniform protocol.
	//  Need to convert US result to an integer [0,255]

	public void run() {
		//int distance;
		while (true) {
			lastdistance = distance;
			usSensor.fetchSample(usData, 0);
			distance=(usData[0]*100.0);
<<<<<<< HEAD
			if (distance==255) 
=======
			if (distance>=255) 
>>>>>>> testingOdometryCOrrection
				distance = lastdistance;
			distance=median_filter(distance);

			try { Thread.sleep(30); } catch(Exception e){}		// Poor man's timed sampling
		}
	}

	private double median_filter(double dist){
		
		//put element in next null
		for(int i=0; i < distArray.length; i++) {
			
			if(distArray[i] == 0)
				distArray[i] = dist;
			else{
				if(i==distArray.length-1)
					replaceInFullArray(dist);
			}
		}
		
		double [] sortedArray = sortArray(distArray);

//		System.out.println("sortedArray: "+Arrays.toString(sortedArray));

		dist = sortedArray[sortedArray.length/2];

		return dist;
	}

	private void replaceInFullArray(double dist){

		for(int j=1;j<distArray.length;j++){
			distArray[j-1]=distArray[j];
		}
		distArray[distArray.length-1] = dist;
	}
	

	private double[] sortArray(double[] array){

		double[] newArray= new double[array.length];

		for(int i=0; i < array.length; i++){
			newArray[i]=array[i];
		}

		Arrays.sort(newArray);

		return newArray;
	}
}