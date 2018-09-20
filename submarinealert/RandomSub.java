import java.util.Random;

public class RandomSub implements Submarine {

  int position;
  Random rand = new Random(123);

  RandomSub(int position) {
    this.position = position;
  }

  public int getMove() {
    return rand.nextInt(3) - 1;
  }

  public void hasBeenProbed(boolean probed) {

  }

}