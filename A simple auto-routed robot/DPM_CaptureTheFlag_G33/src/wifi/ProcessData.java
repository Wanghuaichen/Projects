/**
 * 
 */
package wifi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import Navigation.Odometer;
import Utilities.Log;
import Utilities.robotControls;
import CaptureTheFlag.CaptureTheFlag;

/**
 * @author Retina
 *
 */
public class ProcessData {

	Transmission trans;
	Odometer odo;

	int iKey;
	/* 0=BL
	 * 1=BR
	 * 2=TR
	 * 3=TL
	 */
	
	double [] startSearchwp= new double[2];
	
	double upperWall=CaptureTheFlag.upperWall_y;
	double lowerWall=CaptureTheFlag.lowerWall_y;


	//	public static final Double [] zoneWaypoint={90.0,90.0,0.0};

	private static final Double [] dropZoneCenter=CaptureTheFlag.dropZoneCenter;
	
	private static final Double [] searchZoneEntry=CaptureTheFlag.searchZoneEntry;

	private ArrayList <Double[]>  searchWaypoint= new ArrayList<Double[]> ();
	
//	static Stack<Double[]> waypoints=CaptureTheFlag.waypoints;
	
	public static Stack<Double[]> waypoints= CaptureTheFlag.waypoints;
	
	public static double homeZoneBL_X;
	public static double homeZoneBL_Y;
	public static double homeZoneTR_X;
	public static double homeZoneTR_Y;
	public static double opponentHomeZoneBL_X;
	public static double opponentHomeZoneBL_Y;
	public static double opponentHomeZoneTR_X;
	public static double opponentHomeZoneTR_Y;
	public static double dropZone_X;
	public static double dropZone_Y;
	
	public static int searchZoneSize_X;
	public static int searchZoneSize_Y;
	public static int wpt_count;
	
	public static int opponentFlagType;
	public static int flagType;
	
	public static StartCorner startingCorner;
	
//	public static final double tSize=30.48;
	public static final double tSize=30;


	public ProcessData(Transmission trans,Odometer odo){
		this.trans=trans;
		this.odo=odo;
	}

	public void process(){
		
		searchZoneSize_X=Math.abs(trans.opponentHomeZoneBL_X-trans.opponentHomeZoneTR_X);
		searchZoneSize_Y=Math.abs(trans.opponentHomeZoneBL_Y-trans.opponentHomeZoneTR_Y);
		
		Log.log(Log.Sender.init,"SearchZone Size "+searchZoneSize_Y+" x "+searchZoneSize_X);
		
		wpt_count=Math.min(2*searchZoneSize_X, 2*searchZoneSize_Y);
		
		Log.log(Log.Sender.init,"wp count"+wpt_count);
		
		homeZoneBL_X=(trans.homeZoneBL_X)*tSize;
		homeZoneBL_Y=(trans.homeZoneBL_Y)*tSize;
		homeZoneTR_X=(trans.homeZoneTR_X)*tSize;
		homeZoneTR_Y=(trans.homeZoneTR_Y)*tSize;
		
		dropZone_X=(trans.dropZone_X)*tSize;
		dropZone_Y=(trans.dropZone_Y)*tSize;
		
		opponentFlagType=trans.opponentFlagType;
		flagType=trans.flagType;
		
		opponentHomeZoneBL_X=(trans.opponentHomeZoneBL_X)*tSize;
		opponentHomeZoneBL_Y=(trans.opponentHomeZoneBL_Y)*tSize;
		opponentHomeZoneTR_X=(trans.opponentHomeZoneTR_X)*tSize;
		opponentHomeZoneTR_Y=(trans.opponentHomeZoneTR_Y)*tSize;
		
		startingCorner=trans.startingCorner;
		
		Log.log(Log.Sender.init,"\n**Converted Trans. Values***");
		Log.log(Log.Sender.init,"Start: " + startingCorner.toString());
		Log.log(Log.Sender.init,"HZ: " + homeZoneBL_X + " " + homeZoneBL_Y + " " + homeZoneTR_X + " " + homeZoneTR_Y);
		Log.log(Log.Sender.init,"OHZ: " + opponentHomeZoneBL_X + " " + opponentHomeZoneBL_Y + " " + opponentHomeZoneTR_X + " " + opponentHomeZoneTR_Y);
		Log.log(Log.Sender.init,"DZ: " + dropZone_X + " " + dropZone_Y);
		Log.log(Log.Sender.init,"Flg: " + flagType + " " + opponentFlagType);
		
		
		Log.log(Log.Sender.processData, "\nProcessing Data...");

		Log.log(Log.Sender.processData, "\nInitial Position...");
		//Set odometer
		odo.setPosition(setInitialPos(), new boolean [] {true, true, true});
		
		Log.log(Log.Sender.processData, "Initial Position:");

		//compute search waypoints
		setSearchWaypoints(getSearchWaypoints());
		
//		Log.log(Log.Sender.processData, "Search wps:"+DoubleDoubleArrayContent(searchWaypoint));
	
		String keyName=getKeyName(iKey);
		
		//compute search zone entry
		setSearchZoneEntry(getSearchZoneEntry(startSearchwp,keyName));
		
		Log.log(Log.Sender.processData, "Search wps:"+DoubleArrayContent(searchZoneEntry));
		
		//compute center of dropzone
		setDropZoneCenter(getDropZoneCenter());
		
		Log.log(Log.Sender.processData, "Search wps:"+DoubleArrayContent(dropZoneCenter));

		//push into waypoints stack
		initializeStack();
		
//		System.out.print(stackContent());
		
		Log.log(Log.Sender.processData, "Stack Content:"+stackContent());
	}


