#include "Submarine.hpp"

class GoesRightSub: public Submarine {
  public:
    int position;

    GoesRightSub(int);
    int getMove();
    void hasBeenProbed(bool);
};