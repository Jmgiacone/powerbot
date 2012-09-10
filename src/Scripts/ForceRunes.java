package Scripts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.util.LinkedList;

import org.powerbot.concurrent.strategy.Strategy;
import org.powerbot.game.api.ActiveScript;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Tabs;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.GroundItems;
import org.powerbot.game.api.methods.node.Menu;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.Area;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.node.GroundItem;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.Widget;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.Context;
import org.powerbot.game.bot.event.listener.PaintListener;

@Manifest(name = "Force Runes", 
	version = 0.1, 
	description = "(gp)Picks up runes in the dark warriors fortress.", 
	authors = "TaylorSwift",
	website = "")
public class ForceRunes extends ActiveScript implements PaintListener
{
	
	/**
	 * PAINT DIMENSIONS 765x50
	 */

	Area fortress = new Area(new Tile[] { new Tile(3018, 3642, 0), 
			new Tile(3024, 3642, 0), new Tile(3024, 3640, 0), 
			new Tile(3033, 3640, 0), new Tile(3033, 3635, 0), 
			new Tile(3018, 3635, 0) });
	
	Area innerPolygon = new Area(new Tile[] { new Tile(3018, 3642, 0), 
			new Tile(3024, 3642, 0), new Tile(3024, 3635, 0), 
			new Tile(3018, 3635, 0) });
	
	private String status = "";

	private Tile currTile;
	
	private boolean inArea, playerDetected = false;
	
	private int stopAt = 0,
			height = (int) Game.getDimensions().getHeight(),
			width = (int) Game.getDimensions().getWidth();
	
	private WidgetChild play, select, back;

	@Override
	protected void setup() {
		provide(new PickUp());
		provide(new Idle());
		play = new Widget(906).getChild(197);
		select = new Widget(906).getChild(28);
		back = new Widget(906).getChild(262);
		provide(new RareCases());
	}
	
	private final LinkedList<MousePathPoint> mousePath = new LinkedList<MousePathPoint>();

	@SuppressWarnings("serial")
	private class MousePathPoint extends Point { // All credits to Enfilade

		private long finishTime;
		@SuppressWarnings("unused")
		private double lastingTime;

		public MousePathPoint(int x, int y, int lastingTime) {
			super(x, y);
			this.lastingTime = lastingTime;
			finishTime = System.currentTimeMillis() + lastingTime;
		}

		public boolean isUp() {
			return System.currentTimeMillis() > finishTime;
		}
	}
	
	public static void antiBan(int multiply) {
		int random = Random.nextInt(0, (10 * multiply));
		switch (random) {
		case 1:
			if (Random.nextInt(0, 10) == 5) {
				Mouse.move(Random.nextInt(0, 500) + 1,
						Random.nextInt(0, 500) + 1);

				Time.sleep(200, 600);

				Mouse.move(Random.nextInt(0, 500) + 1,
						Random.nextInt(0, 500) + 1);
			}
			break;
		case 2:
			if (Random.nextInt(0, 6) == 2) {
				Camera.setAngle(Random.nextInt(50, 360) + 1);
				Time.sleep(400, 1200);
			}
			break;
		case 4:
			if (Random.nextInt(0, 4) == 3) {
				Camera.setPitch(Random.nextInt(30, 75));
				Time.sleep(200, 300);
			}
			break;
		case 5:
			if (Random.nextInt(0, 5) == 1) {
				Mouse.move(Random.nextInt(50, 100) + 1,
						Random.nextInt(50, 100) + 1);
				Time.sleep(1, 300);
				Mouse.move(Random.nextInt(50, 100) + 1,
						Random.nextInt(50, 100) + 1);
			}
			break;
		case 6:
			if (Random.nextInt(0, 15) == 10) {
				Tabs.FRIENDS.open();
				Time.sleep(2000, 3000);
				Tabs.INVENTORY.open();
			}
		default:
			break;
		}
	}

	public boolean shouldIdle() {
		GroundItem chaos = GroundItems.getNearest(562);
		GroundItem body = GroundItems.getNearest(559);
		GroundItem mind = GroundItems.getNearest(558);
		GroundItem fire = GroundItems.getNearest(554);
		GroundItem water = GroundItems.getNearest(555);
		GroundItem air = GroundItems.getNearest(556);
		GroundItem earth = GroundItems.getNearest(557);
		return chaos == null && body == null && mind == null && fire == null
				&& water == null && air == null && earth == null;
	}
	
	private class RareCases extends Strategy implements Runnable {
		public boolean validate() {
			return !fortress.contains(Players.getLocal().getLocation())
					|| Players.getLoaded().length > 1;
		}

