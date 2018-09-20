import java.util.ArrayDeque;
import java.util.Random;

public class RandomSub implements Submarine {

  int position;
  Random rand = new Random(123);

  ArrayDeque<Integer> commandList = new ArrayDeque<>();

  RandomSub(int position) {
    this.position = position;
  }

  void generateNewCommand() {
    int magnitude = rand.nextInt(10) + 1;
    int dir = rand.nextInt(2) == 0 ? -1 : 1;
    for (int i = 0; i < magnitude; i++) {
      commandList.add(1 * dir);
    }
  }

  public int getMove() {
    if (commandList.size() == 0) {
      generateNewCommand();
    }
    return commandList.poll();
  }

  public void hasBeenProbed(boolean probed) {

  }

}