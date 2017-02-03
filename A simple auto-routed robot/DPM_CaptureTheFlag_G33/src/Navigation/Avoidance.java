package Navigation;

import java.util.Arrays;
import java.util.Stack;

import CaptureTheFlag.CaptureTheFlag;
import Navigation.Navigation.State;
import Utilities.Log;
import Utilities.UltrasonicPoller;
import Utilities.robotControls;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * 
 * Class allows the robot to avoid an obstacle in its path
 *
 */
public class Avoidance{
	//objects
	private Odometer odometer;
	private robotControls rc;
	private UltrasonicPoller rightUspoller;
	private UltrasonicPoller leftUspoller;
	private ObjectSearch search;

	//double
	private double iniTheta;

	//int

	//boolean
	private boolean cleared= false;


	EV3LargeRegulatedMotor leftMotor = CaptureTheFlag.getLeftMotor();
	EV3LargeRegulatedMotor rightMotor = CaptureTheFlag.getRightMotor();
	private Stack<Double[]> waypoints=CaptureTheFlag.waypoints;

	//wall positions
	double rightWall_x=CaptureTheFlag.rightWall_x;
	double leftWall_x=CaptureTheFlag.leftWall_x;
	double upperWall_y=CaptureTheFlag.upperWall_y;
	double lowerWall_y=CaptureTheFlag.lowerWall_y;

	int preAvoidDist=CaptureTheFlag.preAvoidDist;
	int postAvoidDist=CaptureTheFlag.postAvoidDist;
	int bufferAvoidDist=CaptureTheFlag.bufferAvoidDist;
	int wallAvoidBandwidth=CaptureTheFlag.wallAvoidBandwidth;
	int avoidedDistance=CaptureTheFlag.avoidedDistance;


	private double[] position = new double[3];	
	private int[] wp = new int[3];
	private int[] bufferwp = new int[3];
	private static final Double [] newWaypoint_1= new Double[3];
	private static final Double [] newWaypoint_2= new Double[3];

	enum State{
		GORIGHT, GOLEFT
	}

	State side;
	public boolean isAvoiding;
	double emergency_thd=CaptureTheFlag.emergency_thd;	

	//constructor
	/**
	 * @param usDistance
	 * @param usData
	 * @param odometer
	 */
	public Avoidance(Odometer odometer, robotControls rc,ObjectSearch search, UltrasonicPoller leftUspoller,UltrasonicPoller rightUspoller ){
		this.odometer = odometer;
		this.rc = rc;
		this.leftUspoller = leftUspoller;
		this.rightUspoller = rightUspoller;
		this.search = search;
	}

	/**
	 * Orient robot to avoid object properly and push next waypoint onto stack
	 * 
	 */
	public void avoid(){

		Log.log(Log.Sender.avoidance, "EMERGENCY: Start Avoid");
		
		iniTheta=odometer.getTheta();
		
		//calculate if must go right or left based on absolute position on field
		//Make sure robot won't crash into wall
		//
		
		side=decidePath();
		
		cleared= false;

		//choose left path...rotate left until right us sees nothing
		Log.log(Log.Sender.avoidance, "EMERGENCY: Start ROTATING "+side);
		while(!cleared){
			switch(side){
			case GOLEFT:
				Log.log(Log.Sender.avoidance, "EMERGENCY: In GOLEFT case");
				if(search.isWall(getRightFilteredData())||search.isWall(getLeftFilteredData())){
					Log.log(Log.Sender.avoidance, "EMERGENCY: Wall this side, change side");
					popEmergencywp();
					side=State.GORIGHT;
				}
				rc.rotateCounterClockwise(40);
				if(leftPathCleared()){
					cleared=true;
				}
				break;
			case GORIGHT:
				Log.log(Log.Sender.avoidance, "EMERGENCY: In GORIGHT case");
				if(search.isWall(getRightFilteredData())||search.isWall(getLeftFilteredData())){
					Log.log(Log.Sender.avoidance, "EMERGENCY: Wall this side, change side");
					popEmergencywp();
					side=State.GOLEFT;
				}
				rc.rotateClockwise(40);
				if(rightPathCleared()){
					cleared=true;
				}
				break;
			}
		}

		Sound.beep();
		//Keep turning a bit to make sure object is cleared
//		rc.waitTime(200);
//		rc.waitTime(2000);

		//Calculate a first waypoint
		wp = (int[]) calcNewWaypoint();
		Log.log(Log.Sender.avoidance, "**EMERGENCY: New Waypoint**");
		Log.log(Log.Sender.avoidance, "EMERGENCY: wp->"+Arrays.toString(wp));
		

		newWaypoint_1[0]=(double)wp[0];
		newWaypoint_1[1]=(double)wp[1];
		newWaypoint_1[2]=1.0;
		
		
		bufferwp=calcNewBufferWaypoint(wp,iniTheta);
		
		Log.log(Log.Sender.avoidance, "**EMERGENCY: Buffer Waypoint**");
		Log.log(Log.Sender.avoidance, "EMERGENCY: wp->"+Arrays.toString(wp));
		
		newWaypoint_2[0]=(double)bufferwp[0];
		newWaypoint_2[1]=(double)bufferwp[1];
		newWaypoint_2[2]=1.0;
		
		waypoints.push(newWaypoint_2);
		waypoints.push(newWaypoint_1);
		
		isAvoiding=true;

		//go back to navigation
	}

	

