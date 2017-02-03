package Localization;

import CaptureTheFlag.CaptureTheFlag;
import Navigation.Navigation;
import Navigation.Odometer;
import Utilities.LightPoller;
import Utilities.robotControls;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;


/**
 * 
 * Class allows robot to localize with light sensor
 * 
 *
 */
public class LightLocalizer {

	//objects
	private Odometer odo;
	private robotControls rc;
	private Navigation navigator;
	public LightPoller lightPoll;
	
	//gte motors from main
	EV3LargeRegulatedMotor leftMotor = CaptureTheFlag.getLeftMotor();
	EV3LargeRegulatedMotor rightMotor = CaptureTheFlag.getRightMotor();

	//double
	public double x, y;

	public double angle1;
	public double angle2;
	public double angle3;
	public double angle4;
	
	public double dThetaX,dThetaY, dTheta, dTx= 0, dTy= 0;
	
	public double iniTheta;
	private double minAngle;
	
	private double sensorDist=13.5;
	
	//boolean
	private boolean localizing = false;
	
	//float
	public float intensity;

	double WR=CaptureTheFlag.WHEEL_RADIUS_LEFT;
	double WB=CaptureTheFlag.TRACK;
	
	

	/**
	 * @param odo Odometer
	 * @param rc RobotControls
	 * @param navigator Navigation
	 * @param lightPoll LightPoller
	 * 
	 * Create a reference
	 * 
	 */
	public LightLocalizer(Odometer odo,robotControls rc, Navigation navigator,LightPoller lightPoll) {
		this.odo = odo;
		this.rc = rc;
		this.navigator = navigator;
		this.lightPoll = lightPoll;
	}

	/**
	 * Perform Light localization
	 * 
	 */
	public void doLocalization() {
		// drive to location listed in tutorial

		boolean ready=false;

		//turn robot 45 degrees from assumed 0 degrees
		rc.turn(45,80);

		//go forward until robot sees a line and go backward
		//step insure that robot is at good position to catch the 4 lines
		while(!ready){
			rc.goForward(50);
			intensity = lightPoll.intensity;
			if(intensity<0.3 && intensity>0.01){
				Sound.beep();
				ready=true;
<<<<<<< HEAD
				rc.travelBackward(sensorDist*1.5, 100);
=======
				rc.travelBackward(sensorDist*1.5,80);
>>>>>>> testingOdometryCOrrection
			}
		}
		

		localizing =true;
		int counter = 0;


		while(localizing){

			rc.rotateClockwise(40);

			//get light intensity from light poller
			intensity = lightPoll.intensity;

			//if sensor meets a black line, update counter
			if(intensity<0.3 && intensity>0.01){
				counter++;		
				Sound.beep();

				if(counter == 1){
					//first line
					angle1=odo.getTheta();
				}
				else if(counter == 2){
					//second line
					angle2 = odo.getTheta();
				}
				else if(counter == 3){
					angle3 = odo.getTheta();
					//compute tetha x
					dTx = Math.abs(angle3- angle1);
					//calculate y
					y = -sensorDist*(Math.cos(Math.toRadians(dTx/2)));
				}
				else if(counter ==4 ){
					angle4 = odo.getTheta();
					//compute theta y
					dTy = Math.abs(angle2-angle4);
					//calculate x
					x = -sensorDist*(Math.cos(Math.toRadians(dTy/2)));
					localizing = false;
				}
			}
		}

		rc.stopRobot();

		dThetaY = -270 - dTy/2 + angle4;
		
//		dThetaX = 180 + dTx/2 - angle3;
		
//		dTheta=-(dThetaY+dThetaX)/4;
		dTheta=dThetaY-8;

		Sound.beep();
		
//		minAngle=navigator.calcMinAngle(odo.getTheta()%360,0);
		
		odo.setPosition(new double [] {x,y,0.0}, new boolean [] {true, true, false});
		
		//navigate to (0,0)
		navigator.start();
		
		try {
			navigator.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//		minAngle=navigator.calcMinAngle(odo.getTheta(),0);
		
		rc.turnTo(odo.getTheta(),0,80);
		
//		minAngle=navigator.calcMinAngle(odo.getTheta(),dTheta);
		
		rc.turn(dTheta,80);
		
		rc.stopRobot();
		
		odo.setPosition(new double [] {0.0,0.0,0.0}, new boolean [] {true, true, true});
		
		
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees
	}

}