		@Override
		public void run() {
			if (Players.getLoaded().length > 0) {
				playerDetected = true;
				status = "Detected a player"; 
				Context.setLoginWorld(Random.nextInt(1,139));
				Game.logout(true);
				playerDetected = false;
			} else {
				status = "Not in area";
				Game.logout(true);
				stop();
			}
		}
	}

	private class Idle extends Strategy implements Runnable {
		public boolean validate() {
			return shouldIdle();
		}

		@Override
		public void run() {
			status = "Waiting for runes to spawn";
			Tile centerTile = new Tile(3023, 3638, 0);
			currTile = centerTile;
			SceneObject door = SceneEntities.getNearest(64831);
			Tile doorTile = new Tile(3025, 3637, 0);
			if (door != null && door.getLocation().equals(doorTile)) {
				if (door.isOnScreen()) {
					if (!Players.getLocal().isMoving()) {
						if (door.getLocation().equals(doorTile)) {
							status = "Opening door";
							door.interact("Open");
						}
					}
				} else {
					status = "Finding door";
					Walking.walk(door);
				}
			} else if (!innerPolygon.contains(Players.getLocal().getLocation())) {
				status = "Walking to center";
				Walking.walk(centerTile);
				Time.sleep(100,150);
			}
			if (innerPolygon.contains(Players.getLocal().getLocation())) {
				antiBan(4);
				Time.sleep(10,20);
			}
		}
	}

	private class PickUp extends Strategy implements Runnable {

		public boolean validate() {
			return fortress.contains(Players.getLocal().getLocation());
		}

		@Override
		public void run() {
			GroundItem chaos = GroundItems.getNearest(562);
			GroundItem body = GroundItems.getNearest(559);
			GroundItem mind = GroundItems.getNearest(558);
			GroundItem fire = GroundItems.getNearest(554);
			GroundItem water = GroundItems.getNearest(555);
			GroundItem air = GroundItems.getNearest(556);
			GroundItem earth = GroundItems.getNearest(557);
			SceneObject door = SceneEntities.getNearest(64831);
			Tile doorTile = new Tile(3025, 3637, 0);
			if (door != null && door.getLocation().equals(doorTile)) {
				currTile = door.getLocation();
				if (door.isOnScreen()) {
					if (!Players.getLocal().isMoving()) {
						if (door.getLocation().equals(doorTile)) {
							status = "Opening";
							door.interact("Open");
						}
					}
				} else {
					status = "Finding door";
					Walking.walk(door);
				}
			} else if (chaos != null) {
				status = "Chaos rune";
				currTile = chaos.getLocation();
				if (!chaos.isOnScreen()) {
					Walking.walk(chaos);
				} else {
					if (!Players.getLocal().isMoving())
						Mouse.hop((int)chaos.getCentralPoint().getX(), (int)chaos.getCentralPoint().getY());
						Menu.select("Take");
						Time.sleep(10,20);
				}
			} else if (body != null) {
				status = "Body rune";
				currTile = body.getLocation();
				if (!body.isOnScreen()) {
					Walking.walk(body);
				} else {
					if (!Players.getLocal().isMoving())
						Mouse.hop((int)body.getCentralPoint().getX(), (int)body.getCentralPoint().getY());
						Menu.select("Take");
						Time.sleep(10,20);
				}
			} else if (mind != null) {
				 status = "Mind rune";
				 currTile = mind.getLocation();
				if (!mind.isOnScreen()) {
					Walking.walk(mind);
				} else {
					if (!Players.getLocal().isMoving())
						Mouse.hop((int)mind.getCentralPoint().getX(), (int)mind.getCentralPoint().getY());
						Menu.select("Take");
						Time.sleep(10,20);
				}
			} else if (fire != null) {
				 status = "Fire rune";
				 currTile = fire.getLocation();
				if (!fire.isOnScreen()) {
					Walking.walk(fire);
				} else {
					if (!Players.getLocal().isMoving())
						Mouse.hop((int)fire.getCentralPoint().getX(), (int)fire.getCentralPoint().getY());
						Menu.select("Take");
						Time.sleep(10,20);
				}
			} else if (water != null) {
				 status = "Water rune";
				 currTile = water.getLocation();
				if (!water.isOnScreen()) {
					Walking.walk(water);
				} else {
					if (!Players.getLocal().isMoving())
						Mouse.hop((int)water.getCentralPoint().getX(), (int)water.getCentralPoint().getY());
						Menu.select("Take");
						Time.sleep(10,20);
				}
			} else if (air != null) {
				 status = "Air rune";
				 currTile = air.getLocation();
				if (!air.isOnScreen()) {
					Walking.walk(air);
				} else {
					if (!Players.getLocal().isMoving())
						Mouse.hop((int)air.getCentralPoint().getX(), (int)air.getCentralPoint().getY());
						Menu.select("Take");
						Time.sleep(10,20);
				}
			} else if (earth != null) {
				 status = "Earth rune";
				 currTile = earth.getLocation();
				if (!earth.isOnScreen()) {
					Walking.walk(earth);
				} else {
					if (!Players.getLocal().isMoving())
						Mouse.hop((int)earth.getCentralPoint().getX(), (int)earth.getCentralPoint().getY());
						Menu.select("Take");
						Time.sleep(10,20);
				}
			}
		}
	}
	