	private boolean rightPathCleared() {
		//Check if object is totally cleared while rotating right
		Log.log(Log.Sender.avoidance, "Check right Path...");
		Log.log(Log.Sender.avoidance, "L_Us:"+getLeftFilteredData());
		Log.log(Log.Sender.avoidance, "R_Us:"+getRightFilteredData());
		//if left sensor is still seeing something, not avoided yet
		if(getLeftFilteredData()>avoidedDistance){
			if(getRightFilteredData()>avoidedDistance){
				Log.log(Log.Sender.avoidance, "RightPathCleared!!!");
				return true;}
			else{
				iniTheta=odometer.getTheta();
			}
		}

		return false;
	}

	private boolean leftPathCleared() {
		//Check if object is totally cleared while rotating left
		Log.log(Log.Sender.avoidance, "Check left Path...");
		Log.log(Log.Sender.avoidance, "L_Us:"+getLeftFilteredData());
		Log.log(Log.Sender.avoidance, "R_Us:"+getRightFilteredData());

		//if right sensor is still seeing something, not avoided yet
		if(getRightFilteredData()>avoidedDistance){
			if(getLeftFilteredData()>avoidedDistance){
				Log.log(Log.Sender.avoidance, "LeftPathCleared!!!");
				return true;}
			else{
				iniTheta=odometer.getTheta();
			}
		}

		return false;

	}

