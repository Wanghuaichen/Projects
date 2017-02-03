package Navigation;



import java.util.Arrays;
import java.util.Stack;

import CaptureTheFlag.CaptureTheFlag;
import Localization.USLocalizer;
import Utilities.Log;
import Utilities.UltrasonicPoller;
import Utilities.robotControls;


/**
 * 
 * Class allows robot to search for a certain type of object
 *
 */
public class ObjectSearch {
	//objects
	private UltrasonicPoller rightUspoller;
	private UltrasonicPoller leftUspoller;

	private Odometer odo;
	private ObjectDetection od;
	private robotControls rc;
	private static double error = 3;
	private static double USD = 7.5;
	private static double USL = 8;
	private static Stack<Double[]> waypoints=CaptureTheFlag.waypoints;

	//double


	//booleans
	/**
	 * Define if robot is searching or not
	 */
	public boolean searching=false;
	boolean found=false;

	double rightWall_x=CaptureTheFlag.rightWall_x;
	double leftWall_x=CaptureTheFlag.leftWall_x;
	double upperWall_y=CaptureTheFlag.upperWall_y;
	double lowerWall_y=CaptureTheFlag.lowerWall_y;
	
	double opponentHomeZoneBL_X=CaptureTheFlag.opponentHomeZoneBL_X;
	double opponentHomeZoneBL_Y=CaptureTheFlag.opponentHomeZoneBL_Y;
	double opponentHomeZoneTR_X=CaptureTheFlag.opponentHomeZoneTR_X;
	double opponentHomeZoneTR_Y=CaptureTheFlag.opponentHomeZoneTR_Y;

	double dirBuffer=3;


	int searchRange=CaptureTheFlag.searchRange;

	enum State{
		RIGHT, LEFT, STRAIGHT
	}

	State direction;

	/**
	 * @param odo Odometer
	 * @param rc Robot Controls
	 * @param leftUsPoll Poller for Left US sensor
	 * @param rightUsPoll Poller for Right US sensor
	 * @param od Object Detection
	 */
	public ObjectSearch(Odometer odo, robotControls rc,  UltrasonicPoller leftUsPoll,
			UltrasonicPoller rightUsPoll, ObjectDetection od ) {
		this.odo = odo;
		this.rc=rc;
		this.leftUspoller = leftUsPoll;
		this.rightUspoller = rightUsPoll;
		this.od = od;
	}

	/**
	 * 
	 */
	public void Search() {
		double iTheta=odo.getTheta();
		double newAngle=0;
		
		Log.log(Log.Sender.avoidance, "Search Iteration");

		//check if something in search range 
		if(!(isWall(leftUSFilteredData()) && isWall(rightUSFilteredData()))){

			if(leftUSFilteredData()<searchRange || rightUSFilteredData()<searchRange){
				//				rc.stopRobot();

				Log.log(Log.Sender.avoidance, "I See an object...");
				
				//find where is the block
				direction=decideDirection();
				
				double dLeft;
				double dRight;
				double firstAngle = odo.getTheta();
				double secondAngle = odo.getTheta();

				switch(direction){
				
				case STRAIGHT:
					//do nothing
					Log.log(Log.Sender.avoidance, "*GO STARIGHT*");
					break;
				
				case LEFT:
<<<<<<< HEAD
					dLeft = leftUSFilteredData();
=======
					dLeft = Math.min(leftUSFilteredData(), rightUSFilteredData());
>>>>>>> testingOdometryCOrrection
					//turn slightly left
					Log.log(Log.Sender.avoidance, "*GO LEFT*");
					while (leftUSFilteredData() < dLeft + error) {
						rc.rotateClockwise(80);
						firstAngle = odo.getTheta();
					}
					rc.stopRobot();
<<<<<<< HEAD
					while (leftUSFilteredData() > dLeft) {
						rc.rotateCounterClockwise(80);
					}
=======
					while (leftUSFilteredData() > dLeft ) {
						rc.rotateCounterClockwise(80);
					}
					dLeft = leftUSFilteredData();
>>>>>>> testingOdometryCOrrection
					while (leftUSFilteredData() < dLeft + error) {
						rc.rotateCounterClockwise(80);
						secondAngle =  odo.getTheta();
					}
					rc.stopRobot();
					
					
					newAngle=(firstAngle+secondAngle)/2 - Math.atan2(USL, USD + dLeft)*(180/Math.PI);
					Log.log(Log.Sender.avoidance, "New angle: "+newAngle );
					rc.turnTo(odo.getTheta(), newAngle,50);
					break;
				
				case RIGHT:
					Log.log(Log.Sender.avoidance, "*GO RIGHT*");
					//turn slightly right
					
<<<<<<< HEAD
					dRight = rightUSFilteredData();
=======
					dRight = Math.min(leftUSFilteredData(), rightUSFilteredData());
>>>>>>> testingOdometryCOrrection
					//turn slightly left
					//Log.log(Log.Sender.avoidance, "*GO LEFT*");
					while (rightUSFilteredData() < dRight + error) {
						rc.rotateCounterClockwise(80);
<<<<<<< HEAD
						firstAngle = odo.getTheta();
=======
						firstAngle = odo.getTheta();	
>>>>>>> testingOdometryCOrrection
					}
					rc.stopRobot();
					while (rightUSFilteredData() > dRight) {
						rc.rotateClockwise(80);
					}
<<<<<<< HEAD
=======
					dRight = rightUSFilteredData();
>>>>>>> testingOdometryCOrrection
					while (rightUSFilteredData() < dRight + error) {
						rc.rotateClockwise(80);
						secondAngle =  odo.getTheta();
					}
					rc.stopRobot();
					
					
					newAngle=(firstAngle+secondAngle)/2 + Math.atan2(USL, USD + dRight)*(180/Math.PI);
					//Log.log(Log.Sender.avoidance, "New angle: "+newAngle );
					rc.turnTo(odo.getTheta(), newAngle,50);
					break;
				}

				//recognize block
				if(recognize()){
					//if true, exit search zone
					terminateSearch();
				}
				else{
					//return to initial angle
					rc.turnTo(odo.getTheta(), iTheta, 80);
//					rc.PushitBack();
				}
			}
		}
	}