	long startTime = System.currentTimeMillis();
	Color fadeGreen = new Color(0,100,0,100);
	Color fadeBlue = new Color(0,0,100,100);
	Color fadeRed = new Color(100,0,0,100);
	Font large = new Font("Arial", Font.BOLD, 36);
	Font small = new Font("Arial", 0, 12);
	private long lastFrame = System.currentTimeMillis();
	Tile local;
	
	public void drawMouse(Graphics g) {
		g.setColor(Color.RED);
		int mouseY = (int) Mouse.getLocation().getY();
		int mouseX = (int) Mouse.getLocation().getX();
		g.drawLine(mouseX - 5, mouseY + 5, mouseX + 5, mouseY - 5);
		g.drawLine(mouseX + 5, mouseY + 5, mouseX - 5, mouseY - 5);
		g.drawOval(mouseX - 8, mouseY - 8, 16, 16);
	}

	@Override
	public void onRepaint(Graphics g1) {
		inArea = fortress.contains(Players.getLocal().getLocation());
		local = Players.getLocal().getLocation();
		Graphics2D g = (Graphics2D) g1;
		long framedelay = System.currentTimeMillis() - lastFrame;
        lastFrame = System.currentTimeMillis();
        int fps = (int) (1000.0 / framedelay);
		long millis = System.currentTimeMillis() - startTime;
		long hours = millis / (1000 * 60 * 60);
		millis -= hours * (1000 * 60 * 60);
		long minutes = millis / (1000 * 60);
		millis -= minutes * (1000 * 60);
		long seconds = millis / 1000;
		while (!mousePath.isEmpty() && mousePath.peek().isUp())
			mousePath.remove();
		Point clientCursor = Mouse.getLocation();
		MousePathPoint mpp = new MousePathPoint(clientCursor.x, clientCursor.y,
				400); // 1000 = 1 second lasting time
		if (mousePath.isEmpty() || !mousePath.getLast().equals(mpp))
			mousePath.add(mpp);
		MousePathPoint lastPoint = null;
		for (MousePathPoint a : mousePath) {
			if (lastPoint != null) {
				g.setColor(Color.RED);
				g.drawLine(a.x, a.y, lastPoint.x, lastPoint.y);
			}
			lastPoint = a;
		}
		drawTile(currTile, fadeGreen, Color.GREEN, g);
		drawLocalPlayer(local, fadeBlue, Color.BLUE, g);
		drawMouse(g);
		g.setColor(Color.BLACK);
		if (!playerDetected) {
			g.setFont(small);
			g.setColor(Color.BLACK);
			g.drawString("Timer: " + hours + ":" + minutes + ":" + seconds, 550, 390);
			g.drawString("Debug: " + status, 550, 410);
			g.drawString("" + currTile, 592, 430);
			g.drawString("" + inArea, 592, 450);
			g.drawString("fps: " + fps, 592, 470);
		} else {
			g.setColor(fadeRed);
			g.drawRect(0, 0, width, height);
			g.fillRect(0, 0, width, height);
			g.setFont(large);
			g.setColor(Color.BLACK);
			g.drawString("PLAYER IN AREA", 160, 280);
		}
	}
	
	private void drawLocalPlayer(Tile loc, Color c, Color c2, Graphics g) {
		g.setColor(c);
		if (currTile != null) {
			for (Polygon p : loc.getBounds()) {
				g.fillPolygon(p);
		    	g.setColor(c2);
		    	g.drawPolygon(p);
			}
		}
	}
	
	private void drawTile(Tile loc, Color c, Color c2, Graphics g) {
		g.setColor(c);
		if (currTile != null) {
			for (Polygon p : loc.getBounds()) {
				g.fillPolygon(p);
		    	g.setColor(c2);
		    	g.drawPolygon(p);
			}
		}
	}
	
}