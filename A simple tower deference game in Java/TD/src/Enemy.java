import java.awt.*;

public abstract class Enemy extends Rectangle{
	private int xC, yC;
	public int health=100;
	public int healthSpace=0, healthHeight=6;
	public int enemySize=52;
	public int enemyWalk=0;
	private int upward=0, downward=1, right=2,left=3;
	private int direction=right;
	private int enemyID=Map.enemyAir;
	private boolean inGame=false;
	private boolean hasUpward=false;
	private boolean hasDownward=false;
	private boolean hasLeft=false;
	private boolean hasRight=false;
	public int counter=0;
	private int speedDecrease=0;
	
	private int num=0;
	public Image enemyImage;
	public int getNum(){
		return num;
	}
	
	public void setNum(int num){
		this.num=num;
	}
	
	public int getSpeedDecrease(){
		return speedDecrease;
	}
	
	public boolean getInGame(){
		return inGame;
	}
	
	public Enemy(int health, int speed){
		this.health=health;
		this.walkSpeed=15000/speed;
	}
	
	public void spawnEnemy(int enemyID){
		for (int y=0;y<Map.screen.block.length;y++){
			if (Map.screen.block[y][0].getGroundID() == Map.groundRoad){
				setBounds(Map.screen.block[y][0].x,Map.screen.block[y][0].y,enemySize,enemySize);
				xC=0;
				yC=y;
			}
		}
		this.enemyID=enemyID;
		//this.health=enemySize;
		inGame=true;
	}
	
	public void deleteEnemy(){
		//Map.enemies.remove(enemyID);
		inGame=false;
		direction = right;
		enemyWalk=0;
		
		Map.screen.block[0][0].getMoney(enemyID);
	}
	
	public void delete2Enemy(){//delete without money
		//Map.enemies.remove(enemyID);
		inGame=false;
		direction = right;
		enemyWalk=0;
		
		//Map.screen.block[0][0].getMoney(enemyID);
	}
	 
	public void looseHealth(){
		Map.health -=1;
	}
	
	
	private int walkFrame = 0, walkSpeed=1000;// adjust speed of enemys
	public void physic(){//move
		if (walkFrame>= (walkSpeed+speedDecrease)){
			if (direction == right){
				x+=1;
			} else if (direction == upward){
				y -=1;
				
			} else if (direction == downward){
				y+=1;
				
			} else if (direction == left){
				x-=1;
			}
			
			enemyWalk+=1;
			
			if(enemyWalk== Map.screen.blockSize){
				if (direction == right){
					xC += 1;
					hasRight=true;
				} else if (direction == upward){
					yC -= 1;
					hasUpward=true;
				} else if (direction == downward){
					yC += 1;
					hasDownward=true;
				} else if (direction == left){
					xC -= 1;
					hasLeft=true;
				}
				
				if (!hasUpward){
				try {
					if(Map.screen.block[yC+1][xC].getGroundID() == Map.groundRoad){
						direction = downward;
					}
				} catch(Exception e) {}
				}
				
				if (!hasDownward){
					try {
						if(Map.screen.block[yC-1][xC].getGroundID() == Map.groundRoad){
							direction = upward;
						}
					} catch(Exception e) {}
				}
				
				if (!hasLeft){
					try {
						if(Map.screen.block[yC][xC+1].getGroundID() == Map.groundRoad){
							direction = right;
						}
					} catch(Exception e) {}
				}
				
				if (!hasRight){
					try {
						if(Map.screen.block[yC][xC-1].getGroundID() == Map.groundRoad){
							direction = left;
						}
					} catch(Exception e) {}
				}
				
				if(Map.screen.block[yC][xC].getAirID() == Map.airCave){
					delete2Enemy();
					looseHealth();
					Map.enemies.remove(this);
				}
					
				hasUpward=false;
				hasDownward=false;
				hasLeft=false;
				hasRight=false;
				enemyWalk=0;
			}
			
			walkFrame=0;
	
		} else {
			walkFrame +=1;
		}
		
	}
	
	public void loseHealth(int amo){
		health -= amo;
		
		//checkDeath();
	}
	
	public void checkDeath(){
		if(health==0){
			deleteEnemy();
			//Map.enemies.remove(enemyID);
			//Map.screen.block[0][0].getMoney(enemyID);
		}
	}
	
	public boolean isDead(){
		if(inGame){
			return false;
		} else {
			return true;
		}
	}

	public void draw(Graphics g, int i){
		g.drawImage(Map.enemies.get(i).enemyImage, x, y, width, height,null);
		
		//Health Bar
		g.setColor(new Color(180,50,50));
		g.fillRect(x, y-(healthSpace + healthHeight), width, healthHeight);
		
		g.setColor(new Color(50,180,50));
		g.fillRect(x, y-(healthSpace + healthHeight), health, healthHeight);

		g.setColor(new Color(0,0,255));
		g.drawRect(x, y-(healthSpace + healthHeight), health-1, healthHeight-1);
		
	}
}
