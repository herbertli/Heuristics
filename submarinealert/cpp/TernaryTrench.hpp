#include "TrenchManager.hpp"
#include <set>

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
    int* scannedLocations;

    TernaryTrench(int, int, int, int, int, int);
    int* sendBoundaryScan();
    int* sendInitialScan();
    int* sendScan();
    void receiveProbeResults(bool[]);
    int* getProbes();
    bool shouldGoRed();

};