import java.util.Random;

public class RandomSub {

  int position;
  Random rand = new Random(123);

  RandomSub(int position) {
    this.position = position;
  }

  int getMove() {
    return rand.nextInt(3) - 1;
  }

}