import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

public class Obstacle {
	private Polygon shape;
	private int layers;
	private int xVelo;
	private int yVelo;
	private int sizeMuliplier = 30;
	private double offset;
	private Rectangle r = PelletDodger.spawnArea;

	public Obstacle(int sides, int xStart, int yStart, int xV, int yV) {
		int[] xVals = new int[sides];
		int[] yVals = new int[sides];
		offset = Math.random() * Math.PI * 2;
		double theta = 2 * Math.PI / sides;
		for (int i = 0; i < sides; i++) {
			double x = sizeMuliplier * Math.cos(theta * i + offset) + xStart;
			double y = sizeMuliplier * Math.sin(theta * i + offset) + yStart;
			xVals[i] = (int) x;
			yVals[i] = (int) y;
		}
		shape = new Polygon(xVals, yVals, sides);
		layers = sides;
		xVelo = xV;
		yVelo = yV;
	}

	public Obstacle stripLayer() {
		if (layers - 1 == 2) {
			return null;
		}
		int centerOfShapeX = 0;
		int centerOfShapeY = 0;
		for (int k = 0; k < layers; k++) {
			centerOfShapeX += shape.xpoints[k];
			centerOfShapeY += shape.ypoints[k];
		}
		centerOfShapeX /= layers;
		centerOfShapeY /= layers;
		return new Obstacle(layers - 1, centerOfShapeX, centerOfShapeY, xVelo, yVelo);
	}

	public boolean containsPellet(Pellet p) {
		return shape.contains(p.getLoc()) && !r.contains(p.getLoc());
	}

	public boolean containsBaby(Baby b) {
		if (!r.contains(b.getLocation())) {
			double size = b.getMass() / 4;
			int approximateSides = 100;
			int[] xVals = new int[approximateSides];
			int[] yVals = new int[approximateSides];
			// creates parallel arrays of ints to represent x,y pairs to check for
			// intersection
			offset = Math.random() * Math.PI * 2;
			double theta = 2 * Math.PI / 100;
			for (int i = 0; i < 100; i++) {
				double x = size * Math.cos(theta * i + offset) + b.getLocation().getX();
				double y = size * Math.sin(theta * i + offset) + b.getLocation().getY();
				xVals[i] = (int) x;
				yVals[i] = (int) y;
			}
			for (int k = 0; k < approximateSides; k++) {
				if (shape.contains(new Point(xVals[k], yVals[k]))) {
					PelletDodger.score += layers - 2;
					return true;
				}
			}
		}
		return false;
	}

	public boolean containsPlayer(Player p) {// check if the player went into the obstacle
		int size = PelletDodger.playerSize / 4;
		int approximateSides = 100;
		int[] xVals = new int[approximateSides];
		int[] yVals = new int[approximateSides];
		// creates parallel arrays of ints to represent x,y pairs to check for
		// intersection
		offset = Math.random() * Math.PI * 2;
		double theta = 2 * Math.PI / 100;
		for (int i = 0; i < 100; i++) {
			double x = size * Math.cos(theta * i + offset) + p.getPosition().getX();
			double y = size * Math.sin(theta * i + offset) + p.getPosition().getY();
			xVals[i] = (int) x;
			yVals[i] = (int) y;
		}
		for (int k = 0; k < approximateSides; k++) {
			if (shape.contains(xVals[k], yVals[k])) {
				return true;
			}
		}
		return false;
	}

	public int getLayerCount() {
		return layers;
	}

	public void move(double x, double y) {
		for (int t = 0; t < x; t++) {
			if (shape.contains(t, y)) {
				yVelo *= -1;
			} else if (shape.contains(t, 0)) {
				yVelo *= -1;
			}
		}
		for (int k = 0; k < y; k++) {
			if (shape.contains(0, k)) {
				xVelo *= -1;
			} else if (shape.contains(x, k)) {
				xVelo *= -1;
			}
		}
		Polygon tempShape = new Polygon(shape.xpoints, shape.ypoints, layers);
		for (int k = 0; k < layers; k++) {
			tempShape.xpoints[k] += xVelo;
			tempShape.ypoints[k] += yVelo;
		}
		shape = tempShape;
	}

	public Polygon getPoly() {
		return shape;
	}

	public Polygon loseLayer(Polygon p) {
		return new Polygon();
	}
	/*
	 * Shapes will start as triangle, square, penta, or hex and you have to shoot
	 * them before they hit you.
	 */
}
