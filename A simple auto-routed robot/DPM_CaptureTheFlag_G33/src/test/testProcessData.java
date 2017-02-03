package test;

import java.util.ArrayList;
import java.util.Arrays;

import Navigation.Odometer;
import wifi.ProcessData;
import wifi.Transmission;

public class testProcessData {
	
	static Double[][] myArray={{105.0, 105.0},{105.0, 165.0},{135.0, 165.0},{135.0, 105.0},{165.0, 105.0},{165.0, 165.0}};
	private static ArrayList <Double[]>  searchWaypoint= new ArrayList<Double[]> ();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		for(int i =0;i<myArray.length;i++){
			searchWaypoint.add(myArray[i]);
			System.out.println("\nIn loop:");
			for(Double[] wp1:searchWaypoint){
				System.out.print(Arrays.toString(wp1));
			}
		}
		
		System.out.println("\nFinal:");
		for(Double[] wp1:searchWaypoint){
			System.out.print(Arrays.toString(wp1));
		}
		
		
//		pd.process();

	}

}
