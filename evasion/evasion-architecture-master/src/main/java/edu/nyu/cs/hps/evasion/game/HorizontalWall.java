package edu.nyu.cs.hps.evasion.game;

import java.awt.*;

public class HorizontalWall implements Wall {

  public int y;
  public int x1;
  public int x2;

  public HorizontalWall(int y, int x1, int x2){
    this.y = y;
    this.x1 = x1;
    this.x2 = x2;
  }

  public boolean occupies(Point point){
    return point.y == this.y && point.x >= this.x1 && point.x <= this.x2;
  }

  public String toString(){
    StringBuilder stringBuilder = new StringBuilder()
      .append("0").append(" ")
      .append(y).append(" ")
      .append(x1).append(" ")
      .append(x2);
    return stringBuilder.toString();
  }
}
