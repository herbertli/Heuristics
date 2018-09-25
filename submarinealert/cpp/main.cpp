#include "TrenchManager.hpp"
#include "TernaryTrench.cpp"
#include "Submarine.hpp"
#include "GoesRightSub.cpp"
#include <algorithm>
#include <set>
#include <vector>
#include <array>
#include <iterator>
#include <utility>
#include <random>

using namespace std;

bool randomize = true;
bool verbose = false;
int fails = 0;

int test(int run, TrenchManager *tm, int d, int y, int r, int m, int L, int p, int subPosition)
{
  set<int> redZone;
  for (int i = d; i < d + 6; i++)
  {
    redZone.insert(i % 100);
  }
  if (verbose)
  {
    printf("d: %d, y: %d, r: %d, m: %d, L: %d, p: %d, subPosition: %d\n", d, y, r, m, L, p, subPosition);
  }
  GoesRightSub gr(m);
  Submarine *sub = &gr;

  int cost = 0;

  bool failed = false;
  int probesUsed = 0;

  for (int i = 0; i < m; i++)
  {
    subPosition = (subPosition + sub->getMove() + 100) % 100;

    if (verbose)
    {
      printf("Time: %d\n", i);
      printf("Submarine Position: %d\n", subPosition);
    }

    vector<int> probes = tm->getProbes();
    int numProbes = probes.size();
    cost += numProbes * p;
    probesUsed += numProbes;
    if (verbose)
    {
      printf("TM probes: ");
      for (int i = 0; i < numProbes; i++)
        printf("%d ", probes[i]);
      printf("\n");
    }
    // calculate which probes are "yes"
    vector<bool> yes;
    bool probed = false;
    for (int j = 0; j < numProbes; j++)
    {
      int probe = probes[j];
      int lb = (probe + 100 - L) % 100;
      int ub = (probe + 100 + L) % 100;
      if (ub < lb)
        ub += 100;
      int tempSubPosition = subPosition;
      while (tempSubPosition < lb)
        tempSubPosition += 100;
      yes.push_back(tempSubPosition <= ub);
      probed |= yes[j];
    }
    if (verbose)
    {
      printf("Probe result: ");
      for (auto y : yes)
        printf(y ? "true " : "false ");
      printf("\n");
    }
    tm->receiveProbeResults(yes);

    bool redAlert = tm->shouldGoRed();
    if (redAlert)
    {
      cost += r;
      if (verbose)
        printf("TM goes on red alert\n");
    }
    else
    {
      cost += y;
      if (verbose)
        printf("TM goes on yellow alert\n");
      if (redZone.count(subPosition))
      {
        // if (verbose) {
        printf("Run %d:\n", run);
        printf("Time: %d\n", i);
        printf("d: %d, y: %d, r: %d, m: %d, L: %d, p: %d, subPosition: %d\n", d, y, r, m, L, p, subPosition);
        printf("Uh oh! Game over!\n");
        exit(1);
        // }
        failed = true;
        fails++;
        break;
      }
    }

    // send to sub if it has been probed
    sub->hasBeenProbed(probed);
  }

  if (!failed)
  {
    return cost;
  }
  else
  {
    return 5 * m * p + r * m;
  }
}

int main(int argc, char *argv[])
{
  int d = 13;
  int y = 2;
  int r = 20;
  int m = 107;
  int L = 9;
  int p = 15;
  int subPosition = 13;

  vector<TrenchManager *> list;
  vector<int> wins;
  for (int i = 0; i < 100000; i++)
  {
    // if (randomize) {
    //   default_random_engine generator;
    //   d = rand.nextInt(100);
    //   y = rand.nextInt(10) + 1;
    //   r = y * 10;
    //   m = rand.nextInt(100) + 100;
    //   L = rand.nextInt(11) + 1;
    //   p = rand.nextInt(50) + 1;
    //   subPosition = rand.nextInt(100);
    // }

    list.clear();
    // list.add(new AldoTM(d, y, r, m, L, p));
    // list.add(new UselessTrenchManager(d, y, r, m, L, p));
    TernaryTrench tt = TernaryTrench(d, y, r, m, L, p);
    TrenchManager *tm = &tt;
    list.push_back(tm);

    if (i == 0)
    {
      for (int i = 0; i < list.size(); i++)
      {
        wins.push_back(0);
      }
    }

    if (i % 10000 == 0)
      printf("Run: %d\n", i);

    vector<int> costs;
    for (auto tm : list)
    {
      int cost = test(i, tm, d, y, r, m, L, p, subPosition);
      costs.push_back(cost);
      if (verbose)
      {
        printf("Cost: %d\n", cost);
      }
    }
    vector<int>::iterator result = min_element(begin(costs), end(costs));
    int minCost = costs[distance(begin(costs), result)];
    for (int j = 0; j < costs.size(); j++)
    {
      if (costs[j] == minCost)
        wins[j]++;
    }
  }
  printf("Failures: %d\nWins: ", fails);
  for (auto i : wins)
    printf("%d ", i);
  printf("\n");
}