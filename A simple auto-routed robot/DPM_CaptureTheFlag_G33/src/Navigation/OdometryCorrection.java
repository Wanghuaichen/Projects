/* 
 * OdometryCorrection.java
 * 
 * @author Samuel Roux & The-Luan Thran
 * Created Oct 3 2015 by Samuel Roux
 * Last Modification Oct 4 by Samuel Roux
 *
 */

package Navigation;

import CaptureTheFlag.CaptureTheFlag;
import Utilities.LightPoller;
import Utilities.Log;
import lejos.hardware.*;

/**
 *
 *Class allows robot to correct the odometer value based on it's absolute position, if needed, while moving.
 *
 */
public class OdometryCorrection extends Thread {
	
	//Objects
	private Odometer odometer;
	private LightPoller lightPoller;
	
	//Constants
	private static final long CORRECTION_PERIOD = 10;
	private static double offset=CaptureTheFlag.LSOFFSET;
	
	//int
	public static int moduloValue;
	public int counterX=0;
	public int counterY=0;


	// constructor
	public OdometryCorrection(Odometer odometer,LightPoller lightPoller) {
		this.odometer = odometer;
		this.lightPoller = lightPoller;
	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;

		double theta;
		double x;
		double y;
		float intensity;

//		EV3ColorSensor colorSensor= CaptureTheFlag.getDownColorSensor();
//
//		SensorMode mode =colorSensor.getRedMode();			// colorSensor is the instance
//		float[] sample = new float[mode.sampleSize()];		// colorData is the buffer in which data are returned
//		mode.fetchSample(sample, 0);
//		float intensity=sample[0];

		// some objects that need to be instantiated


		while (true) {
			correctionStart = System.currentTimeMillis();

//			mode.fetchSample(sample, 0);
//			intensity=sample[0];
			
			intensity=getLightIntensity();

			x=this.odometer.getX(); 
			y=this.odometer.getY();
			theta=this.odometer.getTheta();

			if(Math.abs(theta)<=-1.60){

			}

			Log.log(Log.Sender.odometer,"****Intensity:"+intensity);
			
			//>0.01 only to filter values. (Too far (null)). <0.30 to catch black line
			if(intensity<0.30 && intensity>0.01)	
			{	
				Log.log(Log.Sender.odometer,"****CORRECTING**** "+String.format("x: %f, y: %f, t: %f",
						odometer.getX(), odometer.getY(), odometer.getTheta()));
				
				if( x%30 <2){
					odometer.setX(round(x));
				}
				if(y%30 < 2){
					odometer.setY(round(y));
				}

				Sound.beep();
				Log.log(Log.Sender.odometer,"****CORRECTED**** "+String.format("x: %f, y: %f, t: %f",
						odometer.getX(), odometer.getY(), odometer.getTheta()));
			}

			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}


	public double round(double num){
		double temp = num%30;
		if(temp<15){
			return num-temp;
		}
		else{
			return num +30 -temp;
		}
	}
	public float getLightIntensity(){
		return lightPoller.intensity;
	}
}