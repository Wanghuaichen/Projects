package Localization;
import java.util.Arrays;

import CaptureTheFlag.CaptureTheFlag;
import Navigation.Navigation;
import Navigation.Odometer;
import Utilities.UltrasonicPoller;
import Utilities.robotControls;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class USLocalizer {
	public static int ROTATE_SPEED = 70;

	public UltrasonicPoller leftuspoller;
	public UltrasonicPoller rightuspoller;
	private Odometer odo;
	private robotControls rc;
	public double angleA=0, angleB=0;
	private int wallThreshold = 35;
	public static int FAST = 200, SLOW = 100;
	public static double error = 1;
	public static double USD = 7.5; // US distance from wheels axis center

	//get motors from main(Lab4)
	EV3LargeRegulatedMotor leftMotor = CaptureTheFlag.getLeftMotor();
	EV3LargeRegulatedMotor rightMotor = CaptureTheFlag.getRightMotor();

	public USLocalizer(Odometer odo,robotControls robotControl,UltrasonicPoller leftusPoll, UltrasonicPoller rightusPoll) {
		this.odo = odo;
		this.rc = robotControl;
		this.leftuspoller = leftusPoll;
		this.rightuspoller = rightusPoll;
	}

	public void doLocalization() {
		double [] pos = new double [3];
		boolean firstdirection = false;
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
		
		double [] minDistXY = new double [2];
		
		if (leftUSFilteredData() < wallThreshold && rightUSFilteredData() < wallThreshold)
			firstdirection = true;
		else firstdirection = false;
		
		
		if (firstdirection) {
			angleA = FindEdge1();
			Sound.beep();
			minDistXY = wallDist();
			angleB = FindEdge2();
			Sound.beep();
		}
		else {
			while (leftUSFilteredData() >= wallThreshold || rightUSFilteredData() >= wallThreshold) {
				rc.rotateCounterClockwise(FAST);
			}
			angleA = FindEdge1();
			Sound.beep();
			minDistXY = wallDist();
			angleB = FindEdge2();
			Sound.beep();
		}
		
	
		
		odo.getPosition(pos, new boolean [] {false, false, true});
		pos[0] = - (30.46 - minDistXY[1] - USD); pos [1] = - (30.46 - minDistXY[0] - USD);
		pos[2] = (pos[2] + FinddTheta(angleA, angleB, minDistXY));
		odo.setPosition(pos, new boolean [] {true, true, true});
		rc.turnTo(pos[2], 0, SLOW);
		rc.stopRobot();
		Sound.beepSequenceUp();
		
	}

	public double leftUSFilteredData() {		

		double distance=leftuspoller.distance;

		if (distance>60)
			distance=60;

		return distance;
	}	
	
	public double rightUSFilteredData() {		

		double distance=rightuspoller.distance;

		if (distance>60)
			distance=60;

		return distance;
	}
	
	
	
	
	public double FindEdge1() {
		boolean left = false;
		boolean right = false;
		double leftAngle = 0, rightAngle = 0;
		while (!left || !right) {
			rc.rotateCounterClockwise(SLOW);
			if (leftUSFilteredData() > wallThreshold && !left) {
				leftAngle = odo.getTheta();
				left = true;
			}
			if (rightUSFilteredData() > wallThreshold && !right) {
				rightAngle = odo.getTheta();
				right = true;
			}	
		}
		rc.stopRobot();
		return (rightAngle + leftAngle) / 2;
	}
	
	public double FindEdge2() {
		boolean left = false;
		boolean right = false;
		double leftAngle = 0, rightAngle = 0;
		while (!left || !right) {
			rc.rotateClockwise(SLOW);
			if (leftUSFilteredData() > wallThreshold && !left) {
				leftAngle = odo.getTheta();
				left = true;
			}
			if (rightUSFilteredData() > wallThreshold && !right) {
				rightAngle = odo.getTheta();
				right = true;
			}	
		}
		rc.stopRobot();
		return (rightAngle + leftAngle) / 2;
	}
	
	public double[] wallDist() {
		boolean X = true, Y = false;
		double [] minDistXY = new double [4];
		double Xangle = 0, Yangle = 0;
		
		double leftUSData = leftUSFilteredData(); double rightUSData = rightUSFilteredData();
		while (leftUSData >= wallThreshold && rightUSData >= wallThreshold) {
			rc.rotateClockwise(SLOW);
			leftUSData = leftUSFilteredData(); rightUSData = rightUSFilteredData();
		}
		
		leftUSData = leftUSFilteredData(); rightUSData = rightUSFilteredData();
		while (leftUSData < wallThreshold || rightUSData < wallThreshold) {
			rc.rotateClockwise(SLOW);
			
			if (Math.abs(leftUSData - rightUSData) < error && X) {
				minDistXY[0] = (leftUSData + rightUSData) / 2;
				X = false;
				Y = true;
				Xangle = odo.getTheta();
				Sound.beep();
			} 
		
			if (Math.abs(leftUSData - rightUSData) < error && Y) {
				if ( checkAngle(odo.getTheta(), Xangle) ) { 
					minDistXY[1] = (leftUSData + rightUSData) / 2;
					X = false;
					Y = false;
					Yangle = odo.getTheta();
					Sound.beep();
				}
			} 

			
			leftUSData = leftUSFilteredData(); rightUSData = rightUSFilteredData();
		}
		rc.stopRobot();
		minDistXY[2] = Xangle; minDistXY[3] = Yangle;
		return minDistXY;
	}
	
	public boolean checkAngle (double Cangle, double Fangle) {
		
		if (Cangle - Fangle < 100 && Cangle - Fangle > 80)
			return true;
		return false;
	}
	
	public double FinddTheta (double angleA, double angleB, double[] minDistXY) {
		double [] dTheta = new double [3];
		if ( angleA < angleB ) {
			dTheta[0] = 180 + Math.atan2( minDistXY[0], minDistXY[1])*(180/Math.PI) - (angleA + angleB ) / 2;
		} else if ( angleA > angleB ) {
			dTheta[0] = Math.atan2(minDistXY[0], minDistXY[1])*(180/Math.PI) - (angleA + angleB ) / 2;
		}
		dTheta[1] = 180 - minDistXY[2];
		dTheta[2] = 270 - minDistXY[3];
		
		Arrays.sort(dTheta);
		
		return dTheta[1];
	}
}