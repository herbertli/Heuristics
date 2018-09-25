#include "TrenchManager.hpp"
#include <set>
#include <vector>

using namespace std;

class TernaryTrench : public TrenchManager {
  public: 
    int redZoneStart;     // d
    int redAlertCost;     // r
    int yellowAlertCost;  // y
    int gameTime;         // m
    int scanRange;        // L
    int probeCost;        // p
    int time;
    set<int> redZone;
    bool redAlert = false;
    bool subFound = false;
    bool verbose = false;
    int leftProbe;
    int rightProbe;
    vector<int> scannedLocations;

    TernaryTrench(int, int, int, int, int, int);
    vector<int> sendBoundaryScan();
    vector<int> sendInitialScan();
    vector<int> sendScan();
    void receiveProbeResults(vector<bool>);
    vector<int> getProbes();
    bool shouldGoRed();

};