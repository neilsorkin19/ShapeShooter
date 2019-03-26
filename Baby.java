import java.awt.Point;

public class Baby {
	private Point loc;
	private int speed = 3;
	private double mass;
	private int xVelo;
	private int yVelo;
	private double gravity = 0.94;
	private double maxMass = 10;

	public Baby(int x, int y) {
		loc = new Point(x, y);
		mass = 10;
	}

	public Point getLocation() {
		return loc;
	}

	public void move(Point playerLoc, boolean mag) {
		if (mag) {
			double magnitude = Math
					.sqrt(Math.pow(playerLoc.getX() - loc.getX(), 2) + Math.pow(playerLoc.getY() - loc.getY(), 2));
			double moveX = speed * ((playerLoc.getX() - loc.getX()) / magnitude);
			double moveY = speed * ((playerLoc.getY() - loc.getY()) / magnitude);
			xVelo += moveX;
			yVelo += moveY;
			loc.translate(xVelo, yVelo);
		} else {
			loc.translate(xVelo, yVelo);
			xVelo *= gravity;
			yVelo *= gravity;
		}
	}

	public double getMass() {
		return mass;
	}

	public void addMass() {
		if (mass <= maxMass)
			mass += 0.1;
	}
}

/*
 * Baby/shield is for protecting you. Each layer shredded increases baby size by
 * a factor. Use space for a magnet. Baby grows and can tank shapes gaining you
 * points.
 */
