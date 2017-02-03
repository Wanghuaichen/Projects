import java.awt.*;
public class Store {
	private static int shopWidth = 4;
	private static int buttonSize = 52;
	private static int cellSpace= 2;
	private static int awayFromRoom= 29;
	private static int iconSize = 20;
	private static int iconSpace= 25;
	private static int iconTextY= 15;
	private static int itemIn= 4;
	private static int heldID= -1;
	private static int realID= -1;
	private static int[] buttonID = {Map.airTowerLaser,Map.airTowerSlow,Map.airTowerFire,/*Map.airAir,Map.airAir,Map.airAir,Map.airAir,*/Map.airTrashCan};
	private static int[] buttonPrice = {10, 20, 30, /*0, 0, 0, 0, */0};
	
	
	public Rectangle[] button = new Rectangle[shopWidth];
	public Rectangle buttonHealth;
	public Rectangle buttonCoins;
	
	private boolean holdsItem= false;
	
	public Store(){
		define();
	}
	
	public void click(int mouseButton){
		if(mouseButton==1){ //1 is the life #
			for(int i=0; i<button.length; i++){
				if (button[i].contains(Map.mse)){
					if (buttonID[i] != Map.airAir){
						if (buttonID[i]== Map.airTrashCan){//delete item
							holdsItem=false;
	
						} else {
							heldID=buttonID[i];
							realID=i;
							holdsItem=true;
						} 
					} 
				}
			}
		
			
			if(holdsItem){
				if(Map.coinage >= buttonPrice[realID]){
					for (int y=0; y<Map.screen.block.length; y++){
						for (int x=0; x<Map.screen.block[0].length; x++){
							if(Map.screen.block[y][x].contains(Map.mse)){
								if(Map.screen.block[y][x].getGroundID() != Map.groundRoad && Map.screen.block[y][x].getAirID() == Map.airAir){
									Map.screen.block[y][x].setAirID(heldID);
									Map.coinage -= buttonPrice[realID];
									
									holdsItem=false;
								}
								
							}
							
						}	
					}
				}
			}	
		} 
	}
	
	public static void click2(int x, int y){
		if(x<=12 && y<=7){
			//System.out.println(x+" "+y);
		}
	}
	
	public void define(){
		for (int i=0;i<button.length;i++){
			button[i]= new Rectangle((Map.myWidth/2)-((shopWidth*buttonSize+cellSpace)/2)+((buttonSize+cellSpace)*i),(Map.screen.block[Map.screen.worldHeight-1][0].y)+Map.screen.blockSize+awayFromRoom,buttonSize,buttonSize);
			
		}
		buttonHealth = new Rectangle (Map.screen.block[0][0].x-1,button[0].y,iconSize,iconSize);
		buttonCoins = new Rectangle (Map.screen.block[0][0].x-1,button[0].y+button[0].height-iconSize,iconSize,iconSize);
	}
	
	public void draw(Graphics g){
		for (int i=0;i<button.length;i++){
			if (button[i].contains(Map.mse)){
				g.setColor(new Color(100, 100, 200, 170));
				g.fillRect(button[i].x, button[i].y, button[i].width, button[i].height);
			}
			
			g.drawImage(Map.picture_res[0], button[i].x, button[i].y, button[i].width, button[i].height,null);
			if (buttonID[i] != Map.airAir) g.drawImage(Map.picture_air[buttonID[i]], button[i].x+itemIn, button[i].y+itemIn, button[i].width-itemIn*2, button[i].height-itemIn*2,null);
			if (buttonPrice[i]>0) {
				g.setColor(new Color(255,255,255));
				g.setFont(new Font("Arial", Font.BOLD, 14));
				
				g.drawString("$"+buttonPrice[i]+"", button[i].x+itemIn, button[i].y+itemIn+10);
			}
		}
		g.drawImage(Map.picture_res[1], buttonHealth.x+iconSpace, buttonHealth.y, buttonHealth.width, buttonHealth.height,null);
		g.drawImage(Map.picture_res[2],buttonCoins.x+iconSpace, buttonCoins.y, buttonCoins.width, buttonCoins.height,null);
		g.setFont(new Font("Calibri", Font.BOLD,16));
		g.setColor(new Color(255,255,255));
		g.drawString("" + Map.health, buttonHealth.x+buttonHealth.width+iconSpace, buttonHealth.y+iconTextY);          
		g.drawString("" + Map.coinage, buttonCoins.x+buttonCoins.width+iconSpace, buttonCoins.y+iconTextY);

		if (holdsItem){
			g.drawImage(Map.picture_air[heldID], Map.mse.x-((button[0].width-(itemIn*2))/2)+itemIn, Map.mse.y-((button[0].width-(itemIn*2))/2)+itemIn, button[0].width-itemIn*2, button[0].height-itemIn*2, null);
		}
	}
}
