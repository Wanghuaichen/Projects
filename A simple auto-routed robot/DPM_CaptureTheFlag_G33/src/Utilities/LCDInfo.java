package Utilities;

import CaptureTheFlag.CaptureTheFlag;
import Localization.USLocalizer;
import Navigation.Navigation;
import Navigation.ObjectDetection;
import Navigation.ObjectSearch;
import Navigation.Odometer;
import lejos.hardware.lcd.TextLCD;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

public class LCDInfo implements TimerListener{
	public static final int LCD_REFRESH = 100;
	private Odometer odo;
	public Timer lcdTimer;
	@SuppressWarnings("unused")
	private USLocalizer localizer;
	@SuppressWarnings("unused")
	private UltrasonicPoller leftuspoll;
	private UltrasonicPoller rightuspoll;
	public LightPoller lightPoll; 
	public TextLCD LCD;
	public ObjectDetection od;
	public ObjectSearch os;
	public Navigation navigator;

	// arrays for displaying data
	private double [] pos;

	public LCDInfo(Odometer odo, USLocalizer localizer, UltrasonicPoller leftuspoll, UltrasonicPoller rightuspoll
			,LightPoller lightPoll, TextLCD t, ObjectDetection od, ObjectSearch os, Navigation navigator) {
		this.odo = odo;
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		this.localizer = localizer;
		this.leftuspoll = leftuspoll;
		this.rightuspoll = rightuspoll;
		this.lightPoll = lightPoll;
		this.LCD = t;
		this.od = od;
		this.os = os;
		this.navigator = navigator;
		// initialise the arrays for displaying data
		pos = new double [3];


	}

	public void timedOut() { 
		odo.getPosition(pos, new boolean[] { true, true, true });
		LCD.clear();
		LCD.drawString("X: ", 0, 0);
		LCD.drawString("Y: ", 0, 1);
		LCD.drawString("T: ", 0, 2);
		LCD.drawString((Double.toString(pos[0])), 3, 0);
		LCD.drawString((Double.toString(pos[1])), 3, 1);
		LCD.drawString((Double.toString(pos[2])), 3, 2);
		
		LCD.drawString("UsR: "+rightuspoll.distance,0, 3);
		LCD.drawString("UsL: "+leftuspoll.distance,0, 4);
//		LCD.drawString(Integer.toString((int)os.gettingAbsPos()[0]), 0, 0);
//		LCD.drawString("fD: "+(Double.toString(os.getFilteredData())), 0, 1);
		
//		if(os.searching)
//		{
//			LCD.drawString("Searching...", 0, 2);
//		}
//		else if(!os.searching)
//		{
//			LCD.drawString("Going Home...", 0, 2);
//		}
//		
//		if(od.objectDetected){
//			if(od.objectStyrofoam){
//				LCD.drawString("Object Detected", 0, 3);
//				LCD.drawString("Block", 0, 4);
//			}
//			else{
//				LCD.drawString("Object Detected", 0, 3);
//				LCD.drawString("Not a Block", 0, 4);				
//			}
//		}
//		else if(!od.objectDetected){
//			LCD.drawString("No Object Detected", 0, 3);
//			LCD.drawString("", 0, 4);
//		}

		LCD.drawString("I: "+Float.toString(lightPoll.intensity), 0, 5);
//		LCD.drawString("a1: "+(Double.toString(os.angle1)), 0, 6);
//		LCD.drawString("a2: "+(Double.toString(os.angle2)), 0, 7);	
//		LCD.drawString(Integer.toString(navigator.i), 0, 6);
		
//		Double[] arr=new Double[CaptureTheFlag.waypoints.size()];
//		LCD.drawString(CaptureTheFlag.printWaypoint(), 0, 7);


		//		if(os.isWall){
		//			LCD.drawString("isWall", 0, 6);			
		//		}
		//		else if (!os.isWall){
		//			LCD.drawString("NOT Wall", 0, 6);	
		//		}


	}


}
