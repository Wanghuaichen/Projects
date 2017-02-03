import javax.swing.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;


public class Map extends JPanel implements Runnable{
	public Thread thread = new Thread(this);
	
	public static int groundGrass=0;
	public static int groundRoad=1;
	public static int airAir = -1;
	public static int airCave = 0;
	public static int airTrashCan = 1;
	public static int airTowerLaser = 2;
	public static int airTowerSlow = 3;
	public static int airTowerFire = 4;
	public static int enemyAir = -1;
	public static int enemy = 0;
	
	public static int[] deathReward={3};
	/*public static int[] deathReward1={5};
	public static int[] deathReward2={7};
	public static int[] deathReward3={10};*/
	
	public static Image[] picture_ground=new Image[100];
	public static Image[] picture_air=new Image[100];
	public static Image[] picture_res=new Image[100];
	
	
 	private static boolean isFirst = true;
 	private static boolean isDebug = false;
 	private static boolean isWin = false;
 	
 	
 	public static Point mse = new Point (0, 0);
 	
	public static int myWidth,myHeight;
	public static int coinage=10, health=100;
	public static int killed=0, killsToWin=0, level=1, maxLevel=3;
	public static int winTime=4000, winFrame=0;
			
	public static Screen screen;
	public static Save save;
	public static Store store;
	public static ArrayList<Enemy> enemies= new ArrayList<Enemy>(100);
	//private static Enemy e= new Enemy();
	
	public Map(Frame frame){
		frame.addMouseListener(new KeyHandel());
		frame.addMouseMotionListener(new KeyHandel());
		
		thread.start();
	}	
	
	public static boolean getIsDebug(){
		return isDebug;
	}
	
	public static void hasWon(){
		if (killed==killsToWin){
			isWin=true;
			killed=0;
			coinage = 0;

		}
	}
	
	public void define(){
		screen = new Screen();
		save = new Save();
		store= new Store();
		Wave wave = Wave.getWaveInstance();
		
		coinage=20;
		health=100;
		
		
		for (int i=0;i<picture_ground.length;i++){
			picture_ground[i] = new ImageIcon("RS/picture_ground.png").getImage();
			picture_ground[i] = createImage(new FilteredImageSource(picture_ground[i].getSource(), new CropImageFilter(0,450*i,450,450)));
		}
		for (int i=0;i<picture_air.length;i++){
			picture_air[i] = new ImageIcon("RS/picture_air.png").getImage();
			picture_air[i] = createImage(new FilteredImageSource(picture_air[i].getSource(), new CropImageFilter(0,450*i,450,450)));
		}
		
		picture_res[0]= new ImageIcon("RS/picture_res.png").getImage();
		picture_res[1]= new ImageIcon("RS/Heart.png").getImage();
		picture_res[2]= new ImageIcon("RS/Money.png").getImage();
		
		/*picture_enemy1[0]= new ImageIcon("RS/Enemy1.png").getImage();
		picture_enemy2[0]= new ImageIcon("RS/Enemy2.png").getImage();
		picture_enemy3[0]= new ImageIcon("RS/Enemy3.png").getImage();*/
		
		save.loadSave(new File("save/mission"+ level +".ringgold"));
		
		
		
		/*for(int i=0;i<100;i++){
			enemies.add(new Enemy1());
			enemies.add(new Enemy2());
			enemies.add(new Enemy3());
		}*/
		int temp=wave.getWaveNumber()+100;
		while (true){
			if (wave.getWaveNumber()>temp){
				break;
			}
			ArrayList<Enemy> t2=factory.getEnemies();
			for(Enemy e: t2){
				enemies.add(e);
			}
			wave.setWaveNumber(wave.getWaveNumber()+1);
		}
		//wave.setCrittersInWave(factory.getEnemies());
			//wave.setWaveNumber(wave.getWaveNumber()+1);
		
	}
	public void paintComponent (Graphics g){
		if(isFirst){
			myWidth = getWidth();
			myHeight = getHeight();
			define();
			
			isFirst = false;
		}
		g.setColor(new Color(65,65,65));
		g.fillRect(0,0,getWidth(),getHeight());
		//g.setColor(new Color(0,0,0));
		//g.drawLine(screen.block[0][0].x-1,0,screen.block[0][0].x-1, screen.block[screen.worldHeight-1][0].y+screen.blockSize);//Drawing the left line.
		//g.drawLine(screen.block[0][screen.worldWidth-1].x+screen.blockSize,0,screen.block[0][screen.worldWidth-1].x+screen.blockSize, screen.block[screen.worldHeight-1][0].y+screen.blockSize);//Drawing the right line
		//g.drawLine(screen.block[0][0].x, screen.block[screen.worldHeight-1][0].y+screen.blockSize, screen.block[0][screen.worldWidth-1].x+screen.blockSize,screen.block[screen.worldHeight-1][0].y+screen.blockSize);
		screen.draw(g);//Drawing the screen.
		for(int i=0; i<enemies.size(); i++){
			if (enemies.get(i).getInGame()){
				enemies.get(i).draw(g,i);
			}
		}
		
		store.draw(g);//Draw the store there.
		
		if (health<1){
			g.setColor(new  Color (168, 75, 125));
			g.fillRect(0, 0, myWidth, myHeight);
			g.setColor(new Color(255,255,255));
			g.setFont(new Font("Arial", Font.BOLD, 53));
			g.drawString("Game Over", 200, 250);
			g.setFont(new Font("Courier New", Font.BOLD, 38));
			g.drawString("Thanks For Playing", 140, 350);
		}
		
		if(isWin){//We can set Winning Conditions here.
			g.setColor(new Color(255,100,100));
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(new Color(0,0,0));
			g.setFont(new Font("Arial", Font.BOLD, 30));
			g.drawString("You Won! Congratulations!", 120, 235);
			//if(level==maxLevel){
			g.setFont(new Font("Courier New", Font.BOLD, 25));
			g.drawString("Thank You For Playing", 120, 350);
			/*} else{
				g.setFont(new Font("Courier New", Font.BOLD, 25));
				g.drawString("Please wait for the next level.", 50, 350);
			}*/
		}
	}
	