	private String getKeyName(int key) {
		/* 0=BL
		 * 1=BR
		 * 2=TR
		 * 3=TL
		 */
		
		String name=null;
		
		if(key==0){
			name="BL";
		}
		else if(key==1){
			name="BR";
		}
		else if(key==2){
			name="TR";
		}
		else if(key==3){
			name="TL";
		}


		return name;
	}

	private double[] setInitialPos(){
		//fetch robot initial position and set odometer
		
		Log.log(Log.Sender.processData, "Print Starting corner.."+startingCorner);
		
		Log.log(Log.Sender.processData, "Print Starting corner.."+startingCorner.toString());
		
		
		double xCorner=startingCorner.getX();
		double yCorner=startingCorner.getY();
		
//		double xCorner=0;
//		double yCorner=0;
	

		double addTheta=0;
		double addX=0;
		double addY=0;
		
		int cornerId=startingCorner.getId();

		double[] odoPos= new double[3];

		odo.getPosition(odoPos, new boolean [] {true, true, true});

		double odoX=odoPos[0];
		double odoY=odoPos[1];
		double odoTheta=odoPos[2];

		if(cornerId==1){
			addTheta=0;
			addX=odoX;
			addY=odoY;
		}
		else if(cornerId==2){
			addTheta=270;
			addX=-odoY;
			addY=odoX;
		}
		else if(cornerId==3){
			addTheta=180;
			addX=-odoX;
			addY=-odoY;
		}
		else if(cornerId==4){
			addTheta=90;
			addX=odoY;
			addY=-odoX;
		}

		double[] pos ={xCorner+addX,yCorner+addY,(addTheta+odoTheta)%360};

		return pos;
	}

