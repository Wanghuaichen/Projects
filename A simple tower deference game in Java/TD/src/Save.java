import java.io.*;
import java.util.*;

public class Save {
	public void loadSave(File loadPath){
		try{
		Scanner loadScanner = new Scanner(loadPath);
		
		while(loadScanner.hasNext()){
			Map.killsToWin=loadScanner.nextInt();
			
			for (int y=0;y<Map.screen.block.length;y++){
				for (int x=0;x<Map.screen.block[0].length;x++){
	
					Map.screen.block[y][x].setGroundID(loadScanner.nextInt());
					
				}
			}
			
			for (int y=0;y<Map.screen.block.length;y++){
				for (int x=0;x<Map.screen.block[0].length;x++){
					
				    Map.screen.block[y][x].setAirID(loadScanner.nextInt());
				    
				}
			}
		}
		loadScanner.close();
		}
		catch(Exception e) {}
	}
}
