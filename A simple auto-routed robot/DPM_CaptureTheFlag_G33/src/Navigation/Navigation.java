package Navigation;

import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.Stack;

import CaptureTheFlag.CaptureTheFlag;
import Utilities.Log;
import Utilities.robotControls;

import lejos.hardware.Sound;

/**
 * 
 * Class allows robot to navigation between waypoints while looking at any obstacles and doing specific tasks depending on its position.
 *
 */
public class Navigation extends Thread {
	private static final int FORWARD_SPEED = 200;

	//Objects
	private Odometer odometer;
	private robotControls rc;
	private ObjectSearch search;
	private Avoidance avoidance;

	private double[] position = new double[3];

	//waypoints
	private static Stack<Double[]> waypoints=CaptureTheFlag.waypoints;
	private Double[] searchZoneEntry= CaptureTheFlag.searchZoneEntry;
	private Double[] dropZoneCenter= CaptureTheFlag.dropZoneCenter;

	//Angles and distances
	double minAngle;
	double nTheta;
	double fwdDist;
	double initialTheta=0;

	//Booleans
	boolean completed=false;
	boolean searching=false;


	//Utilities
	int countFollower=0;
	double wpt_x;
	double wpt_y;

	double wpt_thd=CaptureTheFlag.wpt_thd;            //threshold defined to check if arrived to waypoint
	double travelAngle_thd=CaptureTheFlag.travelAngle_thd;	//threshold defined to check if must correct trajectory or not.
	double emergency_thd=CaptureTheFlag.emergency_thd;		//threshold to define if must enter in emergency or not

	private Object lock;

	/**
	 * State the navigation thread can be in
	 *
	 */
	enum State {
		TRAVEL, EMERGENCY, SEARCH, REACH, HOME, DETECT
	};

	State state;

	//Constructor
	/**
	 * @param odometer
	 * @param rc
	 * @param search
	 * @param avoidance
	 */
	public Navigation(Odometer odometer, robotControls rc, ObjectSearch search, Avoidance avoidance){
		this.odometer = odometer;
		this.rc = rc;
		this.search = search;
		this.avoidance = avoidance;
		lock = new Object();
	}

	public void run(){

		//Play tone to make sure thread starts
		//		Sound.playTone(200,2000, 1000);
		state = State.TRAVEL;

		while(!completed){
			//			Log.log(Log.Sender.Navigator, "state: " + state);

			switch(state){			

			case TRAVEL:
				/*Travel to current waypoint and check if any emergency
				 * 
				 */
				wpt_x=waypoints.peek()[0];
				wpt_y=waypoints.peek()[1];

				travelTo(wpt_x,wpt_y);
				
				
				//Check if waypoint is reached
				if(distanceToWaypoint(wpt_x,wpt_y)<wpt_thd){
					state=State.REACH;
					break;
				}
				
				//if searching, execute its to check if object
				if(search.searching){
					search.Search();
					break;
				}
				
				if(search.isInSearchZone(0)){
					while(waypoints.peek()[2]==1.0){
						waypoints.pop();
					}
					break;
				}

				//Check if there is something in robot's path (can be replaced by checkEmergenecy in avoidance, when ready)
				if(checkEmergency()){
					state=State.EMERGENCY;
					break;
				}


				//check if robot is passing by a critical waypoint while avoiding. If it is, stop avoiding and continue
				if(distanceToWaypoint(searchZoneEntry[0],searchZoneEntry[1])<wpt_thd || distanceToWaypoint(dropZoneCenter[0],dropZoneCenter[1])<wpt_thd){
					while(waypoints.peek()[2]==1.0){
						waypoints.pop();
					}
				}

				break;
			case EMERGENCY:
				//enter in emergency mode
				Log.log(Log.Sender.Navigator, "\n*********EMERGENCY*******\n");
				
				while(waypoints.peek()[2]==1.0){
					waypoints.pop();
				}

				avoidance.avoid();
				state=State.TRAVEL;
				break;
			case REACH:
				//A waypoint is reached, what robot do?
				Log.log(Log.Sender.Navigator, "\n*********REACH*******\n");
				
				rc.stopRobot();
				Sound.beep();
				Sound.beep();
				Sound.beep();
				
				if(waypoints.peek()==searchZoneEntry){
//					stopMe();
					Log.log(Log.Sender.Navigator, "REACH: SEARCHZONE");
					state=State.SEARCH;
				}
				else if (waypoints.peek()==dropZoneCenter){
					stopMe();
					Log.log(Log.Sender.Navigator, "REACH: HOME");
				}
				else{
					state=State.TRAVEL;
				}
				//simply pop waypoint
				Log.log(Log.Sender.Navigator, "state: " + state+" (popping"+Arrays.toString(waypoints.peek())+")");
				waypoints.pop();
				avoidance.isAvoiding=false;

				break;
			case SEARCH:
				//enter search mode
				Log.log(Log.Sender.Navigator, "\n*********SEARCH*******\n");
			
				//set waypoints
				
				search.searching=true;
				state=State.TRAVEL;
				break;
			case HOME:
				//Exit search and go home
				Log.log(Log.Sender.Navigator, "\n*********GO HOME*******\n");
				
				searching=false;
				
				//remove all search waypoints
				while(waypoints.peek()[2]==2.0){
					waypoints.pop();
				}
				
				//get out of search zone by shortest path
				
				state=State.TRAVEL;
				break;
			}
			try{
				waypoints.peek();
			}
			catch(EmptyStackException e){
				break;
			}

			Log.log(Log.Sender.Navigator, "state: " + state+" ("+Arrays.toString(waypoints.peek())+")");
			Log.log(Log.Sender.Navigator, "state: " + state+" ("+stackContent()+")");
		}
		//stop robot
		stopMe();
	}

