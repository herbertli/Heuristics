import java.util.ArrayDeque;
import java.util.Random;

public class SmartSub implements Submarine {

  Random rand;
  int position;
  int direction;
  ArrayDeque<Integer> probeTimes = new ArrayDeque<>();
  int time = 0;
  int gameTime;
  int timesProbed = 0;
  int tolerance;
  int scanRange;

  SmartSub(int position, int gameTime, int scanRange) {
    this.rand = new Random();
    this.gameTime = gameTime;
    this.scanRange = scanRange;
    this.tolerance = this.gameTime / 10;
    this.position = position;
    this.direction = this.rand.nextInt(2) == 0 ? -1 : 1;
  }

  // keep moving while it's still getting probed
  public int getMove() {
    Integer lastProbed = this.probeTimes.peekLast();
    if (lastProbed == null) {
      this.time++;
      return this.direction;
    } else if (this.time - lastProbed > this.tolerance) {
      this.time++;
      this.direction *= -1;
      return this.direction;
    } else {
      this.time++;
      return this.direction;
    }
  }

  public void hasBeenProbed(boolean probed) {
    this.probeTimes.addLast(this.time);
  }

}