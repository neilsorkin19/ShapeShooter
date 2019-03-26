import java.awt.Point;

public class Player {
	private Point playerLoc;
	private Point lookingAt;
	private int height;
	private int width;// screen width
	private int size;// screen size
	private int speed = 12;

	private int verticalMovement;
	private int horizontalMovement;

	public Player(int h, int w, int s) {
		height = h;
		width = w;
		size = s;
		playerLoc = new Point(width / 2 - s / 2, height / 2 - s / 2);
		lookingAt = new Point(0, 0);
	}

	public void heldDown(int dir) {
		if (dir == 1) {// up
			verticalMovement = -1;
		} else if (dir == 2) {// down
			verticalMovement = 1;
		} else if (dir == 3) {// left
			horizontalMovement = -1;
		} else if (dir == 4) {// right
			horizontalMovement = 1;
		}
	}

	public int getHori() {
		return horizontalMovement;
	}

	public int getVert() {
		return verticalMovement;
	}

	public void keyUp(int dir) {
		if (dir == 1 || dir == 2) {// up and down
			verticalMovement = 0;
		} else if (dir == 3 || dir == 4) {// left and right
			horizontalMovement = 0;
		}
	}

	public void move() {
		Point possiblePos = new Point(0, 0);
		int radius = size / 2;
		possiblePos = addVal(playerLoc, (int) ((speed / Math.sqrt(2)) * horizontalMovement),
				(int) ((speed / Math.sqrt(2)) * verticalMovement));

		if ((possiblePos.getX() - radius <= 0 || possiblePos.getX() + radius >= width)
				&& (possiblePos.getY() - size <= 0 || possiblePos.getY() + radius >= height)) {
			// nothing needs to happen
		} else if (possiblePos.getX() - radius <= 0 || possiblePos.getX() + radius >= width) {
			playerLoc = new Point((int) playerLoc.getX(), (int) possiblePos.getY());
		} else if (possiblePos.getY() - radius <= 0 || possiblePos.getY() + radius >= height) {
			playerLoc = new Point((int) possiblePos.getX(), (int) playerLoc.getY());
		} else {
			playerLoc = possiblePos;
		}
	}

	public int getSpeed() {
		return speed;
	}

	public void lookAt(Point loc) {
		lookingAt = loc;
	}

	public Point getLook() {
		return lookingAt;
	}

	public Point getPosition() {
		return playerLoc;
	}

	public Point addVal(Point p, int x, int y) {
		return new Point((int) (p.getX() + x), (int) (p.getY() + y));
	}
}