	private void terminateSearch() {
		
		while(waypoints.peek()[2]==2.0){
			waypoints.pop();
		}
		
		exitSearchZone();	
	}

	private void exitSearchZone() {
		//set wp out of search zone
		
		double x = odo.getX();
		double y = odo.getY();
		Double[][] exitWps=new Double[2][3];
		
//		if(odo.travelPositiveY()){		
//			//second wp
//			exitWps[1][0]=x+30;
//			exitWps[1][1]=y-60;
//			exitWps[1][2]=3.0;
//			
//			//First wp
//			exitWps[0][0]=x+30;
//			exitWps[0][1]=y;
//			exitWps[0][2]=3.0;	
//		}
//		else{
//			
//			//second wp
//			exitWps[1][0]=x-30;
//			exitWps[1][1]=y-60;
//			exitWps[1][2]=3.0;
//			
//			//First wp
//			exitWps[0][0]=x+30;
//			exitWps[0][1]=y;
//			exitWps[0][2]=3.0;	
//		}
//		
//		for(Double[] wp:exitWps){
//			waypoints.push(wp);
//		}	
	}

	private State decideDirection() {

		double usRight=rightUSFilteredData();
		double usLeft=leftUSFilteredData();

		if(Math.abs(usRight-usLeft)<=dirBuffer){
			return State.STRAIGHT;
		}
		else if(usRight>usLeft){
			return State.LEFT;
		}
		else if(usRight<usLeft){
			return State.RIGHT;
		}
		else{
			return State.STRAIGHT;
		}
	}

	private boolean recognize(){

		if(od.doDetection()){
			searching=false;
			found=true;
//			rc.PickitUp();
			return true;
			}
		else{
			//return to initial state
			//drop the block
//			rc.PickitUp();
			}
		return false;
	}


	private double[] getAbsolutePos(double distance, double x, double y, double heading){
		//return absolute position of the object in the map

		double[] aPos = new double[2];
		//		double heading =  normalizedAngle(odo.getTheta());
		//		double x = odo.getX();
		//		double y = odo.getY();
		distance=distance+9;
		double obX;
		double obY;
		double rX;
		double rY;

		rX=distance*Math.sin(Math.toRadians(heading));
		rY=distance*Math.cos(Math.toRadians(heading));

		obX=x+rX;
		obY=y+rY;

		aPos[0]=obX;
		aPos[1]=obY;

		return aPos;

	}

	private double normalizedAngle(double angle){

		//Convert the angle into a 360 degrees system

		angle=angle%360;

		return angle;

	}
	
