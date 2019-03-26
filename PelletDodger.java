import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

@SuppressWarnings("serial")
public class PelletDodger extends JPanel implements KeyListener, MouseInputListener {
	static final int FPS = 60;
	public final static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	public final static int WIDTH = (int) screenSize.getWidth();
	public final static int HEIGHT = (int) screenSize.getHeight();

	int pelletSize = (int) (HEIGHT / 128);
	static int playerSize = (int) (HEIGHT / 32);

	public static KeyEvent key;

	public static boolean clickRel;
	public static boolean magnetActive = false;
	public static boolean running = true;
	public static boolean showMenu = true;
	public static boolean autoFire = false;
	public static boolean escPressed = false;
	public static boolean spawning = false;
	public static boolean checkedForHighScore = false;
	public static boolean scoreIsHigh = false;// still needs work

	static JFrame frame = new JFrame("Pellet Dodger");

	public static int framesShown = 0;

	static Player p = new Player((int) HEIGHT, (int) WIDTH, playerSize);

	public static ArrayList<Pellet> pellets = new ArrayList<>();
	public static ArrayList<Obstacle> obstacles = new ArrayList<>();

	static Rectangle spawnArea = new Rectangle(WIDTH / 2 - WIDTH / 32, HEIGHT / 2 - HEIGHT / 32, WIDTH / 16,
			HEIGHT / 16);
	static Rectangle Q2 = new Rectangle(WIDTH / 2, 0, WIDTH, HEIGHT / 2);
	static Rectangle Q1 = new Rectangle(0, 0, WIDTH / 2, HEIGHT / 2);
	static Rectangle Q3 = new Rectangle(0, HEIGHT / 2, WIDTH / 2, HEIGHT);
	static Rectangle Q4 = new Rectangle(WIDTH / 2, HEIGHT / 2, WIDTH, HEIGHT);

	public static Baby b;

	public static int score = 0;
	public static int level = 1;
	public static int lives = 3;
	public static int frameCooldown = 0;
	public static long milliStart = -1;
	public static int quadrant1Cooldown = FPS * 15;
	public static int quadrant2Cooldown = FPS * 15;
	public static int quadrant3Cooldown = FPS * 15;
	public static int quadrant4Cooldown = FPS * 15;

