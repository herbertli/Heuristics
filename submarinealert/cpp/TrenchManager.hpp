#pragma once
#include <vector>

class TrenchManager {
  public: 
    virtual std::vector<int> getProbes() = 0;
    virtual void receiveProbeResults(std::vector<bool>) = 0;
    virtual bool shouldGoRed() = 0;
};
