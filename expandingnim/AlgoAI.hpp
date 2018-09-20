#include "AI.hpp"

class AlgoAI : public AI {
    private: 
        int state[1001][51][2][5][5];
        void fillBaseCases();
        void fill(int n);

    public: 
        AlgoAI();
        int getMove(int n, int cM, int cR, int oR, int eR);
        void printStartingMove();
};