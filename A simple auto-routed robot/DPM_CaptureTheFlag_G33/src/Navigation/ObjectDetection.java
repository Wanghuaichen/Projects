package Navigation;


import Utilities.LightPoller;
import Utilities.Log;
import Utilities.UltrasonicPoller;
import Utilities.robotControls;
import lejos.hardware.Sound;
import CaptureTheFlag.CaptureTheFlag;

/**
 *
 *Class allows robot to detect what is the object
 *
 */
public class ObjectDetection {
	
	//objects
	public UltrasonicPoller rightUspoller;
	public UltrasonicPoller leftUspoller;
	
	public LightPoller lightpoller;
	@SuppressWarnings("unused")
	private Odometer odo;
	private robotControls rc;
	private int flagID;
	
	//Distances and angles
	public double blockDistance;
	
	//booleans
	private int opponentFlagType=CaptureTheFlag.opponentFlagType;
	
<<<<<<< HEAD
	private double detectDist=10;
=======
	private double detectDist=15;
>>>>>>> testingOdometryCOrrection

	/**
	 * @param odo
	 * @param robotControl
	 * @param leftUsPoll
	 * @param rightUsPoll
	 * @param lightPoll
	 */
	public ObjectDetection(Odometer odo, robotControls robotControl, UltrasonicPoller leftUsPoll,
			UltrasonicPoller rightUsPoll, LightPoller  lightPoll) {
		this.odo = odo;
		this.rightUspoller = rightUsPoll;
		this.leftUspoller = leftUsPoll;
		this.lightpoller = lightPoll;
		this.rc = robotControl;
	}

	public boolean doDetection() {
		
		Log.log(Log.Sender.avoidance, "\n****OBJECT DETECTION****");
		flagID = opponentFlagType;
		
		//return true if it's a color block, false if it's a wooden block

		//run until it sees something
		while(true){
			if (lightpoller.getColorID() != 0) {
				try {	// Wait for 1 seconds
					Thread.sleep(1500);
				}catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				if (lightpoller.getColorID() != 0)
					break;
			}
			rc.goForward(60);
		}
		rc.stopRobot();

		//identify block in this loop
<<<<<<< HEAD
		while (rightUspoller.distance >= detectDist && rightUspoller.distance >= detectDist) {
			rc.goForward(50);
		}
		rc.stopRobot();
		
		rc.travelForward(4, 80);
=======
		while (rightUspoller.distance >= detectDist && leftUspoller.distance >= detectDist) {
			rc.goForward(80);
		}
		rc.stopRobot();
		rc.DropClaw();
		rc.travelForward(10,100);
>>>>>>> testingOdometryCOrrection
		
		float colorID = lightpoller.getColorID();
		//Log.log(Log.Sender.avoidance, "ColorID:"+colorID);
		//Log.log(Log.Sender.avoidance, "FlagID:"+flagID);
		
		if(colorID == flagID) {
			//rc.travelForward(5);
			//Log.log(Log.Sender.avoidance, "\n****GRAB GOOD BLOCK****");
			rc.PickitUp();
			Sound.beepSequenceUp();
			return true;
		}
		else if(colorID != 0) {
			//rc.travelForward(5);
			//Log.log(Log.Sender.avoidance, "\n****GRAB BAD BLOCK****");
			rc.PickitUp ();
			//Log.log(Log.Sender.avoidance, "\n****THROW BAD BLOCK****");
			rc.PushitBack();
			Sound.beepSequence();
			return false;
		} else {
			rc.PickitUp();
			return false;
		}
			
	}
	
	
	/**
	 * Must delete this piece of code in not so long. Change everything for left and right independently
	 * 
	 * @return
	 */
	public double getFilteredData() {		
		
		//get median value of the list to reduce noise
		double distance=rightUspoller.distance;

		if (distance>80)
			distance=80;

		return distance;
	}	
	
	public double leftUSFilteredData() {		

		double distance=leftUspoller.distance;

		if (distance>60)
			distance=60;

		return distance;
	}	
	
	public double rightUSFilteredData() {		

		double distance=rightUspoller.distance;

		if (distance>60)
			distance=60;

		return distance;
	}

}