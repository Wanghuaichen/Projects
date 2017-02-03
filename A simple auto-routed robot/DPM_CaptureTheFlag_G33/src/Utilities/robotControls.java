package Utilities;

import CaptureTheFlag.CaptureTheFlag;
import Navigation.Odometer;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;

/**
 *
 *This is a utility class which controls all motors
 *
 */
public class robotControls {

	//Objects
	private Odometer odometer;
	
	EV3LargeRegulatedMotor leftMotor = CaptureTheFlag.getLeftMotor();
	EV3LargeRegulatedMotor rightMotor = CaptureTheFlag.getRightMotor();
	EV3LargeRegulatedMotor gripMotor = CaptureTheFlag.getGripMotor();
	EV3MediumRegulatedMotor mediumMotor = CaptureTheFlag.getMediumMotor();
	
	//double
	double minAngle;
	double nTheta;
	double fwdDist;
	double WRL=CaptureTheFlag.WHEEL_RADIUS_LEFT;
	double WRR=CaptureTheFlag.WHEEL_RADIUS_RIGHT;
	double WB=CaptureTheFlag.TRACK;
	
	int SPEED = 200;

	/**
	 * Control all motors of the robot
	 * 
	 * @param odometer odometer
	 */
	public robotControls(Odometer odometer){
		this.odometer = odometer;
	}

	/**
	 * 
	 * 
	 * */
	public void stopRobot(){
		
		rightMotor.setSpeed(0);
		leftMotor.setSpeed(0);
		
		rightMotor.forward();
		leftMotor.forward();
		
	}
	

	/**
	 * Method used turn to an absolute angle on the field
	 * 
	 * @param cTheta		current heading
	 * @param nTheta		target heading
	 * @param speed			rotation speed
	 * 
	 * 
	 */
	public void turnTo(double cTheta, double nTheta, int speed)
	{
		//
		//Set 80 as default speed
		
		double minAngle=calcMinAngle(cTheta,nTheta);

		//turn the minAngle
		turn (minAngle,speed);
	}
	
	/**
	 * turn a given angle (relative turn)
	 * 
	 * @param theta		rotation angle
	 * @param speed		rotation speed
	 * 
	 * */
	public void turn(double theta, int speed)
	{
		//Set 80 as default speed

		//Rotate to angle passed as parameter
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);

		leftMotor.rotate(convertAngle(WRL, WB, theta), true);
		rightMotor.rotate(-convertAngle(WRR, WB, theta), false);
	}

	public void rotateGrip(double theta)
	{
		//Rotate to angle passed as parameter
		gripMotor.setSpeed(150);

		gripMotor.rotate((int) theta, false);
	}

	/**
	 * makes the robot rotate clockwise
	 * @param speed		rotation speed
	 * 
	 * */
	public void rotateClockwise(int speed){
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);

		leftMotor.forward();
		rightMotor.backward();
	}

	/**
	 * makes the robot rotate counterClockwise
	 * @param speed		rotation speed
	 * 
	 * */
	
	public void rotateCounterClockwise(int speed){
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);

		leftMotor.backward();
		rightMotor.forward();
	}

	/**
	 * Go forward at a given speed indefinitely
	 * 
	 * @param speed		robot speed
	 * 
	 * */
	
	public void goForward(int speed){
		
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);

		rightMotor.forward();
		leftMotor.forward();
		
	}

	/**
	 * Go backward at a given speed indefinitely
	 * @param speed		robot speed
	 * 
	 * */
	
	public void goBackward(int speed){
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);

		leftMotor.backward();
		rightMotor.backward();
	}
	
	/**
	 * Travel foward for a given distance
	 * 
	 * @param distance	linear distance traveled by robot
	 * 
	 * */

	public void travelForward(double distance, int speed){

		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);

		leftMotor.rotate(convertDistance(WRL, distance), true);
		rightMotor.rotate(convertDistance(WRR, distance), false);
	}

	/**
	 * Travel backward for a given distance
	 * 
	 * @param distance	linear distance traveled by robot
	 * 
	 * */
	
	public void travelBackward(double distance, int speed){

		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);

		leftMotor.rotate(-convertDistance(WRL, distance), true);
		rightMotor.rotate(-convertDistance(WRR, distance), false);
	}
	
<<<<<<< HEAD

=======
>>>>>>> testingOdometryCOrrection
	
	/**
	 * calculating minimal angle for turn between two absolute angles
	 * 
	 * @param cTheta		current heading
	 * @param nTheta		target heading
	 * */
	public double calcMinAngle(double biasedCTheta, double nTheta){
		double cTheta=biasedCTheta%360;

		double diff=nTheta-cTheta;  //cTheta: currentTheta nTheta=expected new theta

		double min=diff;

		if(diff<=180 && diff>=-180){
			//Do nothing, keep initial value of min. (min=nTheta-cTheta)
		}
		else if(diff<-180){
			min=diff+360;
		}
		else if(diff>180){
			min=diff-360;
		}

		return min;

	}
	
	/**
	 * Calculate linear distance between current two given points
	 * 
	 * @param x			target x
	 * @param y			target y
	 * @param cuX		current x
	 * @param cuY		current y
	 * @return fwdDist 	linear distance between robot and target point
	 */
	public double linearDistance(double x,double y,double cuX,double cuY){
		//calculate linear distance between current position and target position

		double fwdDist=Math.sqrt(Math.pow(x-cuX, 2)+Math.pow(y-cuY, 2));

		return fwdDist;
	}

	/**
	 * @param radius distance traveled by robot
	 * @param distance wheel radius
	 * @return
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	
	/**
	 * @param distance		distance traveled by robot
	 * @param radius		wheel radius
	 * @return
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	
	/**
	 * Pause for given time
	 * 
	 * @param milliSecond time
	 */
	public void waitTime(long milliSecond){

		try {
			Thread.sleep(milliSecond);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void DropClaw() {
		// Claw 2 Motor gets in position to grab object
		mediumMotor.setSpeed(SPEED);		
		mediumMotor.rotate(+90, false);

		// Claw 1 Motor brings claw down
		gripMotor.setSpeed(SPEED);		
		gripMotor.rotate(+180, false);
	}
	
	
	public void PickitUp() {

		// Claw 2 Motor grabs object
		mediumMotor.setSpeed(SPEED);		
		mediumMotor.rotate(-90, false);

		gripMotor.setSpeed(SPEED);	// Retract the arm with the claw grasping the object
		gripMotor.rotate(-180, false);
	}

	public void PushitBack() {
		// Claw 2 Motor gets in the ready-position to grab object
		
		int speed=200;
		
		mediumMotor.setSpeed(speed);		
		mediumMotor.rotate(+90, false);

		// Claw 1 Motor brings claw midway (aiming upwards)
		gripMotor.setSpeed(speed);		
		gripMotor.rotate(+90, false);

		// Claw 2 Motor closes the claw hand
		mediumMotor.setSpeed(speed);		
		mediumMotor.rotate(-90, false);

		// Claw 1 Motor brings claw back into resting position
		gripMotor.setSpeed(speed);		
		gripMotor.rotate(-90, false);

		// Claw 2 Motor gets in the ready-position which knocks block off and behind the brick
		mediumMotor.setSpeed(speed);		
		mediumMotor.rotate(+90, false);

		mediumMotor.setSpeed(speed);		
		mediumMotor.rotate(-90, false);
	}

}