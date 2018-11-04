package nyu.hps.botty.dancing;

public class Point implements Comparable<Point> {
    int x, y;
    int time;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x && y == point.y;
    }

    @Override
    public int compareTo(Point o) {
        if (this.x == o.x && this.y == o.y) return 0;
        if (this.x == o.x) return Integer.compare(this.x, o.x);
        return Integer.compare(this.y, o.y);
    }
}
