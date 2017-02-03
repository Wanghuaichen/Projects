import java.awt.*;
public class Screen {// loading things like levels 
	public int worldWidth = 14;
	public int worldHeight = 8;
	public int blockSize = 50;
	
	public Block[][] block;
	public Screen(){
		define();
 	}
	
	public void define(){
		block = new Block[worldHeight][worldWidth];
		
		for (int y=0;y<block.length;y++){
			for(int x=0;x<block[0].length;x++){
				block[y][x]=new Block((Map.myWidth/2)-((worldWidth*blockSize)/2)+(x*blockSize), y*blockSize, blockSize, blockSize, Map.groundGrass, Map.airAir);
			}
		}
	}
	public void physic(){ 
		
		for(int y=0;y<block.length;y++){
			for(int x=0; x<block[0].length;x++){
				block[y][x].attack();
			}
		}
	}
	
	public void draw(Graphics g){
		for (int y=0;y<block.length;y++){
			for (int x=0;x<block[0].length;x++){
				block[y][x].draw(g);
			}
		}
		
		for (int y=0;y<block.length;y++){
			for (int x=0;x<block[0].length;x++){
				block[y][x].fight(g);
			}
		}
	}
}
