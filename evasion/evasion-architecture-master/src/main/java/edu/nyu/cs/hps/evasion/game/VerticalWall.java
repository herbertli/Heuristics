package edu.nyu.cs.hps.evasion.game;

import java.awt.*;

public class VerticalWall implements Wall {

  public int x;
  public int y1;
  public int y2;

  public VerticalWall(int x, int y1, int y2){
    this.x = x;
    this.y1 = y1;
    this.y2 = y2;
  }

  public boolean occupies(Point point){
    return point.x == this.x && point.y >= this.y1 && point.y <= this.y2;
  }

  public String toString(){
    StringBuilder stringBuilder = new StringBuilder()
      .append("1").append(" ")
      .append(x).append(" ")
      .append(y1).append(" ")
      .append(y2);
    return stringBuilder.toString();
  }
}