	private double [][] getSearchWaypoints(){

		//Set search waypoints (waypooint inside opponentHomeZone)
//		double [] wp_BL={opponentHomeZoneBL_X+15,opponentHomeZoneBL_Y+15};
//		double [] wp_BR={opponentHomeZoneBL_X+45,opponentHomeZoneBL_Y+15};
//		double [] wp_TR={opponentHomeZoneTR_X-15,opponentHomeZoneTR_Y-15};
//		double [] wp_TL={opponentHomeZoneTR_X-45,opponentHomeZoneTR_Y-15};
		
		double[][] wps=new double[wpt_count][2];
		
		Log.log(Log.Sender.processData, "Computing waypoints...");
		
		int k=0;
		int up=0;
		int upCounter=0;
		for(int i=0;i<wpt_count;i++){
			System.out.println( "up: "+up);
			System.out.println( "k: "+k);
			wps[i][0]=opponentHomeZoneBL_X+15+30*k;
			wps[i][1]=opponentHomeZoneBL_Y+15+30*up*(searchZoneSize_Y-1);
			
			Log.log(Log.Sender.processData, "wps "+i+": "+Arrays.toString(wps[i]));
			
			System.out.println( "wps "+i+": "+Arrays.toString(wps[i]));
			
			System.out.println( "i%2: "+i%2);
		
			if(i%2-1==0){
				k++;
			}
			else{
				up=1;
			}
			
			if(upCounter==2){
				up=0;
				upCounter=0;
			}
			
			if(up==1){
				upCounter++;
			}
			
		}


		//		CaptureTheFlag.searchWaypoint;

		//find closest waypoint

//		double[][] wps={wp_BL,wp_BR,wp_TR, wp_TL};

		int i=getClosestSearchwp(wps);
		startSearchwp=wps[i];
		iKey=i;
		boolean sorted=false;

		int j=0;
		double [][] searchwp=new double[wpt_count][2];

		//assuming navigating in order BL, BR,TR, TL
		//set waypoints in good order into searchWaypoints array
		
		System.out.println("\nSort waypoints...");
		
		while(!sorted){
			
			System.out.println("\nI: "+i);
			System.out.println("J: "+j);
			searchwp[j]=wps[i];
			printArray(searchwp);
			if(i==iKey-1){
				sorted=true;
				break;
			}
			if(i+1==wps.length){
				i=0;
			}
			else{
				i++;
			}
			
			if(j+1==searchwp.length){
				break;
			}
			j++;
		}

		System.out.println("\nExit get Waypoints...");
		
		return searchwp;
	}

	private void setSearchWaypoints(double [][] searchwp){
		
		System.out.println("\nStart Setting...");
		
		int i=0;
		double [] wp= new double[2];

		for(int j=searchwp.length-1;j>=0;j--){
			System.out.println("\nI: "+i);
			System.out.println("J: "+j);
			Double [] tempWp= new Double[3];
			
			wp=searchwp[j];
			
			tempWp[0]=Double.valueOf(wp[0]);
			tempWp[1]=Double.valueOf(wp[1]);
			tempWp[2]=2.0;
			
//			System.out.println("tempWP"+Arrays.toString(tempWp));
			searchWaypoint.add(tempWp);
			
//			System.out.print("\nWhile: ");
//			for(Double[] wp1:searchWaypoint){
//				System.out.print(Arrays.toString(wp1));
//			}
			i++;
		}
//		System.out.println("\nEnd set searchWaypoints...");
	}