	/**
	 * Check if what US is looking is a wall or not
	 * 
	 * @param distance seen distance
	 * @return it is a wall or not
	 */
	public boolean isInSearchZone(double distance){

		double heading =normalizedAngle(odo.getTheta());
		double x = odo.getX();
		double y = odo.getY();
		double aPos[]=new double [2]; //absolute position array
		double obX;
		double obY;

		aPos=getAbsolutePos(distance, x, y, heading);

		obX=aPos[0];
		obY=aPos[1];

		Log.log(Log.Sender.avoidance, "Object position: "+Arrays.toString(aPos));
		
		Log.log(Log.Sender.avoidance, "Lower Y: "+opponentHomeZoneBL_Y);
		Log.log(Log.Sender.avoidance, "Upper Y: "+opponentHomeZoneTR_Y);
		Log.log(Log.Sender.avoidance, "Left X: "+opponentHomeZoneBL_X);
		Log.log(Log.Sender.avoidance, "Right X: "+opponentHomeZoneTR_X);

		if(obX>=opponentHomeZoneBL_X && obX<=opponentHomeZoneTR_X && obY>=opponentHomeZoneBL_Y && obY<=opponentHomeZoneTR_Y){
			Log.log(Log.Sender.avoidance, "Object in Zone");
			return true;
		}
		else{
			Log.log(Log.Sender.avoidance, "****Object OUT Zone****");
			Log.log(Log.Sender.avoidance, "Object OUT Zone");
			if(obX>=opponentHomeZoneBL_X){
				Log.log(Log.Sender.avoidance, "Left OK");
			}
			else{
				Log.log(Log.Sender.avoidance, "Left NOT OK");
			}
			
			if(obX<=opponentHomeZoneTR_X){
				Log.log(Log.Sender.avoidance, "Right OK");
			}
			else{
				Log.log(Log.Sender.avoidance, "Right NOT OK");
			}
			
			if(obY>=opponentHomeZoneBL_Y){
				Log.log(Log.Sender.avoidance, "Lower OK");
			}
			else{
				Log.log(Log.Sender.avoidance, "Lower NOT OK");
			}
			if(obY<=opponentHomeZoneTR_Y){
				Log.log(Log.Sender.avoidance, "Upper OK");
			}
			else{
				Log.log(Log.Sender.avoidance, "Upper NOT OK");
			}
		}
		
		return false;
	}
	

	/**
	 * Check if what US is looking is a wall or not
	 * 
	 * @param distance seen distance
	 * @return it is a wall or not
	 */
	public boolean isWall(double distance){

		double heading =normalizedAngle(odo.getTheta());
		double x = odo.getX();
		double y = odo.getY();
		double aPos[]=new double [2]; //absolute position array
		double obX;
		double obY;

		aPos=getAbsolutePos(distance, x, y, heading);

		obX=aPos[0];
		obY=aPos[1];

		Log.log(Log.Sender.avoidance, "object position: "+Arrays.toString(aPos));

		if(obX>=rightWall_x-10){
			Log.log(Log.Sender.avoidance, "it's rightWall_x");
			return true;
		}
		else{
			Log.log(Log.Sender.avoidance, "it's not rightWall_x");}

		if(obX<=leftWall_x+10){
			Log.log(Log.Sender.avoidance, "it's leftWall_x");
			return true;
		}
		else{
			Log.log(Log.Sender.avoidance, "it's not leftWall_x");}
		if(obY>=upperWall_y-10){
			Log.log(Log.Sender.avoidance, "it's upperWall_y");
			return true;
		}
		else{
			Log.log(Log.Sender.avoidance, "it's not upperWall_y");}
		if(obY<=lowerWall_y+10){
			Log.log(Log.Sender.avoidance, "it's lowerWall_y");
			return true;
		}
		else{
			Log.log(Log.Sender.avoidance, "it's not lowerWall_y");}


		return false;
	}

	
	//Was useful for LCD info
//	private double[] gettingAbsPos(){
//
//		double heading =normalizedAngle(odo.getTheta());
//		double x = odo.getX();
//		double y = odo.getY();
//		double distance=getFilteredData();
//
//		double [] array= getAbsolutePos(distance, x, y, heading);;
//
//		return array;
//
//	}

	private double leftUSFilteredData() {		

		double distance=leftUspoller.distance;

		if (distance>60)
			distance=60;

		return distance;
	}	

	private double rightUSFilteredData() {		

		double distance=rightUspoller.distance;

		if (distance>60)
			distance=60;

		return distance;
	}	
}