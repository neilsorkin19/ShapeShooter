import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

public class Pellet {
	private Point2D pelletLoc;
	private double xVelo;
	private double yVelo;

	public Pellet(Point2D loc, double xv, double yv) {
		pelletLoc = loc;
		xVelo = xv;
		yVelo = yv;
	}

	public Point2D getLoc() {
		return pelletLoc;
	}

	public void movePellet(int width, int height, Rectangle r) throws NullPointerException {
		Point2D newPos = new Point2D.Double(pelletLoc.getX() + xVelo, pelletLoc.getY() + yVelo);
		Rectangle screenBorder = new Rectangle(0, 0, width, height);
		if (!screenBorder.contains(pelletLoc)) {// outside boundaries
			pelletLoc = new Point(-10, 0);
			xVelo = 0;
			yVelo = 0;
		} else if (r.contains(pelletLoc)) {// inside of spawn
			pelletLoc = new Point(-10, 0);
			xVelo = 0;
			yVelo = 0;
		} else
			pelletLoc = newPos;
	}
}
