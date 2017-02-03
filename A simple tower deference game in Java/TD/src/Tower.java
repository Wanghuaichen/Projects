import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
public class Tower extends Rectangle{
	public Rectangle towerSquare;
	public int towerSquareSize= 130;
	public int airID;
	private int loseTime=100, loseFrame=0;
	
	private int shotEnemy=-1;
	public boolean shoting = false;
	
	public Tower (int x, int y, int width, int height,int groundID, int airID){
		setBounds(x, y, width, height);
		towerSquare = new Rectangle (x-(towerSquareSize/2), y-(towerSquareSize/2), width+towerSquareSize, height+towerSquareSize);
		this.airID=airID;
	}
	
	public void physic(){
		if(shotEnemy != -1 && towerSquare.contains(Map.enemies.get(shotEnemy))){
			shoting= true;
		} else {
			shoting = false;
		}
		
		if (!shoting){
			if(airID==Map.airTowerLaser){
				for(int i=0;i<Map.enemies.size();i++){
					if(Map.enemies.get(i).getInGame()){
						if(towerSquare.intersects(Map.enemies.get(i))){
							shoting=true;
							shotEnemy=i;
						}
					}
				}
			}
		}
		if (shoting){
			if(loseFrame >= loseTime){
				Map.enemies.get(shotEnemy).loseHealth(1);
				loseFrame=0;
			} else {
				loseFrame+=1;
			}
		   
			if (Map.enemies.get(shotEnemy).isDead()){
				
				
				shoting = false;
				shotEnemy= -1;
				
				Map.killed +=1;
				
				Map.hasWon();
			}
		}
	}
	
	public void getMoney(int enemyID){
		Map.coinage+= Map.deathReward[enemyID];
	}
	
	public void fight(Graphics g){
		if(Map.getIsDebug()){
			if(airID == Map.airTowerLaser){
				g.drawRect(towerSquare.x, towerSquare.y, towerSquare.width, towerSquare.height);
			}
		}
			
		if(shoting){
			g.setColor((new Color(255,0,0)));
			g.drawLine(x+(width/2), y+(height/2), Map.enemies.get(shotEnemy).x+(Map.enemies.get(shotEnemy).width/2), Map.enemies.get(shotEnemy).y+(Map.enemies.get(shotEnemy).height/2));		
		}
	}
}
