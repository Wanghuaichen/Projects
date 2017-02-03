
package CaptureTheFlag;

import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Stack;

import Localization.USLocalizer;
import Navigation.Avoidance;
import Navigation.Navigation;
import Navigation.ObjectDetection;
import Navigation.ObjectSearch;
import Navigation.Odometer;
import Navigation.OdometryCorrection;
import Utilities.LCDInfo;
import Utilities.LightPoller;
import Utilities.UltrasonicPoller;
import Utilities.robotControls;
import Utilities.Log;


import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;
import wifi.ProcessData;
import wifi.Transmission;
import wifi.WifiStarter;


/**
 * This is the main class of the project. Its instantiating all objects variables and contains the main()
 * 
 *
 */
public class CaptureTheFlag {

	// Static Resources:

	//motors
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final EV3LargeRegulatedMotor gripMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	private static final EV3MediumRegulatedMotor mediumMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));

	//light sensors
	private static final EV3ColorSensor frontColorSensor= new EV3ColorSensor(LocalEV3.get().getPort("S2"));
	private static final EV3ColorSensor downColorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S4"));

	//Ultrasonic sensors
	private static final SensorModes leftUsSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
	private static final SensorModes rightUsSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S3"));


	// Constants
	//To different radius since left side is heavier
	public static final double WHEEL_RADIUS_LEFT = 2.08;
	public static final double WHEEL_RADIUS_RIGHT = 2.08;

	//Turn too much= width too large
	//Turn not enough = width too small
	public static final double TRACK = 14.82;
	
	//Down light sensor offset
	public static final double LSOFFSET = 14;

	public static final double rightWall_x=215;
	public static final double leftWall_x=-30;
	public static final double upperWall_y=215;
	public static final double lowerWall_y=-30;
	
	public static int homeZoneBL_X;
	public static int homeZoneBL_Y;
	public static int homeZoneTR_X;
	public static int homeZoneTR_Y;
	public static int opponentHomeZoneBL_X;
	public static int opponentHomeZoneBL_Y;
	public static int opponentHomeZoneTR_X;
	public static int opponentHomeZoneTR_Y;
	
	public static int opponentFlagType;
	public static int FlagType;
	
	
	/**
	 * If US see within this range. Must go and detect object
	 */
	public static int searchRange=20;
	
	//behaviour constants
	/**
	 * Threshold defined to check if arrived to waypoint
	 */
	public static double wpt_thd=0.5;
	/**
	 * threshold defined to check if must correct trajectory or not
	 */
	public static double travelAngle_thd=0.75;
	/**
	 * If any objects in this treshold, enter emergency
	 */
	public static double emergency_thd=25;
	
	/**@Unused
	 */
	public static int preAvoidDist=15;
	
	/**
	 * Distance between current and new emergency waypoint
	 */
	public static int postAvoidDist=40;
	/**
	 * Distance between emergency waypoint and its buffer waypoint
	 */
	public static int bufferAvoidDist=25;
	/**
	 * Min distance from wall to avoid near it
	 */
	public static int wallAvoidBandwidth=50;
	/**
	 * Min distance seen by US sensor to define object as avoided
	 */
	public static int avoidedDistance=30;


	//Waypoints Data

	/**
	 * Store all waypoints it must go to
	 */
	public static Stack<Double[]> waypoints= new Stack<>();

	/*waypoints has 3 values: x, y, type
	*type=0 if home
	*type=1 if emergency
	*type=2 if search
	*type=3 if other
	*/

	public static Navigation navigator;
	
	public static final Double [] dropZoneCenter=new Double [3];

	public static final Double [] searchZoneEntry=new Double [3];