	public void stopMe(){
		//interrupted thread and exit loop
		completed=true;
		rc.stopRobot();
		this.interrupt();
	}


	public double distanceToWaypoint(double x, double y){

		this.odometer.getPosition(position, new boolean[] { true, true, true,true });

		double cuX=position[0]; //Current position in x, polled from odometer
		double cuY=position[1]; //Current position in y, polled from odometer
		fwdDist=rc.linearDistance(x,y,cuX,cuY); //linear distance between current position and waypoint position

		return fwdDist;
	}


	/**
	 * @param x		Target x position
	 * @param y		Target y position
	 */
	public void travelTo(double x, double y)
	{
		//travel to given waypoint

		this.odometer.getPosition(position, new boolean[] { true, true, true,true });

		double cuX=position[0]; //Current position in x, polled from odometer
		double cuY=position[1]; //Current position in y, polled from odometer
		double cTheta=position[2]; //Current direction angle, polled from odometer
		fwdDist=rc.linearDistance(x,y,cuX,cuY); //linear distance between current position and waypoint position

		//calculate required value of theta to get to the waypoint
		nTheta= (Math.atan2(x-cuX, y-cuY)*180)/Math.PI;


		//based on current direction angle, calculate min angle to get to the waypoint 
		minAngle=rc.calcMinAngle(cTheta,nTheta);


		//if angle is significant, correct trajectory
		if(Math.abs(minAngle)>5){//0.75
			Log.log(Log.Sender.Navigator, "\n*********TURNING*******\n");
			rc.turn(minAngle,80);
			Log.log(Log.Sender.Navigator, "\n*********BACK TO TRAVEL*******\n");
		}

		//Let the robot go forward		
		rc.goForward(FORWARD_SPEED);
	}


	private static String stackContent(){

		String StackContent="Bottom->";


		for(Double[] wp:waypoints){
			StackContent+=""+Arrays.toString(wp);
		}

		return StackContent;

	}
	private boolean checkEmergency() {
		Log.log(Log.Sender.Navigator, "\n*********Check Emergency*******\n");
		//Make sure robot is not in search state
		if(!search.searching){
			Log.log(Log.Sender.Navigator, "Search=False");
			//Check if something in emergency threshold
			if(avoidance.getRightFilteredData() <= emergency_thd || avoidance.getLeftFilteredData() <= emergency_thd){
				Log.log(Log.Sender.Navigator, "\n*********SOMETHING IN MY WAY*******\n");
				//Check if it's on the right
				if(avoidance.getRightFilteredData() <= emergency_thd){
					Log.log(Log.Sender.avoidance, "Right says: There's something in my way");
					//check if seen object is in search zone
					if(search.isInSearchZone(avoidance.getRightFilteredData())){
						Log.log(Log.Sender.avoidance, "Right says: Object IN Zone");
						//Pop all emergency waypoints, because close enough to zone to go into it
						while(waypoints.peek()[2]==1.0){
							waypoints.pop();
						}
						return false;
					}
					if(search.isWall(avoidance.getRightFilteredData())){
						Log.log(Log.Sender.avoidance, "Right says: This is a wall");
						//Pop all emergency waypoints, because close enough to zone to go into it
						while(waypoints.peek()[2]==1.0){
							waypoints.pop();
						}
						return false;
					}
				}
				
				//Check if it's on the left
				if(avoidance.getLeftFilteredData() <= emergency_thd){
					Log.log(Log.Sender.avoidance, "Left says: There's something in my way");
					//check if seen object is in search zone
					if(search.isInSearchZone(avoidance.getLeftFilteredData())){
						Log.log(Log.Sender.avoidance, "Left says:Object IN Zone");
						//Pop all emergency waypoints, because close enough to zone to go into it
						while(waypoints.peek()[2]==1.0){
							waypoints.pop();
						}
						return false;
					}
					if(search.isWall(avoidance.getLeftFilteredData())){
						Log.log(Log.Sender.avoidance, "Left says: This is a wall");
						//Pop all emergency waypoints, because close enough to zone to go into it
						while(waypoints.peek()[2]==1.0){
							waypoints.pop();
						}
						return false;
					}
				}
				Log.log(Log.Sender.avoidance, "Object OUT Zone");
				return true;
			}
		}
		else{
			Log.log(Log.Sender.Navigator, "Search=True");
		}
		return false;
	}
}
