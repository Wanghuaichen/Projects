import java.awt.*;
public class Block extends Rectangle{
	private Rectangle towerSquare;
	private int towerSquareSize= 130;
	private int groundID;
	private int airID;
	private int loseTime=80, loseFrame=0;
	
	private int shotEnemy=-1;
	private boolean shoting = false;
	
	public Block (int x, int y, int width, int height,int groundID, int airID){
		setBounds(x, y, width, height);
		towerSquare = new Rectangle (x-(towerSquareSize/2), y-(towerSquareSize/2), width+towerSquareSize, height+towerSquareSize);
		this.groundID=groundID;
		this.airID=airID;
	}
	
	public int getShotEnemy(){
		return shotEnemy;
	}
	
	public boolean getshoting(){
		return shoting;
	}
	
	/*public void setShotEnemy(int shotEnemy){
		this.shotEnemy=shotEnemy;
	}
	
	public void setshoting(boolean shoting){
		this.shoting=shoting;
	}*/
	
	public int getGroundID(){
		return groundID;
	}
	
	
	public int getAirID(){
		return airID;
	}
	
	public void setAirID(int x){
		airID=x;
	}
	
	public void setGroundID(int x){
		groundID=x;
	}
	
	public void draw(Graphics g){
		g.drawImage(Map.picture_ground[groundID],x,y,width,height,null);
		
		if (airID!=Map.airAir){
			g.drawImage(Map.picture_air[airID],x,y,width,height,null);
			
		}
	}
	
	public void attack(){
		if(shotEnemy != -1 && towerSquare.contains(Map.enemies.get(shotEnemy))){
			shoting= true;
		} else {
			shoting = false;
		}
		
		if (!shoting){
			if(airID==Map.airTowerLaser || airID==Map.airTowerSlow || airID==Map.airTowerFire){
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
				if (Map.enemies.get(shotEnemy).health==0){
					Map.killed +=1;
					Map.screen.block[0][0].getMoney(Map.enemy);
					Map.enemies.remove(shotEnemy);
					Map.hasWon();
				}
				loseFrame=0;
			} else {
				loseFrame+=1;
			}
		   
			/*if (Map.enemies.get(shotEnemy).isDead()){
				
				
				shoting = false;
				shotEnemy= -1;
				
				Map.killed +=1;
				
				Map.hasWon();
			}*/
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