//	public static final Double [][] searchWaypoint=new Double [4][3];


	/**
	 * @param args
	 * 
	 * This class is the driver of the project. It starts all the threads needed to perform the challenge.
	 * 
	 * 
	 */
	public static void main(String[] args) {

		int buttonChoice;
		
		//Uncomment this line to print to a file
//		logOnFile();
		//Log.setLogging(true,false,false,false,false,false);

		//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		//Date date = new Date();		

		//Log.log(Log.Sender.init, "\n*********Starting..*******\n");
		//Log.log(Log.Sender.init, "Start time: "+ dateFormat.format(date));
		
		//Fetch data from wifi
		WifiStarter wifiS= new WifiStarter();
		wifiS.startWifi();
//		
//		//Instantiate transmission from current connection
		Transmission trans=wifiS.conn.getTransmission();
		
//		Transmission trans=new Transmission();
		
		
		homeZoneBL_X=trans.homeZoneBL_X;
		homeZoneBL_Y=trans.homeZoneBL_Y;
		homeZoneTR_X=trans.homeZoneTR_X;
		homeZoneTR_Y=trans.homeZoneTR_Y;
		
		opponentFlagType=trans.opponentFlagType;
		FlagType=trans.flagType;
		
		opponentHomeZoneBL_X=trans.opponentHomeZoneBL_X;
		opponentHomeZoneBL_Y=trans.opponentHomeZoneBL_Y;
		opponentHomeZoneTR_X=trans.opponentHomeZoneTR_X;
		opponentHomeZoneTR_Y=trans.opponentHomeZoneTR_Y;
		
		

		//Setup left ultrasonic sensor
		SampleProvider leftUsValue = leftUsSensor.getMode("Distance");			// colorValue provides samples from this instance
		float[] leftUsData = new float[leftUsValue.sampleSize()];				// colorData is the buffer in which data are returned

		//Setup left ultrasonic sensor
		SampleProvider rightUsValue = rightUsSensor.getMode("Distance");			// colorValue provides samples from this instance
		float[] rightUsData = new float[rightUsValue.sampleSize()];				// colorData is the buffer in which data are returned

		//Setup front color sensor
		SampleProvider frontColorValue = frontColorSensor.getRGBMode();				// colorValue provides samples from this instance
		float[] frontColorData = new float[frontColorValue.sampleSize()];			// colorData is the buffer in which data are returned

		//Setup down color sensor
		SampleProvider downColorValue = downColorSensor.getRedMode();				// colorValue provides samples from this instance
		float[] downColorData = new float[downColorValue.sampleSize()];			// colorData is the buffer in which data are returned

		//Create US pollers
		UltrasonicPoller leftUspoller = new UltrasonicPoller(leftUsValue,leftUsData);
		UltrasonicPoller rightUspoller = new UltrasonicPoller(rightUsValue,rightUsData);

		//create light pollers
		LightPoller frontLightPoll = new LightPoller(frontColorValue, frontColorData);
		LightPoller downLightPoll = new LightPoller(downColorValue, downColorData);
		
		
		// setup the odometer and display
		Odometer odo = new Odometer();
		OdometryCorrection odometryCorrection = new OdometryCorrection(odo,downLightPoll);

		

		//setup robotControls
		robotControls rc = new robotControls(odo);

	


		ObjectDetection od= new ObjectDetection(odo, rc,leftUspoller,rightUspoller,frontLightPoll);
		ObjectSearch search = new ObjectSearch(odo, rc,leftUspoller,rightUspoller,od);
		
		//setup avoidance
		Avoidance avoidance = new Avoidance(odo,rc,search,leftUspoller,rightUspoller);

		//Create navigation class to get all navigating methods
		navigator = new Navigation(odo, rc, search, avoidance);


		USLocalizer usl = new USLocalizer(odo, rc,rightUspoller,leftUspoller );
		
		//Create object to process data
		//ProcessData pd= new ProcessData(trans,odo);

		final TextLCD t = LocalEV3.get().getTextLCD();
		LCDInfo lcd = new LCDInfo(odo,usl,leftUspoller, rightUspoller,downLightPoll, t,od,search,navigator);


//			//Start the threads
			odo.start();
			odometryCorrection.start();
			frontLightPoll.start();
			downLightPoll.start();
			rightUspoller.start();
			leftUspoller.start();
			lcd.lcdTimer.start();
<<<<<<< HEAD

			//Log.setLogging(true,true,true,false,true,true);
=======
//
			Log.setLogging(true,true,true,false,true,true);
//			Log.setLogging(false,false,true,false,false,false);
>>>>>>> testingOdometryCOrrection
			
			pd.process();
//			
<<<<<<< HEAD
//			for(Double[] wp:waypoints){
//				wp_string+=Arrays.toString(wp);
//			}
			
//			Log.log(Log.Sender.init, "Waypoints: "+ wp_string);
			
//			Log.log(Log.Sender.init, "Waypoints: "+ stackContent());
			
			
//			od.doDetection(); 
			try {	// Wait for 3 seconds
				Thread.sleep(3000);
			}catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			while(true){
				search.Search(); 
=======
			String wp_string="";
//			
			for(Double[] wp:waypoints){
				wp_string+=Arrays.toString(wp);
>>>>>>> testingOdometryCOrrection
			}
			
//			
			Log.log(Log.Sender.init, "Waypoints: "+ wp_string);

			Log.log(Log.Sender.init, "Waypoints: "+ stackContent());
			
			//start navigation thread
//			navigator.start();

<<<<<<< HEAD
		}
		else if(buttonChoice == Button.ID_RIGHT){
//			//Localization+Navigation
				odo.start();
				frontLightPoll.start();
				rightUspoller.start();
				leftUspoller.start();
				lcd.lcdTimer.start();
				Log.setLogging(true,true,true,false,true,true);

				usl.doLocalization();
				
//				while (Button.waitForAnyPress() != Button.ID_RIGHT);
				
				//pd.process();			
//
//				//Start navigator
				navigator.start();
		}
//		else if(buttonChoice == Button.ID_DOWN){
//			
//			rc.PickitUp();
//		}
//
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
=======
			while (Button.waitForAnyPress() != Button.ID_ESCAPE);
			System.exit(0);
>>>>>>> testingOdometryCOrrection
	}

	private static void logOnFile() {
		// TODO Auto-generated method stub
		try {
			Log.setLogWriter(System.currentTimeMillis() + ".log");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @return left motor
	 */
	//Getter methods
	public static EV3LargeRegulatedMotor getLeftMotor(){
		return leftMotor;
	}
	/**
	 * @return rightMotor
	 */
	public static EV3LargeRegulatedMotor getRightMotor(){
		return rightMotor;
	}

	/**
	 * @return gripMotor
	 */
	public static EV3LargeRegulatedMotor getGripMotor(){
		return gripMotor;
	}
	
	/**
	 * @return mediumMotor
	 */
	public static EV3MediumRegulatedMotor getMediumMotor(){
		return mediumMotor;
	}

	/**
	 * @return downColorSensor
	 */
	public static EV3ColorSensor getDownColorSensor()	
	{
		return downColorSensor;
	}

	/**
	 * @param usl UltraSonic Localizer
	 * @param lsl Light Localizer
	 * 
	 * Perform the localization by calling the two localization methods back-to-back.
	 * 
	 */
	private static String stackContent(){
		
		String StackContent="\nBottom->";
		
		
		for(Double[] wp:waypoints){
			StackContent+=""+Arrays.toString(wp);
		}
		
		return StackContent;
		
	}



}