	public static Font font = new Font("Calibri", Font.BOLD, (int) (WIDTH / 40));
	public static Font ruleFont = new Font("Calibri", Font.PLAIN, (int) (WIDTH / 60));

	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paint(g);
		g2d.setFont(font);
		if (lives <= 0) {
			drawCenterText(g2d, 0, 0, "Press 'R' to respawn or 'ESC' to quit.", font);
			drawCenterText(g2d, 0, HEIGHT / 5, "Your final score was " + score, font);
			if (scoreIsHigh) {
				drawCenterText(g2d, -50, -50, "You got the high score!!!", font);
			}
		} else if (showMenu) {
			drawMenu(g2d);
		} else {
			drawObjects(g2d);
		}
	}

	private void drawMenu(Graphics2D g2d) {
		setForeground(Color.CYAN);
		drawCenterText(g2d, 0, -(HEIGHT / 5), "Shape Dodger", font);
		g2d.setFont(ruleFont);
		drawCenterText(g2d, -WIDTH / 6, 0, "Eliminate the shapes before:", ruleFont);
		g2d.drawString("-the shapes take all three of your lives by colliding into you", WIDTH / 6, 6 * HEIGHT / 10);
		g2d.drawString("You must enter each quadrant once every 15 seconds.", WIDTH / 7, (int) (8.5 * HEIGHT / 10));
		g2d.drawString("Otherwise you will lose a life.", WIDTH / 7, 9 * HEIGHT / 10);
		g2d.drawString("This is done to prevent corner camping.", WIDTH / 7, (int) (9.5 * HEIGHT / 10));
		drawCenterText(g2d, 0, -(HEIGHT / 9), "Press 'G' to begin...", ruleFont);
		g2d.drawString("Current High Score: " + checkIfScoreIsGlobalRecord(), 50, 50);
		String keyB = "";
		int mulitplier = HEIGHT / 32;
		for (int lineGap = 0; lineGap <= 6; lineGap++) {

			switch (lineGap) {
			case 0:
				keyB = "Keybinds:";
				break;
			case 1:
				keyB = "W = Up";
				break;
			case 2:
				keyB = "A = Left";
				break;
			case 3:
				keyB = "S = Down";
				break;
			case 4:
				keyB = "D = Right";
				break;
			case 5:
				keyB = "E = Auto-Fire";
				break;
			case 6:
				keyB = "Space Bar = Activate Shield Magnet";
				break;

			default:
				break;
			}
			drawCenterText(g2d, (WIDTH / 8), (lineGap * mulitplier), keyB, ruleFont);
			keyB = "";
		}

		setBackground(Color.black);
		g2d.setFont(font);
	}

	private static void drawCenterText(Graphics2D g2d, int x, int y, String phrase, Font f) {
		Font prevFont = g2d.getFont();
		g2d.setFont(f);
		FontMetrics metrics = g2d.getFontMetrics(f);
		int xTranslate = x + ((WIDTH - metrics.stringWidth(phrase)) / 2);
		int yTranslate = y + ((HEIGHT - metrics.getHeight()) / 2) + metrics.getAscent();
		g2d.drawString(phrase, x + xTranslate, y + yTranslate);
		g2d.setFont(prevFont);
	}

	private void drawObjects(Graphics2D g2d) {
		for (Obstacle o : obstacles) {// obstacles
			if (o.getLayerCount() == 3) {
				g2d.setColor(Color.red);
			} else if (o.getLayerCount() == 4) {
				g2d.setColor(new Color(204, 153, 0));
			} else if (o.getLayerCount() == 5) {
				g2d.setColor(Color.blue);
			} else if (o.getLayerCount() == 6) {
				g2d.setColor(Color.GREEN);
			}
			g2d.fillPolygon(o.getPoly());
		}
		g2d.setColor(Color.RED);
		for (int pellet = 0; pellet < pellets.size(); pellet++) {// pellets
			if (pellets.get(pellet).getLoc() != null)
				g2d.fillOval((int) pellets.get(pellet).getLoc().getX(), (int) pellets.get(pellet).getLoc().getY(),
						pelletSize, pelletSize);
		}
		g2d.setColor(new Color(100, 30, 200));
		// player
		g2d.fillOval((int) p.getPosition().x - playerSize / 2, (int) p.getPosition().getY() - playerSize / 2,
				playerSize, playerSize);
		g2d.draw(spawnArea);
		// spawn area
		g2d.drawRoundRect(spawnArea.x, spawnArea.y, spawnArea.width, spawnArea.height, 100, 100);
		g2d.drawLine(WIDTH / 2, 0, WIDTH / 2, HEIGHT);// vertical line
		g2d.drawLine(0, HEIGHT / 2, WIDTH, HEIGHT / 2);// horizontal line
		if (quadrant1Cooldown / FPS <= 5) {
			g2d.setColor(Color.red);
		} else
			g2d.setColor(new Color(100, 30, 200));
		g2d.drawString(quadrant1Cooldown / FPS + "", WIDTH / 2 - WIDTH / 32, HEIGHT / 32);
		if (quadrant2Cooldown / FPS <= 5) {
			g2d.setColor(Color.red);
		} else
			g2d.setColor(new Color(100, 30, 200));
		g2d.drawString(quadrant2Cooldown / FPS + "", WIDTH / 2 + WIDTH / 32, HEIGHT / 32);
		if (quadrant3Cooldown / FPS <= 5) {
			g2d.setColor(Color.red);
		} else
			g2d.setColor(new Color(100, 30, 200));
		g2d.drawString(quadrant3Cooldown / FPS + "", WIDTH / 2 - WIDTH / 32, HEIGHT - HEIGHT / 32);
		if (quadrant4Cooldown / FPS <= 5) {
			g2d.setColor(Color.red);
		} else
			g2d.setColor(new Color(100, 30, 200));
		g2d.drawString(quadrant4Cooldown / FPS + "", WIDTH / 2 + WIDTH / 32, HEIGHT - HEIGHT / 32);
		g2d.setColor(new Color(100, 30, 200));
		g2d.setStroke(new BasicStroke((float) (HEIGHT / 128)));
		// gun on player
		double magnitude = Math.sqrt(Math.pow(p.getLook().getX() - p.getPosition().getX(), 2)
				+ Math.pow(p.getLook().getY() - p.getPosition().getY(), 2));
		int size = (HEIGHT / 32);
		double pointAtX = size * ((p.getLook().getX() - p.getPosition().getX()) / magnitude);
		double pointAtY = size * ((p.getLook().getY() - p.getPosition().getY()) / magnitude);
		g2d.drawLine(p.getPosition().x, p.getPosition().y, (int) ((int) p.getPosition().getX() + pointAtX),
				(int) ((int) p.getPosition().getY() + pointAtY));
		// frame printer
		g2d.setFont(ruleFont);
		g2d.setColor(Color.WHITE);
		g2d.drawString("Score: " + score, 10, 20);
		// score printer
		g2d.setColor(Color.red);
		g2d.setFont(font);
		g2d.drawString("Lives: " + lives, (int) (WIDTH - WIDTH / 8), (int) (HEIGHT / 32));
		if (checkIfSpawningIsAllowed(false) != -1) {
			if (checkIfSpawningIsAllowed(false) > 15)
				g2d.setColor(Color.GREEN);
			drawCenterText(g2d, -WIDTH / 5, 5 * HEIGHT / 22,
					"Time left in Wave " + level + ": " + checkIfSpawningIsAllowed(false), ruleFont);
		}
		if (frameCooldown > 0) {
			g2d.setFont(ruleFont);
			g2d.drawString((frameCooldown * 1000) / FPS + "ms", (int) (WIDTH - WIDTH / 8), (int) (HEIGHT / 16));
		}
		if (framesShown > 180) {
			// baby
			g2d.setColor(Color.ORANGE);
			g2d.setStroke(new BasicStroke(10));
			g2d.drawOval((int) (b.getLocation().x - b.getMass() / 4), (int) (b.getLocation().y - b.getMass() / 4),
					(int) (b.getMass() / 2), (int) (b.getMass() / 2));
		}
	}

	public static void run(String[] args) throws InterruptedException {
		PelletDodger pelletDodger = new PelletDodger();
		frame.add(pelletDodger);
		frame.setSize((int) WIDTH, (int) HEIGHT);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);// full-screen
		frame.setUndecorated(true);// no border
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addKeyListener(pelletDodger);
		frame.addMouseListener(pelletDodger);
		frame.setFocusable(true);
		frame.requestFocusInWindow();
		System.out.println("Monitor Width: " + WIDTH + "      Monitor Height: " + HEIGHT);

		while (running) {
			while (lives > 0 && !showMenu) {// game loop
				checkIfSpawningIsAllowed(true);
				moveObjects();
				checkCollisions();
				createObjects();
				if (framesShown == 180) {// spawn baby
					b = new Baby(p.getPosition().x, p.getPosition().y);
				}
				framesShown++;
				frameCooldown--;
				pelletDodger.repaint();
				Thread.sleep(1000 / FPS, 66666);
			}
			if (!checkedForHighScore)
				checkIfScoreIsGlobalRecord();
			pelletDodger.repaint();
		}
	}

	private static int checkIfScoreIsGlobalRecord() {
		int globalScore = 0;
		URL high;
		try {
			high = new URL("http://www.neilsorkin.com/highscorekeeper.php?high=-1");
			try {
				BufferedReader highCheck = new BufferedReader(new InputStreamReader(high.openStream()));
				high.openConnection();
				globalScore = Integer.parseInt(highCheck.readLine());
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (score > globalScore && !scoreIsHigh) {
				try {
					high = new URL("http://www.neilsorkin.com/highscorekeeper.php?high=" + score);
					high.openConnection();
					scoreIsHigh = true;
				} catch (Exception e) {
					System.err.println("oof" + e);
				}
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		return globalScore;
	}

	private static int checkIfSpawningIsAllowed(boolean testing) {
		if (framesShown == 180) {
			if (milliStart == -1) {
				milliStart = System.currentTimeMillis();
			}
		} else if (framesShown > 180) {
			long currentTime = System.currentTimeMillis();
			if ((currentTime - milliStart) % 1000 <= ((int) (Math.random() * level + 1) * 1000) / FPS) {
				if ((currentTime - milliStart) % 30000 <= 15000)
					spawning = true;
				if (testing && (currentTime - milliStart) % 30000 <= 1000 / FPS)
					level++;
			} else
				spawning = false;
			return 30 - (int) ((currentTime - milliStart) % 30000) / 1000;
		}
		return -1;
	}

	private static void createObjects() {
		if (autoFire && framesShown % 5 == 0) {
			double x = MouseInfo.getPointerInfo().getLocation().getX();
			double y = MouseInfo.getPointerInfo().getLocation().getY();
			double magnitude = Math
					.sqrt(Math.pow(x - p.getPosition().getX(), 2) + Math.pow(y - p.getPosition().getY(), 2));
			double speed = HEIGHT / 64;
			double pointAtX = speed * ((p.getLook().getX() - p.getPosition().getX()) / magnitude);
			double pointAtY = speed * ((p.getLook().getY() - p.getPosition().getY()) / magnitude);
			pellets.add(new Pellet(
					new Point2D.Double((p.getPosition().getX() + pointAtX), (p.getPosition().getY() + pointAtY)),
					(pointAtX + (Math.random() * HEIGHT / 500)), (pointAtY + (Math.random() * HEIGHT / 500))));
		}
		// create new shapes
		if (spawning) {
			obstacles.add(new Obstacle((int) (Math.random() * 4 + 3),
					(int) (WIDTH / 2 + (Math.random() * WIDTH / 16) - WIDTH / 32),
					(int) (HEIGHT / 2 + (Math.random() * HEIGHT / 16) - HEIGHT / 32),
					getRandOne() * ((int) (Math.random() * 4) + 2), getRandOne() * ((int) (Math.random() * 4) + 2)));
		}
	}

	private static void checkCollisions() {
		// pellet-shape collision
		for (int pel = 0; pel < pellets.size(); pel++) {
			for (int g = 0; g < obstacles.size(); g++) {
				if (pellets.size() > pel && obstacles.get(g).containsPellet(pellets.get(pel))) {
					score++;
					if (framesShown > 180)
						b.addMass();
					pellets.remove(pel);
					if (obstacles.get(g).getLayerCount() > 3)
						obstacles.set(g, obstacles.get(g).stripLayer());
					else
						obstacles.remove(g);
				}
			}
		}
		// baby-shape collision
		for (int o = 0; o < obstacles.size(); o++) {
			if (framesShown > 180 && obstacles.get(o).containsBaby(b)) {
				obstacles.remove(o);
			}
		}
		// player-shape collision
		for (Obstacle obs : obstacles) {
			if (frameCooldown < 0 && obs.containsPlayer(p)) {
				lives--;
				frameCooldown = 60;
			}
		}
	}

	private static void moveObjects() {
		setLoc(MouseInfo.getPointerInfo().getLocation());
		p.move();// player move
		if (Q1.contains(p.getPosition())) {
			quadrant1Cooldown = FPS * 15;
			quadrant2Cooldown--;
			quadrant3Cooldown--;
			quadrant4Cooldown--;
		} else if (Q2.contains(p.getPosition())) {
			quadrant2Cooldown = FPS * 15;
			quadrant1Cooldown--;
			quadrant3Cooldown--;
			quadrant4Cooldown--;
		} else if (Q3.contains(p.getPosition())) {
			quadrant3Cooldown = FPS * 15;
			quadrant2Cooldown--;
			quadrant1Cooldown--;
			quadrant4Cooldown--;
		} else {
			quadrant4Cooldown = FPS * 15;
			quadrant2Cooldown--;
			quadrant3Cooldown--;
			quadrant1Cooldown--;
		}
		if (quadrant1Cooldown <= 0 || quadrant2Cooldown <= 0 || quadrant3Cooldown <= 0 || quadrant4Cooldown <= 0) {
			lives--;
			quadrant1Cooldown = FPS * 15;
			quadrant2Cooldown = FPS * 15;
			quadrant3Cooldown = FPS * 15;
			quadrant4Cooldown = FPS * 15;
		}
		if (framesShown > 180) {// baby move
			b.move(p.getPosition(), magnetActive);
		}
		for (int g = 0; g < obstacles.size(); g++) {// move obstacles
			obstacles.get(g).move(WIDTH, HEIGHT);
		}
		// move pellets
		for (int k = 0; k < pellets.size(); k++) {
			if (pellets.get(k).getLoc().getX() == -10) {
				pellets.remove(k);
				k--;
			} else
				pellets.get(k).movePellet(WIDTH, HEIGHT, spawnArea);
		}
	}

	public static void setLoc(Point location) {
		p.lookAt(location);
	}

	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		if (code == KeyEvent.VK_W) {// up
			p.heldDown(1);
		}
		if (code == KeyEvent.VK_S) {// down
			p.heldDown(2);
		}
		if (code == KeyEvent.VK_A) {// left
			p.heldDown(3);
		}
		if (code == KeyEvent.VK_D) {// right
			p.heldDown(4);
		}
		if (code == KeyEvent.VK_SPACE) {
			magnetActive = true;
		}
		if (code == KeyEvent.VK_ESCAPE) {// quit
			JOptionPane.showMessageDialog(new JOptionPane(),
					"On my honor as a member of the Woodson HS Community,\n"
							+ "I, Neil Sorkin certify that I have neither given \n"
							+ "nor received unauthorized aid on this assignment, \n"
							+ "that I have cited my sources for authorized aid, and \n"
							+ "that this project was started on or after April 18, 2018.",
					"Honor Code", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
		}
		if (code == KeyEvent.VK_G) {
			showMenu = false;
		}
		if (code == KeyEvent.VK_R && lives <= 0) {// restart
			framesShown = 0;
			obstacles.clear();
			pellets.clear();
			p = new Player((int) HEIGHT, (int) WIDTH, playerSize);
			b = new Baby(p.getPosition().x, p.getPosition().x);
			frameCooldown = 0;
			lives = 3;
			milliStart = -1;
			level = 1;
			score = 0;
			quadrant1Cooldown = FPS * 15;
			quadrant2Cooldown = FPS * 15;
			quadrant3Cooldown = FPS * 15;
			quadrant4Cooldown = FPS * 15;
			checkedForHighScore = false;
			scoreIsHigh = false;
		}
		if (code == KeyEvent.VK_K) {
			framesShown = 0;
			obstacles.clear();
			pellets.clear();
			p = new Player((int) HEIGHT, (int) WIDTH, playerSize);
			b = new Baby(p.getPosition().x, p.getPosition().x);
			lives = 3;
		}

		if (code == KeyEvent.VK_E) {
			autoFire = !autoFire;
		}
	}

	public void keyReleased(KeyEvent e) {
		int code = e.getKeyCode();
		if (code == KeyEvent.VK_W && p.getVert() != 1) {
			p.keyUp(1);
		}
		if (code == KeyEvent.VK_S && p.getVert() != -1) {
			p.keyUp(2);
		}
		if (code == KeyEvent.VK_A && p.getHori() != 1) {
			p.keyUp(3);
		}
		if (code == KeyEvent.VK_D && p.getHori() != -1) {
			p.keyUp(4);
		}
		if (code == KeyEvent.VK_SPACE) {
			magnetActive = false;
		}
	}

	public void keyTyped(KeyEvent e) {
		// ignore
	}

	// mouse events
	public void mousePressed(MouseEvent m) {
		if (!autoFire) {
			int x = m.getX();
			int y = m.getY();
			double magnitude = Math
					.sqrt(Math.pow(x - p.getPosition().getX(), 2) + Math.pow(y - p.getPosition().getY(), 2));
			int speed = (int) (HEIGHT / 64);
			double pointAtX = speed * ((p.getLook().getX() - p.getPosition().getX()) / magnitude);
			double pointAtY = speed * ((p.getLook().getY() - p.getPosition().getY()) / magnitude);
			pellets.add(new Pellet(
					new Point((int) (p.getPosition().getX() + pointAtX), (int) (p.getPosition().getY() + pointAtY)),
					(int) pointAtX, (int) pointAtY));
		}
	}

	public void mouseReleased(MouseEvent m) {
	}

	public void mouseClicked(MouseEvent arg0) {
		// ignore
	}

	public void mouseEntered(MouseEvent arg0) {
		// ignore
	}

	public void mouseExited(MouseEvent arg0) {
		// ignore
	}

	public void mouseDragged(MouseEvent arg0) {
		// ignore
	}

	public void mouseMoved(MouseEvent m) {
		// ignore
	}

	public static int getRandOne() {
		double num = Math.random();
		if (num > 0.5) {
			return 1;
		}
		return -1;
	}
}