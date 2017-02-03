import java.util.ArrayList;


	public class factory {

		public static ArrayList<Enemy> getEnemies(){
			Wave wave = Wave.getWaveInstance();// get instance of wave and then wave number
			ArrayList<Enemy> CrittersProduced = new ArrayList<Enemy>();//record the critters created by factory
			//produce the critters. every wave has 3 more footmans, 1.5 more wizards and 1 more knights
			for(int i = 1; i<=3*wave.getWaveNumber();i++){
				CrittersProduced.add(new Enemy1());
				//System.out.println("1 ready!");
				if(i%2==0){
					CrittersProduced.add(new Enemy2());
					//System.out.println("2 ready!");
				}
				if(i%3==0){
					CrittersProduced.add(new Enemy3());
					//System.out.println("3 ready!");
				}
			}
			return CrittersProduced;	// put all critters created in there
		}
	}

