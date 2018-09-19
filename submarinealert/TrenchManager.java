interface TrenchManager {

  int[] getProbes();
  void receiveProbeResults(boolean[] probeResults);
  boolean shouldGoRed();

}
