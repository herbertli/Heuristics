#include "TernaryTrench.hpp"
#include <algorithm>
#include <set>
#include <vector>
#include <array>
#include <stdio.h>
#include <stdlib.h>

using namespace std;

TernaryTrench::TernaryTrench(int d, int y, int r, int m, int L, int p)
{
  this->time = 0;
  this->redZoneStart = d;
  this->yellowAlertCost = y;
  this->redAlertCost = r;
  this->gameTime = m;
  this->scanRange = L;
  this->probeCost = p;
  for (int i = d; i < d + 6; i++)
  {
    this->redZone.insert(i % 100);
  }
}

vector<int> TernaryTrench::sendBoundaryScan()
{
  this->scannedLocations = vector<int>();
  this->scannedLocations.push_back(this->leftProbe);
  this->scannedLocations.push_back(this->rightProbe);
  return this->scannedLocations;
}

// send enough probes to cover the red zone
// as well as probes on the extreme left and right
vector<int> TernaryTrench::sendInitialScan()
{
  vector<int> probeLocations;
  probeLocations.push_back((this->redZoneStart + 2) % 100);

  int left = (this->redZoneStart + 2 - this->scanRange + 100) % 100;
  while (left > this->redZoneStart)
  {
    probeLocations.push_back((left - this->scanRange - 1 + 100) % 100);
    left = left - 2 * this->scanRange - 1;
  }
  probeLocations.push_back((left - this->scanRange - 1 + 100) % 100);
  this->leftProbe = (left - this->scanRange - 1 + 100) % 100;

  int right = (this->redZoneStart + 2 + this->scanRange) % 100;
  while (this->redZone.count(right) != 0)
  {
    probeLocations.push_back((right + this->scanRange + 1) % 100);
    right = right + 2 * this->scanRange + 1;
  }
  this->rightProbe = (right + this->scanRange + 1) % 100;
  probeLocations.push_back((right + this->scanRange + 1) % 100);

  this->scannedLocations = vector<int>();
  for (int i = 0; i < probeLocations.size(); i++)
  {
    this->scannedLocations.push_back(probeLocations[i]);
  }
  return this->scannedLocations;
}

vector<int> TernaryTrench::sendScan()
{
  if (time % (2 * this->scanRange + 1) == 0)
  {
    this->scannedLocations = vector<int>();
    this->scannedLocations.push_back(this->leftProbe);
    this->scannedLocations.push_back(this->rightProbe);
    return this->scannedLocations;
  }
  else
  {
    vector<int> temp = vector<int>();
    return temp;
  }
}

void TernaryTrench::receiveProbeResults(vector<bool> results)
{
  if (results.size() == 0)
    return;

  int subLoc = -1;
  for (int i = 0; i < results.size(); i++)
  {
    if (time == 0)
    {
      if (results[i])
      {
        subLoc = this->scannedLocations[i];
        this->subFound = true;
        break;
      }
    }
    else
    {
      if (results[i])
      {
        if (i == 0)
          subLoc = this->leftProbe;
        else
          subLoc = this->rightProbe;
        this->subFound = true;
        break;
      }
    }
  }

  // now let's get the intervals for the next scan
  // assume you knew that the sub is in interval M at time t - probeRange
  // at time t, you deploy probes at L and R...
  // three cases:
  // |----- LL -----||----- L -----||----- M -----||----- R -----||----- RR -----|
  // 1. L returns true: deploy probes at LL and M at time t + probeRange
  // 2. R returns true: deploy probes at M and RR at time t + probeRange
  // 3. L and R return false: deploy probes at L and R at time t + probeRange
  // if L, M, R overlap with redzone, go to red alert over the next probeRange time
  if (verbose)
  {
    printf("============================\n");
    printf("Time: %d\n", this->time);
    printf("Sub loc: %d\n", subLoc);
  }
  // sub has moved to L or R, unscanned interval
  if (subLoc != -1)
  {
    if (subLoc == this->leftProbe)
    {
      this->rightProbe = (this->leftProbe + this->scanRange * 2 + 1) % 100;
      this->leftProbe = (this->leftProbe - this->scanRange * 2 - 1 + 100) % 100;
    }
    else if (subLoc == this->rightProbe)
    {
      this->leftProbe = (this->rightProbe - this->scanRange * 2 - 1 + 100) % 100;
      this->rightProbe = (this->rightProbe + this->scanRange * 2 + 1) % 100;
    }
  }

  set<int> scanZone;
  for (int i = this->leftProbe - this->scanRange; i != (this->rightProbe + this->scanRange + 1) % 100; i = (i + 1) % 100)
  {
    scanZone.insert((i + 100) % 100);
  }

  if (verbose)
  {
    printf("Scan Zone:");
    for (auto j : scanZone)
    {
      printf("%d ", j);
    }
    printf("\n");
  }

  // SPECIAL CASE: subLoc is too far from redZone to matter
  // should just check middle interval!
  bool tooFar = true;
  int d = this->redZoneStart;
  for (int i = (this->leftProbe - this->scanRange - 1 + 100) % 100; i != (this->rightProbe + this->scanRange + 1) % 100; i = (i + 1) % 100)
  {
    if (abs(i - d) <= this->gameTime - this->time)
    {
      tooFar = false;
      break;
    }
    if (abs(i - ((d + 5) % 100)) <= this->gameTime - this->time)
    {
      tooFar = false;
      break;
    }
  }

  if (tooFar)
  {
    if (verbose)
      printf("Special case!\n");
    this->redAlert = false;
    return;
  }

  // using scan zone, check for overlap with red zone
  // if so, go red for the next scanRange seconds, otherwise go yellow
  this->redAlert = false;
  for (auto j : this->redZone)
  {
    if (scanZone.count(j))
    {
      this->redAlert = true;
      return;
    }
  }
}

bool TernaryTrench::shouldGoRed()
{
  this->time = this->time + 1;
  return this->redAlert;
}

vector<int> TernaryTrench::getProbes()
{
  if (this->time == 0)
  {
    return sendInitialScan();
  }
  else if (!this->subFound && (this->time % (2 * this->scanRange + 1) == 0))
  {
    return sendBoundaryScan();
  }
  else if (this->subFound && (this->time % (2 * this->scanRange + 1) == 0))
  {
    return sendScan();
  }
  else
  {
    vector<int> temp = vector<int>();
    return temp;
  }
}