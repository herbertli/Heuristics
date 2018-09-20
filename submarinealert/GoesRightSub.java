public class GoesRightSub implements Submarine {

  int position;

  GoesRightSub(int position) {
    this.position = position;
  }

  public int getMove() {
      return 1;
  }

  public void hasBeenProbed(boolean probed) {

  }

}