package Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 *
 *Class used to create log files on the EV3 directory
 *
 */
public class Log {

	static PrintStream writer = System.out;

	/**
	 * Type of object sending the info to be printed in log
	 *
	 */
	public static enum Sender {
		odometer, Navigator, usSensor, avoidance, init, processData
	}

	static boolean printInitialization;
	static boolean printOdometer;
	static boolean printNavigator;
	static boolean printUsSensor;
	static boolean printAvoidance;
	static boolean printProcessData;

	public static void log(Sender sender, String message) {
		long timestamp = System.currentTimeMillis() % 100000;

		if (sender == Sender.Navigator && printNavigator) {
			writer.println("NAV::" + timestamp + ": " + message);
		}
		if (sender == Sender.odometer && printOdometer) {
			writer.println("ODO::" + timestamp + ": " + message);
		}
		if (sender == Sender.usSensor && printUsSensor) {
			writer.println("US::" + timestamp + ": " + message);
		}
		if (sender == Sender.avoidance && printAvoidance){
			writer.println("OA::" + timestamp + ": " + message);
		}
		if (sender == Sender.init && printInitialization){
			writer.println("Init::" + timestamp + ": " + message);
		}
		if (sender == Sender.processData && printProcessData){
			writer.println("Init::" + timestamp + ": " + message);
		}
	}

	public static void setLogging(boolean init,boolean nav, boolean odom, boolean us,boolean avoid,boolean pd) {
		printInitialization = init;
		printNavigator = nav;
		printOdometer = odom;
		printUsSensor = us;
		printAvoidance = avoid;
		printProcessData = pd;
	}

	public static void setLogWriter(String filename) throws FileNotFoundException {
		writer = new PrintStream(new File(filename));
	}

}