	private double[] getSearchZoneEntry(double[] firstwp, String wpID){
		double firstwp_x=firstwp[0];
		double firstwp_y=firstwp[1];

		double[] entry=new double[2];
		double[] temp1=new double[2];
		double[] temp2=new double[2];

		boolean manyPts=false;


		if(wpID.equals("BL")){
			if(opponentHomeZoneBL_Y==lowerWall){
				//only one entry point
				entry[0]=firstwp_x-15;
				entry[1]=firstwp_y;
			}
			else{
				manyPts=true;
				temp1[0]=firstwp_x-15;
				temp1[1]=firstwp_y;

				temp2[0]=firstwp_x;
				temp2[1]=firstwp_y-15;
			}
		}
		else if(wpID.equals("BR")){
			if(opponentHomeZoneBL_Y==lowerWall){
				//only one entry point
				entry[0]=firstwp_x+15;
				entry[1]=firstwp_y;
			}
			else{
				manyPts=true;
				temp1[0]=firstwp_x+15;
				temp1[1]=firstwp_y;

				temp2[0]=firstwp_x;
				temp2[1]=firstwp_y-15;
			}

		}
		else if(wpID.equals("TR")){
			if(opponentHomeZoneTR_Y==upperWall){
				//only one entry point
				entry[0]=firstwp_x+15;
				entry[1]=firstwp_y;
			}
			else{
				manyPts=true;
				temp1[0]=firstwp_x+15;
				temp1[1]=firstwp_y;

				temp2[0]=firstwp_x;
				temp2[1]=firstwp_y+15;
			}
		}
		else if(wpID.equals("TL")){
			if(opponentHomeZoneTR_Y==upperWall){
				//only one entry point
				entry[0]=firstwp_x-15;
				entry[1]=firstwp_y;
			}
			else{
				manyPts=true;
				temp1[0]=firstwp_x-15;
				temp1[1]=firstwp_y;

				temp2[0]=firstwp_x;
				temp2[1]=firstwp_y+15;
			}
		}

		if(manyPts){
			double[][] entryPts={temp1,temp2};

			int i=getClosestSearchwp(entryPts);

			entry=entryPts[i];
		}

		return entry;	
	}
	
	private int getClosestSearchwp(double[][] wps){
		double[] pos= new double[3];

		odo.getPosition(pos, new boolean [] {true, true, true});
		
//		double[] pos={0,0,0};
		
		int i=0;
		int closestid=0;
		double distance;
		double closestD=0;

		for(double[] wp:wps){
			distance=linearDistance(pos[0],pos[1],wp[0],wp[1]);

			if(i==0)
				closestD=distance;
			if(distance<=closestD)
				closestid=i;
			i++;
		}

		return closestid;
	}
	
	private void setSearchZoneEntry(double[] entry){
		searchZoneEntry[0]=entry[0];
		searchZoneEntry[1]=entry[1];
		searchZoneEntry[2]=2.0;
	}
	
	private double [] getDropZoneCenter(){
		
		double [] center= new double[2];
		
		center[0]=dropZone_X+15;
		center[1]=dropZone_Y+15;
		
		return center;
	}
	
	private void setDropZoneCenter(double[] center){
		dropZoneCenter[0]=center[0];
		dropZoneCenter[1]=center[1];
		dropZoneCenter[2]=0.0;
//		System.out.println("\nDropZone: "+Arrays.toString(dropZoneCenter));
	}

	private double linearDistance(double x,double y,double cuX,double cuY){
		//calculate linear distance between current position and target position

		double fwdDist=Math.sqrt(Math.pow(x-cuX, 2)+Math.pow(y-cuY, 2));

		return fwdDist;
	}

	private void initializeStack(){
		waypoints.push(dropZoneCenter);
		
		waypoints.push(searchZoneEntry);
		
		for(Double[] wp:searchWaypoint){
			waypoints.push(wp);
		}
		
		waypoints.push(searchZoneEntry);

	}
	
	private static String stackContent(){
		
		String StackContent="\nBottom->";
		
		
		for(Double[] wp:waypoints){
			StackContent+=""+Arrays.toString(wp);
		}
		
		return StackContent;
		
	}
	
	private static String DoubleDoubleArrayContent(Double[][] array){
		
		String ArrayContent="\n{";
		
		
		for(int i=0;i<array.length;i++){
			ArrayContent+=","+Arrays.toString(array[i]);
		}
		
		ArrayContent+="}";
		
		return ArrayContent;
		
	}
	
	private static String DoubleArrayContent(Double[]array){
		
		String ArrayContent="\n{";
		
		for(int i=0;i<array.length;i++){
			ArrayContent+=","+array[i];
		}
		
		ArrayContent+="}";
		
		return ArrayContent;
		
	}
	
	private static void printArray(double[][] array){
		System.out.print("\n{");
		
		for(int i=0;i<array.length;i++){
			System.out.print(Arrays.toString(array[i]));
		}
		System.out.print("}");
	}

}