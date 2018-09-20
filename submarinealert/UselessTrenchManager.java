public class UselessTrenchManager implements TrenchManager {
    public UselessTrenchManager(int d, int y, int r, int m, int L, int p){

    }
    public int[] getProbes(){
        return new int[0];
    }
    public void receiveProbeResults(boolean[] probeResults){
        return;
    }
    public boolean shouldGoRed(){
        return true;
    }
  
  }
  