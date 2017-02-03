package Navigation;


import CaptureTheFlag.CaptureTheFlag;
import Utilities.Log;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 *
 *Class allows robot to keep track of it's position on the playground
 *
 */
public class Odometer extends Thread {
	// robot position
	private double x, y, theta;
	static boolean running;
	int lastTachoL,lastTachoR,nowTachoL,nowTachoR;
	double distL,distR,deltaD,deltaT,dX,dY, modulo;

	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;

	// lock object for mutual exclusion
	private Object lock;

	// default constructor
	public Odometer() {
		x = 0.0;
		y = 0.0;
		theta = 0.0;
		lock = new Object();
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;
		EV3LargeRegulatedMotor leftMotor = CaptureTheFlag.getLeftMotor();
		EV3LargeRegulatedMotor rightMotor = CaptureTheFlag.getRightMotor();
		double WRL=CaptureTheFlag.WHEEL_RADIUS_LEFT;
		double WRR=CaptureTheFlag.WHEEL_RADIUS_LEFT;
		double WB=CaptureTheFlag.TRACK;


		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();

		lastTachoL=leftMotor.getTachoCount();
		lastTachoR=rightMotor.getTachoCount();

		while (true) {
			updateStart = System.currentTimeMillis();
			// put (some of) your odometer code here
			running=true;


			nowTachoL=leftMotor.getTachoCount();
			nowTachoR=rightMotor.getTachoCount();

			distL=Math.PI*WRL*(nowTachoL-lastTachoL)/180;
			distR=Math.PI*WRR*(nowTachoR-lastTachoR)/180;

			lastTachoL=nowTachoL;
			lastTachoR=nowTachoR;

			deltaD= 0.5*(distL+distR);
			deltaT= (distL-distR)/WB;


			synchronized (lock) {
				// don't use the variables x, y, or theta anywhere but here!

				setTheta(this.theta+deltaT);

				dX= deltaD*Math.sin(this.theta);
				dY= deltaD*Math.cos(this.theta);

				setX(this.x+dX);
				setY(this.y+dY);
			}


			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
				Log.log(Log.Sender.odometer,String.format("x: %f, y: %f, t: %f",
						getX(), getY(), getTheta()));
			}
		}
	}

	// accessors
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = (theta *180)/Math.PI;
		}
	}

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result = (180*theta)/Math.PI;
			//	result = theta;
		}

		return result;
	}

	public double getNowTachoL() {
		double result;

		synchronized (lock) {
			result =nowTachoL;
		}

		return result;
	}



	// mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			Sound.beep();
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2] * (Math.PI / 180);
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}


	public boolean travelPositiveY(){
		//return true if the robot is travelling in incresing y
		
		double absoluteTetha=Math.abs(getTheta()%360);
		
		if(absoluteTetha>=267 && absoluteTetha<=360 || absoluteTetha<=93 && absoluteTetha>=0)
			return true;
		else
			return false;
	}

	public boolean travelPositiveX(){
		//return true if the robot is travelling in incresing x
		
		double absoluteTheta=Math.abs(getTheta()%360);

		if((absoluteTheta>=0 && absoluteTheta<=183) ||absoluteTheta>=357)
			return true;
		else
			return false;	
	}

}