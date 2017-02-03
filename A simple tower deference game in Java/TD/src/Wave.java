import java.util.ArrayList;


public class Wave {
// keep track of wave number
	private int waveNumber;
    private static Wave waveinstance = null;
    // record the critters produced by the factory
    private ArrayList<Enemy> CrittersInWave;
	//wave is a singleton class with private constructor
	private Wave(){
		waveNumber = 1;	
	}
	// way to get access to the singleton 
	public synchronized static Wave getWaveInstance(){
		if (waveinstance ==null)
		{waveinstance = new Wave();}
		return waveinstance;
	}

	public int getWaveNumber() {
		return waveNumber;
	}

	public void setWaveNumber(int waveNumber) {
		this.waveNumber = waveNumber;
	}
	public ArrayList<Enemy> getCrittersInWave() {
		return CrittersInWave;
	}
	public void setCrittersInWave(ArrayList<Enemy> crittersInWave) {
		CrittersInWave = crittersInWave;
	}


}
