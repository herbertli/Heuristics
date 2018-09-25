class TrenchManager {
  public: 
    virtual int* getProbes() = 0;
    virtual void receiveProbeResults(bool[]) = 0;
    virtual bool shouldGoRed() = 0;
};