	private State decidePath() {
		this.odometer.getPosition(position, new boolean[] { true, true, true,true });

		double cuX=position[0]; //Current position in x, polled from odometer
		double cuY=position[1]; //Current position in y, polled from odometer
		
		boolean low_tClose=false;  //lower
		boolean uw_tClose=false;  //upper
		boolean rw_tClose=false;  //right
		boolean lw_tClose=false; //left
		
		double dist_low=Math.abs(cuY-lowerWall_y);
		double dist_uw=Math.abs(cuY-upperWall_y);
		double dist_rw=Math.abs(cuX-rightWall_x);
		double dist_lw=Math.abs(cuX-leftWall_x);
		
		int countCloseWall=0;
		
		if(dist_rw<wallAvoidBandwidth){
			Log.log(Log.Sender.avoidance, "RightWall too close");
			rw_tClose=true;
		}
		//Check if close to left wall
		else if(dist_lw<wallAvoidBandwidth){
			Log.log(Log.Sender.avoidance, "leftWall too close");
			lw_tClose=true;
			countCloseWall+=1;
		}
		//Check if close to upper wall
		else if(dist_uw<wallAvoidBandwidth){
			Log.log(Log.Sender.avoidance, "upperWall too close");
			uw_tClose=true;
			countCloseWall+=1;
		}
		//Check if close to lower wall
		else if(dist_low<wallAvoidBandwidth){
			Log.log(Log.Sender.avoidance, "lowerWall too close");
			low_tClose=true;
			countCloseWall+=1;
		}
		else{
			Log.log(Log.Sender.avoidance, "None too close");
			return sensorPreferedSide();
		}
		
		
		if(countCloseWall==1){
			if(odometer.travelPositiveY()){
				Log.log(Log.Sender.avoidance, "TravelPositiveY");
				//robot is going in increasing y
				if(odometer.travelPositiveX()){
					Log.log(Log.Sender.avoidance, "TravelPositiveX");
					if(rw_tClose){
						return State.GOLEFT;
					}
					else if(lw_tClose){
						return State.GORIGHT;
					}
					else if(uw_tClose){
						return State.GORIGHT;
					}
					else if(low_tClose){
						return State.GOLEFT;
					}
				}
				else{
					Log.log(Log.Sender.avoidance, "TravelNegativeX");
					if(rw_tClose){
						return State.GOLEFT;
					} 
					else if(lw_tClose){
						return State.GORIGHT;
					}
					else if(uw_tClose){
						return State.GOLEFT;
					}
					else if(low_tClose){
						return State.GORIGHT;
					}
				}
			}
			else{
				Log.log(Log.Sender.avoidance, "TravelNegativeY");
				if(odometer.travelPositiveX()){
					Log.log(Log.Sender.avoidance, "TravelPositiveX");
					if(rw_tClose){
						return State.GORIGHT;
					} 
					else if(lw_tClose){
						return State.GOLEFT;
					}
					else if(uw_tClose){
						return State.GORIGHT;
					}
					else if(low_tClose){
						return State.GOLEFT;
					}
				}
				else{
					Log.log(Log.Sender.avoidance, "TravelNegativeX");
					if(rw_tClose){
						return State.GOLEFT;
					} 
					else if(lw_tClose){
						return State.GORIGHT;
					}
					else if(uw_tClose){
						return State.GOLEFT;
					}
					else if(low_tClose){
						return State.GORIGHT;
					}
				}
			}
		}
		
	
		//Combination of walls too close
		else{
			
			if(rw_tClose&&uw_tClose){
				if(dist_rw>dist_uw){
					
				}
				else{
					
				}
	
				return State.GOLEFT;
			} 
			else if(rw_tClose&&low_tClose){
				if(dist_rw>dist_low){

				}
				else{

				}
				return State.GOLEFT;
			}
			else if(lw_tClose&&uw_tClose){
				if(dist_lw>dist_uw){

				}
				else{

				}
				
				return State.GOLEFT;
			}
			else if(lw_tClose&&low_tClose){
				if(dist_lw>dist_low){

				}
				else{

				}
				return State.GOLEFT;
			}
				
		}
		
		return State.GOLEFT;
	}

	private State sensorPreferedSide() {
		// TODO Auto-generated method stub
		Log.log(Log.Sender.avoidance, "Checking Sensor Prefered side");
		
		if(getRightFilteredData()>getLeftFilteredData()){
			return State.GORIGHT;
		}
		else{
			return State.GOLEFT;
		}
		
	}

	private int[] calcNewWaypoint(){

		int[] wp=new int[2];

		this.odometer.getPosition(position, new boolean[] { true, true, true,true });

		double cuX=position[0]; //Current position in x, polled from odometer
		double cuY=position[1]; //Current position in y, polled from odometer
		double cTheta=position[2]; //Current direction angle, polled from odometer

		double dx=postAvoidDist*Math.sin(Math.toRadians(cTheta));
		double dy=postAvoidDist*Math.cos(Math.toRadians(cTheta));

		wp[0]=(int) (cuX+dx);
		wp[1]=(int) (cuY+dy);

		return wp;
	}
	
	private int[] calcNewBufferWaypoint(int[] wp1, double iTheta) {
		
		Log.log(Log.Sender.avoidance, "**Calculate buffer**");
		int[] wp2=new int[2];
		
		double wp1X=wp1[0];
		double wp1Y=wp1[1];
		
		double dx=bufferAvoidDist*Math.sin(Math.toRadians(iTheta));
		double dy=bufferAvoidDist*Math.cos(Math.toRadians(iTheta));

		wp2[0]=(int) (wp1X+dx);
		wp2[1]=(int) (wp1Y+dy);

		return wp2;
		
	}
	
	private void popEmergencywp(){
		//remove all emergency waypoints
		while(waypoints.peek()[2]==1.0){
			waypoints.pop();
		}
	}
	
	

	public double getRightFilteredData() {		

		//get median value of the list to reduce noise
		double distance=rightUspoller.distance;

		if (distance>100)
			distance=100;

		return distance;
	}

	public double getLeftFilteredData() {		

		//get median value of the list to reduce noise
		double distance=leftUspoller.distance;

		if (distance>100)
			distance=100;

		return distance;
	}
}