	private int spawnTime=2400, spawnFrame=0; //Enemy Spawning Machine without wave and factory
	public void enemySpawner(){
		if (spawnFrame >= spawnTime){
			for(int i =0;i<enemies.size();i++){
				if (!enemies.get(i).getInGame()){
					enemies.get(i).spawnEnemy(Map.enemy);
					break;
					/*if (enemies.get(i).getNum()==1){
						enemies.get(i).spawnEnemy(Map.enemy1);
						break;
					} else if (enemies.get(i).getNum()==2){
						enemies.get(i).spawnEnemy(Map.enemy2);
						break;
					} else if (enemies.get(i).getNum()==3){
						enemies.get(i).spawnEnemy(Map.enemy3);
						break;
					}*/
					
					
				}
			}
			spawnFrame=0;
		} else{
			spawnFrame+=1;
		}
	}
	
	
	
	public void run(){
		while(true){
			if(!isFirst && health>0 && !isWin){
				screen.physic();
				enemySpawner();
				for (int i =0;i<enemies.size();i++){
					if (enemies.get(i).getInGame()){
						enemies.get(i).physic();//invoke move
						/*if (enemies.get(i).counter==5000){
							int x=enemies.get(i).getSpeedDecrease();
							x=0;
							enemies.get(i).counter=0;
						}
						if (enemies.get(i).getSpeedDecrease()!=0){
							enemies.get(i).counter++;
						}*/
					}
				}
			} else {
				if(isWin){
					if(winFrame>=winTime){
						//if(level == maxLevel){
							System.exit(0);
						/*} else {
							level+=1;
							define();
							
							isWin=false;
							
						}*/
						
						winFrame=0;
					} else {
						winFrame +=1;
					}
				}
			}
			
			repaint();
			try{
				Thread.sleep(1);
			}
			catch(Exception e){}
		}
		
	}
}
