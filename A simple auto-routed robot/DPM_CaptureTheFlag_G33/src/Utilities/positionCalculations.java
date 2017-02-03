///**
// * 
// */
//package Utilities;
//
//import java.util.Arrays;
//
///**
// * @author Retina
// *
// */
//public class positionCalculations {
//	
//	private double[] getAbsolutePos(double distance, double x, double y, double heading){
//		//return absolute position of the object in the map
//
//		double[] aPos = new double[2];
//		//		double heading =  normalizedAngle(odo.getTheta());
//		//		double x = odo.getX();
//		//		double y = odo.getY();
//		distance=distance+9;
//		double obX;
//		double obY;
//		double rX;
//		double rY;
//
//		rX=distance*Math.sin(Math.toRadians(heading));
//		rY=distance*Math.cos(Math.toRadians(heading));
//
//		obX=x+rX;
//		obY=y+rY;
//
//		aPos[0]=obX;
//		aPos[1]=obY;
//
//		return aPos;
//
//	}
//
//	private double normalizedAngle(double angle){
//
//		//Convert the angle into a 360 degrees system
//
//		angle=angle%360;
//
//		return angle;
//
//	}
//	
//	/**
//	 * Check if what US is looking is a wall or not
//	 * 
//	 * @param distance seen distance
//	 * @return it is a wall or not
//	 */
//	public boolean isInSearchZone(double distance){
//
//		double heading =normalizedAngle(odo.getTheta());
//		double x = odo.getX();
//		double y = odo.getY();
//		double aPos[]=new double [2]; //absolute position array
//		double obX;
//		double obY;
//
//		aPos=getAbsolutePos(distance, x, y, heading);
//
//		obX=aPos[0];
//		obY=aPos[1];
//
//		Log.log(Log.Sender.avoidance, "Object position: "+Arrays.toString(aPos));
//		
//		Log.log(Log.Sender.avoidance, "Lower Y: "+opponentHomeZoneBL_Y);
//		Log.log(Log.Sender.avoidance, "Upper Y: "+opponentHomeZoneTR_Y);
//		Log.log(Log.Sender.avoidance, "Left X: "+opponentHomeZoneBL_X);
//		Log.log(Log.Sender.avoidance, "Right X: "+opponentHomeZoneTR_X);
//
//		if(obX>=opponentHomeZoneBL_X && obX<=opponentHomeZoneTR_X && obY>=opponentHomeZoneBL_Y && obY<=opponentHomeZoneTR_Y){
//			Log.log(Log.Sender.avoidance, "Object in Zone");
//			return true;
//		}
//		else{
//			Log.log(Log.Sender.avoidance, "****Object OUT Zone****");
//			Log.log(Log.Sender.avoidance, "Object OUT Zone");
//			if(obX>=opponentHomeZoneBL_X){
//				Log.log(Log.Sender.avoidance, "Left OK");
//			}
//			else{
//				Log.log(Log.Sender.avoidance, "Left NOT OK");
//			}
//			
//			if(obX<=opponentHomeZoneTR_X){
//				Log.log(Log.Sender.avoidance, "Right OK");
//			}
//			else{
//				Log.log(Log.Sender.avoidance, "Right NOT OK");
//			}
//			
//			if(obY>=opponentHomeZoneBL_Y){
//				Log.log(Log.Sender.avoidance, "Lower OK");
//			}
//			else{
//				Log.log(Log.Sender.avoidance, "Lower NOT OK");
//			}
//			if(obY<=opponentHomeZoneTR_Y){
//				Log.log(Log.Sender.avoidance, "Upper OK");
//			}
//			else{
//				Log.log(Log.Sender.avoidance, "Upper NOT OK");
//			}
//		}
//		
//		return false;
//	}
//	
//
//	/**
//	 * Check if what US is looking is a wall or not
//	 * 
//	 * @param distance seen distance
//	 * @return it is a wall or not
//	 */
//	public boolean isWall(double distance){
//
//		double heading =normalizedAngle(odo.getTheta());
//		double x = odo.getX();
//		double y = odo.getY();
//		double aPos[]=new double [2]; //absolute position array
//		double obX;
//		double obY;
//
//		aPos=getAbsolutePos(distance, x, y, heading);
//
//		obX=aPos[0];
//		obY=aPos[1];
//
//		Log.log(Log.Sender.avoidance, "object position: "+Arrays.toString(aPos));
//
//		if(obX>=rightWall_x-10){
//			Log.log(Log.Sender.avoidance, "it's rightWall_x");
//			return true;
//		}
//		else{
//			Log.log(Log.Sender.avoidance, "it's not rightWall_x");}
//
//		if(obX<=leftWall_x+10){
//			Log.log(Log.Sender.avoidance, "it's leftWall_x");
//			return true;
//		}
//		else{
//			Log.log(Log.Sender.avoidance, "it's not leftWall_x");}
//		if(obY>=upperWall_y-10){
//			Log.log(Log.Sender.avoidance, "it's upperWall_y");
//			return true;
//		}
//		else{
//			Log.log(Log.Sender.avoidance, "it's not upperWall_y");}
//		if(obY<=lowerWall_y+10){
//			Log.log(Log.Sender.avoidance, "it's lowerWall_y");
//			return true;
//		}
//		else{
//			Log.log(Log.Sender.avoidance, "it's not lowerWall_y");}
//
//
//		return false;
//	}
//
//}
