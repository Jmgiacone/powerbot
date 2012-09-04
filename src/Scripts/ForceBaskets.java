package Scripts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.lang.Runnable;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.powerbot.concurrent.strategy.Strategy;
import org.powerbot.game.api.ActiveScript;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.widget.Bank;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.widget.Widget;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.event.listener.PaintListener;

@Manifest(authors = "9Ox", version = 1.0, description = "Fills baskets", name = "Force Baskets", vip = false)
public class ForceBaskets extends ActiveScript implements PaintListener,
		MouseMotionListener, MouseListener {

	private final Color mouse = new Color(0, 0, 0, 140);

	@SuppressWarnings("unused")
	private static int idle = 0, fruitUsing = 0, basketUsing = 0, index = 0,
			orangePrice = 0, orangeBasketPrice = 0, applePrice = 0,
			appleBasketPrice = 0, strawberryPrice = 0,
			strawberryBasketPrice = 0, bananaPrice = 0, bananaBasketPrice = 0,
			tomatoPrice = 0, tomatoBasketPrice = 0, basketPrice = 0,
			realPrice = 0, realBasketPrice = 0, profit = 0, filled = 0, start = 0;

	private static final int ORANGE = 2108, APPLE = 1955, EMPTY_BASKET = 5376,
			FILLED_ORANGE = 5396, FILLED_APPLE = 5386, STRAWBERRY = 5504,
			FILLED_STRAWBERRY = 5406, BANANA = 1963, FILLED_BANANA = 5416,
			TOMATO = 0, FILLED_TOMATO = 0;

	private WidgetChild depositButton;

	private WidgetChild[] inventory = new WidgetChild[4];

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

	public static int getPrice(final int id) {
		try {
			final URL url = new URL(
					"http://open.tip.it/json/ge_single_item?item="
							.concat(Integer.toString(id)));
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(url.openStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("mark_price")) {
					reader.close();
					return Integer.parseInt(line.substring(
							line.indexOf("mark_price") + 13,
							line.indexOf(",\"daily_gp") - 1)
							.replaceAll(",", ""));
				}
			}
		} catch (final Exception e) {
			return -1;
		}
		return -1;
	}
	
	/**
	 * A recursive algorithm for inserting commas into a given number
	 * @param x The number to be comma-ized
	 * @return The comma-ized number in the form of a String
	 */
	private String insertComma(int x)
    {
        String s = x + "";
        
        if(s.length() <= 3)
        {
            return s;
        }
        
        return insertComma(x / 1000) + "," + s.substring(s.length() - 3);
        
    }

	@Override
	protected void setup() {
		orangePrice = getPrice(ORANGE);
		orangeBasketPrice = getPrice(FILLED_ORANGE);
		applePrice = getPrice(APPLE);
		appleBasketPrice = getPrice(FILLED_APPLE);
		bananaPrice = getPrice(BANANA);
		bananaBasketPrice = getPrice(FILLED_BANANA);
		tomatoPrice = getPrice(TOMATO);
		tomatoBasketPrice = getPrice(FILLED_TOMATO);
		strawberryPrice = getPrice(STRAWBERRY);
		strawberryBasketPrice = getPrice(FILLED_STRAWBERRY);
		basketPrice = getPrice(EMPTY_BASKET);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BasketsGui frame = new BasketsGui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		provide(new openBank());
		depositButton = new Widget(762).getChild(34);
		inventory[0] = new Widget(679).getChild(0).getChild(0);
		inventory[1] = new Widget(679).getChild(0).getChild(1);
		inventory[2] = new Widget(679).getChild(0).getChild(2);
		inventory[3] = new Widget(679).getChild(0).getChild(3);
	}
	
	public static void antiBan(int multiply) {
		int random = Random.nextInt(0, (10 * multiply));
		switch (random) {
		case 1:
			System.out.println("Hit case 1");
			if (Random.nextInt(0, 10) == 5) {
				Mouse.move(Random.nextInt(0, 500) + 1,
						Random.nextInt(0, 500) + 1);

				Time.sleep(200, 600);

				Mouse.move(Random.nextInt(0, 500) + 1,
						Random.nextInt(0, 500) + 1);
			}
			break;
		case 2:
			System.out.println("Hit case 2");
			if (Random.nextInt(0, 6) == 2) {
				Camera.setAngle(Random.nextInt(0, 360) + 1);
				Time.sleep(400, 1200);
			}
			break;
		case 4:
			System.out.println("Hit case 4");
			if (Random.nextInt(0, 4) == 3) {
				Camera.setPitch(Random.nextInt(30, 75));
				Time.sleep(200, 300);
			}
			break;
		case 5:
			System.out.println("Hit case 5");
			if (Random.nextInt(0, 5) == 1) {
				Mouse.move(Random.nextInt(50, 100) + 1,
						Random.nextInt(50, 100) + 1);
				Time.sleep(1, 300);
				Mouse.move(Random.nextInt(50, 100) + 1,
						Random.nextInt(50, 100) + 1);
			}
			break;
		default:
			break;
		}
	}
	
	private class Antiban extends Strategy implements Runnable {

		@Override
		public void run() {
			antiBan(4);
		}
		
		public boolean validate() {
			return true;
		}
	}

	private class openBank extends Strategy implements Runnable {

		@Override
		public void run() {
			Bank.open();
		}

		public boolean validate() {
			return !Bank.isOpen();
		}
	}

	private class withdraw extends Strategy implements Runnable {

		@Override
		public void run() {
			if (Bank.getItemCount(fruitUsing) < 1
					|| Bank.getItemCount(EMPTY_BASKET) < 1) {
				System.out.println("No supplies left.");
				Bank.close();
				Game.logout(true);
				stop();
			}
			if (Bank.getItem(basketUsing) != null) {
				filled = Bank.getItem(basketUsing).getStackSize() - start;
			}
			Bank.withdraw(EMPTY_BASKET, 4);
			Bank.withdraw(fruitUsing, 0);
		}

		public boolean validate() {
			return Inventory.getCount() == 0 && Bank.isOpen();
		}
	}

	private class deposit extends Strategy implements Runnable {

		@Override
		public void run() {
			index = 0;
			Mouse.hop((int) depositButton.getCentralPoint().getX(),
					(int) depositButton.getCentralPoint().getY());
			Mouse.click(true);
			while (Inventory.getItem(basketUsing) != null) {
				Time.sleep(1);
				idle++;
				if (Inventory.getItem(basketUsing) == null) {
					idle = 0;
					break;
				} else if (Inventory.getItem(basketUsing) != null) {
					Mouse.hop((int) depositButton.getCentralPoint().getX(),
							(int) depositButton.getCentralPoint().getY());
					idle++;
					if (idle > 900) {
						Mouse.click(true);
						Time.sleep(500);
					}
				}
			}
		}

		public boolean validate() {
			return Bank.isOpen() && Inventory.getCount(fruitUsing) <= 4;
		}
	}

	private class fill extends Strategy implements Runnable {
		@Override
		public void run() {
			idle = 0;
			// int empties = Inventory.getCount(EMPTY_BASKET);
			if (Inventory.getCount(EMPTY_BASKET) > 3) {
				for (int i = 0; i < inventory.length; i++) {
					inventory[i].interact("Fill");
				}
			}
			if (Inventory.getCount(basketUsing) < 4); {
				idle++;
				System.out.println(idle);
				if (idle > 10) {
					idle = 0;
					Bank.depositInventory();
				}
			}
			// if (Inventory.getCount(EMPTY_BASKET) < empties) {
			// index++;
			// }
		}

		public boolean validate() {
			return Bank.isOpen() && Inventory.getItem(EMPTY_BASKET) != null
					&& index != 4;
		}
	}

	private final Color fadeBlack = new Color(0, 0, 0, 150);
	private final BasicStroke stroke2 = new BasicStroke(1);
	private final Color fadeOrange = new Color(255, 140, 0, 140);
	long startTime = System.currentTimeMillis();

	@Override
	public void onRepaint(Graphics g1) {
		long millis = System.currentTimeMillis() - startTime;
		long hours = millis / (1000 * 60 * 60);
		millis -= hours * (1000 * 60 * 60);
		long minutes = millis / (1000 * 60);
		millis -= minutes * (1000 * 60);
		long seconds = millis / 1000;
		Graphics2D g = (Graphics2D) g1;
		g.setStroke(stroke2);
		drawMouse(g1, mouse);
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
				g.setColor(fadeBlack);
				g.drawLine(a.x, a.y, lastPoint.x, lastPoint.y);
			}
			lastPoint = a;
		}
		g.setColor(fadeOrange);
		g.drawRect(0, 0, 765, 47);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 765, 47);
		g.setColor(Color.WHITE);
		g.drawString("Force Baskets", 2, 10);
		g.drawString("Profit per basket: " + profit, 2, 22);
		g.drawString("Profit: " + insertComma((profit * filled)) + " / " + insertComma((int) (((profit * filled) * 3600000d) / (System
				.currentTimeMillis() - startTime))), 2, 34);
		g.drawString("Time running: " + hours + "h" + minutes + "m" + seconds + "s", 2, 46);
	}

	public void drawMouse(Graphics g, final Color color) {
		g.setColor(Color.BLACK);
		int mouseY = (int) Mouse.getLocation().getY();
		int mouseX = (int) Mouse.getLocation().getX();
		g.drawLine(mouseX - 5, mouseY + 5, mouseX + 5, mouseY - 5);
		g.drawLine(mouseX + 5, mouseY + 5, mouseX - 5, mouseY - 5);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("serial")
	public class BasketsGui extends JFrame {

		private JPanel contentPane;

		/**
		 * Create the frame.
		 */
		public BasketsGui() {
			setTitle("Force Baskets UI");
			setBounds(100, 100, 240, 117);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);
			contentPane.setLayout(null);

			JLabel lblFill = new JLabel("Fill:");
			lblFill.setBounds(10, 11, 28, 14);
			contentPane.add(lblFill);

			final JComboBox<String> comboBox = new JComboBox<String>();
			comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {
					"Strawberry", "Apple", "Orange", "Banana", "Tomato" }));
			comboBox.setBounds(28, 8, 84, 20);
			contentPane.add(comboBox);

			final JCheckBox chckbxAntiban = new JCheckBox("Antiban");
			chckbxAntiban.setBounds(121, 7, 97, 23);
			contentPane.add(chckbxAntiban);

			JButton btnNewButton = new JButton("Start");
			btnNewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					String chosen = comboBox.getSelectedItem().toString();
					if (chosen.equals("Strawberry")) {
						fruitUsing = ForceBaskets.STRAWBERRY;
						basketUsing = ForceBaskets.FILLED_STRAWBERRY;
						realPrice = ForceBaskets.strawberryPrice;
						realBasketPrice = ForceBaskets.strawberryBasketPrice;
					} else if (chosen.equals("Apple")) {
						fruitUsing = ForceBaskets.APPLE;
						basketUsing = ForceBaskets.FILLED_APPLE;
						realPrice = ForceBaskets.applePrice;
						realBasketPrice = ForceBaskets.appleBasketPrice;
					} else if (chosen.equals("Orange")) {
						fruitUsing = ForceBaskets.ORANGE;
						basketUsing = ForceBaskets.FILLED_ORANGE;
						realPrice = ForceBaskets.orangePrice;
						realBasketPrice = ForceBaskets.appleBasketPrice;
					} else if (chosen.equals("Banana")) {
						fruitUsing = ForceBaskets.BANANA;
						basketUsing = ForceBaskets.FILLED_BANANA;
						realPrice = ForceBaskets.bananaPrice;
						realBasketPrice = ForceBaskets.appleBasketPrice;
					} else if (chosen.equals("Tomato")) {
						fruitUsing = ForceBaskets.TOMATO;
						basketUsing = ForceBaskets.FILLED_TOMATO;
						realPrice = ForceBaskets.tomatoPrice;
						realBasketPrice = ForceBaskets.tomatoBasketPrice;
					}
					if (chckbxAntiban.isSelected()) {
						provide(new Antiban());
					}
					profit = realBasketPrice - ((realPrice * 5) + basketPrice);
					if (Bank.getItem(basketUsing) != null) {
						start = Bank.getItem(basketUsing).getStackSize();
					} else {
						start = 0;
					}
					provide(new fill());
					provide(new deposit());
					provide(new withdraw());
					setVisible(false);
					dispose();
				}
			});
			btnNewButton.setBounds(10, 45, 208, 23);
			contentPane.add(btnNewButton);
		}
	}
}