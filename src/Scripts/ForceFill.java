package Scripts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.lang.Runnable;

import org.powerbot.concurrent.strategy.Strategy;
import org.powerbot.core.event.events.MessageEvent;
import org.powerbot.core.event.listeners.MessageListener;
import org.powerbot.core.event.listeners.PaintListener;
import org.powerbot.core.script.ActiveScript;
import org.powerbot.core.script.job.Task;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.widget.Bank;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.Area;
import org.powerbot.game.api.wrappers.Identifiable;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.WidgetChild;

@Manifest(authors = "Jdog653, 9Ox", version = 0.03, description = "Fills Vials, Jugs, Buckets and wets Clay quickly and Efficiently", name = "Force Filler",
website = "http://www.powerbot.org/community/topic/750556-force-filler-fills-vialsjugsbucketsbowls-in-3-locations-60-70k-hr/")
public class ForceFill extends ActiveScript implements PaintListener,
		MouseListener, MouseMotionListener 	{

    private Node[] strategies;
    private Color c = new Color(65, 105, 225, 70),
            color1 = new Color(51, 102, 255),
            color2 = new Color(0, 0, 0);
    private long startTime,averageSoakTime, startSoakTime;
    private final Image img1 = getImage("http://i.imgur.com/NeVxB.png");
    private final RenderingHints antialiasing = new RenderingHints(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    private final BasicStroke stroke1 = new BasicStroke(1);
    private final Font font1 = new Font("Arial", 0, 9);

    private boolean clay = false, showhide = true, isStarted = false;
	//Final ID's of all items used in the script
	public final int VIAL = 229, JUG = 1935, BUCKET = 1925,
			GE_FOUNTAIN = 47150, FALADOR_FOUNTAIN = 11661,
			VARROCK_FOUNTAIN = 24214, FULL_VIAL = 227, FULL_JUG = 1937,//g1.drawRect(6, 509, 85, 15); x, y, w, h
			FULL_BUCKET = 1929, EMPTY_BOWL = 1923, FULL_BOWL = 1921, CLAY = 434, WET_CLAY = 1761, showHideX = 6, showHideY = 509, showHideWidth = 85, 
			showHideHeight = 15;

	//ID's that change throughout the script
	private int fullID, emptyID, fountainID, EMPTY_VIAL_PRICE = 0, EMPTY_JUG_PRICE = 0, EMPTY_BUCKET_PRICE = 0,
			FULL_VIAL_PRICE = 0, FULL_JUG_PRICE = 0, FULL_BUCKET_PRICE = 0, FULL_PRICE = 0, EMPTY_PRICE = 0, vialCount = 0, 
			EMPTY_BOWL_PRICE, FULL_BOWL_PRICE, refreshPrices = 0, CLAY_PRICE, WET_CLAY_PRICE;
	


	//What's being filled(Bucket, Jug, Vial, Bowl) and the state of the bot
	private String forcefill = "", botState = "";

    //The constants for the Bank areas
	public final static Area GEBANK = new Area(new Tile(3176, 3476, 0), new Tile(
			3184, 3483, 0)), FBANK = new Area(new Tile(2949, 3372, 0),
			new Tile(2943, 3368, 0)), VBANK = new Area(new Tile(3257, 3424, 0),
			new Tile(3250, 3419, 0)), GEFOUNTAIN = new Area(new Tile(3160,
			3487, 0), new Tile(3169, 3495, 0)), FFOUNTAIN = new Area(new Tile(
			2944, 3379, 0), new Tile(2950, 3385, 0)), VFOUNTAIN = new Area(
			new Tile(3241, 3432, 0), new Tile(3236, 3437, 0));
	
	//The specific Fountain and Bank used
	private Area FOUNTAIN, BANK;

	//The Tile constants of the Tiles for each Bank and Fountain
	public final static Tile VFOUNTAIN_TILE = new Tile(3240, 3434, 0),
			VBANK_TILE = new Tile(3253, 3422, 0), FFOUNTAIN_TILE = new Tile(
					2949, 3382, 0), FBANK_TILE = new Tile(2945, 3370, 0),
			GEFOUNTAIN_TILE = new Tile(3166, 3488, 0), GEBANK_TILE = new Tile(
					3178, 3481, 0);

	//The variant tiles for the selected Fountain and Bank
	private Tile FOUNTAIN_TILE, BANK_TILE;
	
	//The h'Gui
	forcefillgui gui;

	/**
	 * Checks to see if the given array of Identifiables (Inventories anyone?) contains the given Identifiable ID
	 * @param x The array of Identifiables to be searched
	 * @param id The item ID of the item to be searched for
	 * @return Whether or not the Identifiable with the given id resides in the array.
	 */
	private boolean containsID(Identifiable[] x, int id) 
	{
		for (Identifiable i : x) 
		{
			if (i.getId() == id) 
			{
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Moves the mouse off the screen
	 */
	private static void offScreen() {
		int choice = Random.nextInt(0, 4) + 1;

		switch (choice) {
		case 1:
			Mouse.move(Random.nextInt(-200, -50), Random.nextInt(-200, -50));
			break;
		case 2:
			Mouse.move(Random.nextInt(780, 1000), Random.nextInt(560, 1000));
			break;
		case 3:
			Mouse.move(Random.nextInt(780, 1000), Random.nextInt(-200, -50));
			break;
		case 4:
			Mouse.move(Random.nextInt(-200, -50), Random.nextInt(560, 1000));
		}
	}
	
	/**
	 * Either randomly moves the mouse, or opens the inventory to prevent Jagex from realizing that you're botting
	 */
	private void antiBan() {
		int random = Random.nextInt(0, 10);
		switch (random) {
		case 1:
			if (Random.nextInt(0, 10) == 5) {
				Mouse.move(Random.nextInt(0, 500) + 1,
						Random.nextInt(0, 500) + 1);

				Task.sleep(200, 600);

				Mouse.move(Random.nextInt(0, 500) + 1,
						Random.nextInt(0, 500) + 1);
			}
			break;
		case 2:
			if (Random.nextInt(0, 13) == 2) {
				Camera.setAngle(Random.nextInt(0, 360) + 1);
				Task.sleep(400, 1200);
			}
			break;
		case 3:
			if (Random.nextInt(0, 24) == 6) {
				offScreen();

				Task.sleep(Random.nextInt(600, Random.nextInt(1200, 2000)));
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * Checks the price of the item with the given ID via this website: http://services.runescape.com/m=itemdb_rs/
	 * @param id The item ID of the wanted item.
	 * @return The price
	 * @throws IOException If the ID doesn't exist
	 */
	private int getPrice(final int id)
    {
        String line;
        try
        {
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new URL("http://open.tip.it/json/ge_single_item?item="
                                    .concat(
                                    Integer.toString(id))).
                                    openStream()));
            while ((line = reader.readLine()) != null)
            {
                if (line.contains("mark_price"))
                {
                    reader.close();
                    return Integer.parseInt(line.substring(
                            line.indexOf("mark_price") + 13,
                            line.indexOf(",\"daily_gp") - 1)
                            .replaceAll(",", ""));
                }
            }
        }
        catch (final IOException e)
        {
            //Trouble reading the page
            return -1;
        }
        //Item.exists() == false :(
        return -1;
}
	
	/**
	 * Returns an image from the given URL	
	 * @param url The web address of the raw image
	 * @return The image at the given URL
	 */
	public Image getImage(String url) 
	{
	    Image im = null;
	    int i = 0;
	    
	    while(im == null && i < 50)
	    {
			try 
			{
				im = ImageIO.read(new URL(url));
			} 
			catch (IOException e) 
			{
				System.out.println("Try #" + (i + 1));
			}
			i++;
	    }
	    
	    return im;
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
	
	public void onRepaint(Graphics g1)
    {
		//Pulls prices from online every 10,000 repaints
		if(refreshPrices >= 10000)
		{
			System.out.println("Fetching Prices");
			EMPTY_PRICE = getPrice(emptyID);
			FULL_PRICE = getPrice(fullID);
			
			refreshPrices = 0;
		}
		if(isStarted)
        {
            Graphics2D g = (Graphics2D) g1;
            long millis = System.currentTimeMillis() - startTime, hours = millis / (1000 * 60 * 60), minutes, seconds;
            millis -= hours * (1000 * 60 * 60);
            minutes = millis / (1000 * 60);
            millis -= minutes * (1000 * 60);
            seconds = millis / 1000;

            g.setRenderingHints(antialiasing);
            if (showhide)
            {
                g1.drawImage(img1, 0, 388, null);
                g1.setColor(Color.BLACK);
                g1.drawString(forcefill, 200, 487);
                g1.drawString(hours + ":" + minutes + ":" + seconds, 200, 471);
                g1.drawString("" + botState, 10, 408);
                g1.drawString(insertComma(vialCount), 415, 487);
                g1.drawString("" + (FULL_PRICE - EMPTY_PRICE), 200, 502);
                g1.drawString(insertComma((FULL_PRICE - EMPTY_PRICE) * vialCount), 415, 471);

                //Profit per hour = (profit * 3600000d) / timeRan
                g1.drawString(insertComma((int)((((FULL_PRICE - EMPTY_PRICE) * vialCount) * 3600000d) /
                        (System.currentTimeMillis() - startTime))), 415,502);
            }

            g1.setColor(color1);
            g1.fillRect(6, 509, 85, 15);
            g1.setColor(color2);
            ((Graphics2D) g1).setStroke(stroke1);
            g1.drawRect(showHideX, showHideY, showHideWidth, showHideHeight);
            drawMouse(g1);
            g.setFont(font1);
            g.drawString("Hide/Show paint", 15, 520);

            refreshPrices++;
        }
	}

	@Override
	public void onStart()
	{
		EMPTY_VIAL_PRICE = getPrice(VIAL);
		FULL_VIAL_PRICE = getPrice(FULL_VIAL);
		EMPTY_BUCKET_PRICE = getPrice(BUCKET);
		FULL_BUCKET_PRICE = getPrice(FULL_BUCKET);
		EMPTY_JUG_PRICE = getPrice(JUG);
		FULL_JUG_PRICE = getPrice(FULL_JUG);
		FULL_BOWL_PRICE = getPrice(FULL_BOWL);
		EMPTY_BOWL_PRICE = getPrice(EMPTY_BOWL);
		CLAY_PRICE = getPrice(CLAY);
		WET_CLAY_PRICE = getPrice(WET_CLAY);

		if (gui == null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					gui = new forcefillgui();
					gui.setVisible(true);
				}
			});
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();

		//g1.drawRect(6, 509, 85, 15); x, y, w, h
		if (x >= showHideX && x < showHideX + showHideWidth && y >= showHideY && y < showHideY + showHideHeight)
        {
            showhide = !showhide;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void drawMouse(Graphics g1)
    {
		int mouseY = (int) Mouse.getLocation().getY();
		int mouseX = (int) Mouse.getLocation().getX();
		g1.drawOval(mouseX - 7, mouseY - 7, 14, 14);
		g1.setColor(c);
		g1.fillOval(mouseX - 7, mouseY - 7, 14, 14);
		g1.setColor(new Color(245, 255, 250, 140));
		g1.fillOval(mouseX - 1, mouseY - 1, 3, 2);
	}

    @Override
    public int loop() 
    {
        for(Node n : strategies)
        {
            if(n.activate())
            {
                n.execute();
            }
        }
        
        return 0;
    }


    private class BankItems extends Node
{
	@Override
	public void execute()
    {
		botState = "BANK_ITEMS";
		System.out.println(botState);

		//Player is in the chosen area
		if (BANK.contains(Players.getLocal().getLocation())) 
		{
			//If it happens to be the GE
			if (BANK_TILE.equals(GEBANK_TILE)) 
			{
				//Turn Camera to the GEBanker because the Bank.open() method is dumb and doesn't include the GE
                NPC geBanker = NPCs.getNearest(2718);
				if (!geBanker.isOnScreen()) 
				{
					Camera.turnTo(geBanker);
				}
			} 
			
			//Open the bank
			Bank.open();
			antiBan();
			Task.sleep(500);
			
			if (Bank.isOpen()) 
			{
				Bank.depositInventory();
				if (Bank.getItem(emptyID) == null)
				{
					System.out.println("Finished, no empties left");
					Bank.close();
					Game.logout(true);
                    stop();
				} 
				else 
				{
					Bank.withdraw(emptyID, 0);
				}
			}
		}
	}

	public boolean activate()
    {
		return !containsID(Inventory.getItems(), emptyID)
				&& BANK.contains(Players.getLocal().getLocation());
	}
}
private class WalkToBank extends Node
{
	public boolean activate() {
		return !containsID(Inventory.getItems(), emptyID)
				&& !BANK.contains(Players.getLocal().getLocation());
	}

	@Override
	public void execute()
    {
		botState = "WALK_TO_BANK";
		System.out.println(botState);
		Walking.findPath(BANK_TILE).traverse();
		antiBan();
		Task.sleep(500);
	}
}

private class UseClayOnFountain extends Node implements MessageListener {
    @Override
	public void execute() 
	{
		WidgetChild clayButton = Widgets.get(905, 14);
		startSoakTime = System.currentTimeMillis();
		
		int checkIdle = 0, i = 0;
		botState = "WET_CLAY";
		System.out.println(botState);
		

		final SceneObject fountain = SceneEntities
				.getNearest(fountainID);

		if (!fountain.isOnScreen()) 
		{
			Camera.turnTo(fountain);
		}

		//Algorithm for finding the last filled item in the event that filling is interrupted
		while (!(Inventory.getItems()[i].getId() == emptyID)) 
		{
			i++;
			
			if (Inventory.getItems()[i].getId() == emptyID) 
			{
				System.out.println("match found");
				break;
			}
		}
		
		Inventory.getItems()[i].getWidgetChild().click(true);
		Task.sleep(Random.nextInt(100, 200));
		
		if (FOUNTAIN_TILE.equals(FFOUNTAIN_TILE)) 
		{
			fountain.interact("Use", "" + forcefill.substring(forcefill
									.length()) + " -> Waterpump");
		}
		else
		{
			fountain.interact("Use", "" + forcefill.substring(forcefill
								.length()) + " -> Fountain");
		}
		
		System.out.println("Clicking Button");
		Mouse.click(clayButton.getAbsoluteX() + Random.nextInt(0, clayButton.getWidth()), 
				clayButton.getAbsoluteY() + Random.nextInt(0, clayButton.getHeight()), true);
		startSoakTime = System.currentTimeMillis();
		
		while (containsID(Inventory.getItems(), emptyID)) 
		{
			if (System.currentTimeMillis() - startSoakTime > 2800) 
			{
				checkIdle++;
				antiBan();
				Task.sleep(500);
			}	
			else
			{
				checkIdle = 0;
				antiBan();
			}
			
			// Filling was interrupted, now resuming from last filled item
			if (checkIdle > 3) 
			{
				
				System.out.println("IDLE");
				antiBan();
				if (!fountain.isOnScreen()) 
				{
					Camera.turnTo(fountain);
				}
				while (!(Inventory.getItems()[i].getId() == emptyID)) 
				{
					i++;
					//System.out.println("cycling thru inventory");
					if (Inventory.getItems()[i].getId() == emptyID) 
					{
						System.out.println("match found");
						break;
					}
				}
				
				Inventory.getItems()[i].getWidgetChild().click(true);
				
				if (FOUNTAIN_TILE.equals(FFOUNTAIN_TILE)) 
				{
					fountain.interact("Use", "" + forcefill.substring(forcefill
											.length()) + " -> Waterpump");
				}
				else
				{
					fountain.interact("Use", "" + forcefill.substring(
							forcefill.length()) + " -> Fountain");
				}
					
				Task.sleep(Random.nextInt(100, 200));
					
				
				System.out.println("Clicking Button");
				Mouse.click(clayButton.getAbsoluteX() + Random.nextInt(0, clayButton.getWidth()), 
						clayButton.getAbsoluteY() + Random.nextInt(0, clayButton.getHeight()), true);
				startSoakTime = System.currentTimeMillis();
				Task.sleep(2000);
			}
		}
		
		// Cycling through inventory to count the fulls
		for (int j = 0; j < Inventory.getCount(); j++) {
			if (Inventory.getItems()[j].getId() == fullID) {
				vialCount++;
			}
		}
		
	}

	@Override
	public void messageReceived(MessageEvent m) {
		if(m.getMessage().contains("clay"))
		{
			averageSoakTime = System.currentTimeMillis() - startSoakTime;
			System.out.println("Soak Time: " + averageSoakTime);
			startSoakTime = System.currentTimeMillis();
		}
		
	
		
	}
	public boolean activate() {
		return containsID(Inventory.getItems(), emptyID)
				&& FOUNTAIN.contains(Players.getLocal().getLocation());
	}
}
private class UseItemOnFountain extends Node {
	@Override
	public void execute() {
		int checkIdle = 0, i = 0;
		botState = "FILL_EMPTY";
		System.out.println(botState);
		

		final SceneObject fountain = SceneEntities
				.getNearest(fountainID);

		if (!fountain.isOnScreen()) {
			Camera.turnTo(fountain);
		}
		//Algorithm for finding the last filled item in the event that filling is interrupted
		while (!(Inventory.getItems()[i].getId() == emptyID)) {
			i++;
			
			if (Inventory.getItems()[i].getId() == emptyID) {
				System.out.println("match found");
				break;
			}
		}
		Inventory.getItems()[i].getWidgetChild().click(true);
		Task.sleep(Random.nextInt(100, 200));
		
		if (FOUNTAIN_TILE.equals(FFOUNTAIN_TILE)) 
		{
			fountain.interact("Use", "" + forcefill.substring(forcefill
									.length()) + " -> Waterpump");
		}
		else
		{
			fountain.interact("Use", "" + forcefill.substring(forcefill
								.length()) + " -> Fountain");
		}
		
		while (containsID(Inventory.getItems(), emptyID)) {
			Task.sleep(500);
			antiBan();
			if (Players.getLocal().getAnimation() == -1) {
				checkIdle++;
				antiBan();
				Task.sleep(500);
				if (Players.getLocal().getAnimation() == 832) {
					checkIdle = 0;
					antiBan();
				}

				// Filling was interrupted, now resuming from last filled item
				if (checkIdle > 4) {
					antiBan();
					System.out.println("IDLE");
					while (!(Inventory.getItems()[i].getId() == emptyID)) {
						i++;
						if (Inventory.getItems()[i].getId() == emptyID) {
							System.out.println("match found");
							break;
						}
					}
					
					Inventory.getItems()[i].getWidgetChild().click(true);
					
					if (FOUNTAIN_TILE.equals(FFOUNTAIN_TILE)) 
					{
						fountain.interact("Use", "" + forcefill.substring(forcefill
												.length()) + " -> Waterpump");
					}
					else
					{
						fountain.interact("Use", "" + forcefill.substring(
								forcefill.length()) + " -> Fountain");
					}
					
					Task.sleep(2000);
				}
			}
		}

		// Cycling through inventory to count the fulls
		for (int j = 0; j < Inventory.getCount(); j++) {
			if (Inventory.getItems()[j].getId() == fullID) {
				vialCount++;
			}
		}
	}

	public boolean activate() {
		return containsID(Inventory.getItems(), emptyID)
				&& FOUNTAIN.contains(Players.getLocal().getLocation());
	}

	

}

private class WalkToFountain extends Node {
	@Override
	public void execute() {
		botState = "WALK_TO_FOUNTAIN";
		
		System.out.println(botState);
		Walking.findPath(FOUNTAIN_TILE).traverse();
		antiBan();
		Task.sleep(500);
	}

	public boolean activate() {
		return containsID(Inventory.getItems(), emptyID)
				&& !FOUNTAIN.contains(Players.getLocal().getLocation());
	}

}

//The GUI
@SuppressWarnings("serial")
private class forcefillgui extends JFrame{
	public forcefillgui() {
		initComponents();
	}

	private void button1ActionPerformed(ActionEvent e) 
	{
		String chosen = comboBox1.getSelectedItem().toString(), 
				chosenLoc = comboBox2.getSelectedItem().toString();

        switch (chosen)
        {
            case "Vials":
                emptyID = VIAL;
                fullID = FULL_VIAL;
                forcefill = "Vials";
                FULL_PRICE = FULL_VIAL_PRICE;
                EMPTY_PRICE = EMPTY_VIAL_PRICE;
                break;
            case "Jugs":
                emptyID = JUG;
                fullID = FULL_JUG;
                forcefill = "Jugs";
                FULL_PRICE = FULL_JUG_PRICE;
                EMPTY_PRICE = EMPTY_JUG_PRICE;
                break;
            case "Buckets":
                emptyID = BUCKET;
                fullID = FULL_BUCKET;
                forcefill = "Buckets";
                FULL_PRICE = FULL_BUCKET_PRICE;
                EMPTY_PRICE = EMPTY_BUCKET_PRICE;
                break;
            case "Bowls":
                emptyID = EMPTY_BOWL;
                fullID = FULL_BOWL;
                forcefill = "Bowls";
                FULL_PRICE = FULL_BOWL_PRICE;
                EMPTY_PRICE = EMPTY_BOWL_PRICE;
                break;
            case "Clay":
                clay = true;
                emptyID = CLAY;
                fullID = WET_CLAY;
                forcefill = "Clay";
                FULL_PRICE = WET_CLAY_PRICE;
                EMPTY_PRICE = CLAY_PRICE;
                break;
        }
        switch (chosenLoc)
        {
            case "Varrock (east)":
                BANK_TILE = VBANK_TILE;
                fountainID = VARROCK_FOUNTAIN;
                FOUNTAIN_TILE = VFOUNTAIN_TILE;
                BANK = VBANK;
                FOUNTAIN = VFOUNTAIN;
                break;
            case "GE":
                BANK_TILE = GEBANK_TILE;
                fountainID = GE_FOUNTAIN;
                FOUNTAIN_TILE = GEFOUNTAIN_TILE;
                BANK = GEBANK;
                FOUNTAIN = GEFOUNTAIN;
                break;
            case "Falador":
                BANK_TILE = FBANK_TILE;
                fountainID = FALADOR_FOUNTAIN;
                FOUNTAIN_TILE = FFOUNTAIN_TILE;
                BANK = FBANK;
                FOUNTAIN = FFOUNTAIN;
                break;
        }

		if(clay)
		{
            strategies = new Node[]{new WalkToBank(), new BankItems(), new WalkToFountain(), new UseClayOnFountain()};
        }
		else
		{
            strategies = new Node[]{new WalkToBank(), new BankItems(), new WalkToFountain(), new UseItemOnFountain()};
		}
		setVisible(false);

		dispose();
		startTime = System.currentTimeMillis();
        isStarted = true;
		

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initComponents() 
	{
		label1 = new JLabel();
		label2 = new JLabel();
		comboBox1 = new JComboBox();
		label3 = new JLabel();
		comboBox2 = new JComboBox();
		button1 = new JButton();

		// ======== this ========
		Container contentPane = getContentPane();

		// ---- label1 ----
		label1.setText("Force Fill by Jdog653 and 9Ox");

		// ---- label2 ----
		label2.setText("Force Fill:");

		// ---- comboBox1 ----
		comboBox1.setModel(new DefaultComboBoxModel(new String[] { 
				"Vials",
				"Jugs", 
				"Buckets", 
				"Bowls", 
				"Clay"}));

		// ---- label3 ----
		label3.setText("Location:");

		// ---- comboBox2 ----
		comboBox2.setModel(new DefaultComboBoxModel(new String[] {
				"Varrock (East)", 
				"GE", 
				"Falador" }));

		// ---- button1 ----
		button1.setText("Start force filling");
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button1ActionPerformed(e);
			}
		});

		//AutoGen Gobbly Gook 
		GroupLayout contentPaneLayout = new GroupLayout(contentPane);
		contentPane.setLayout(contentPaneLayout);
		contentPaneLayout
				.setHorizontalGroup(contentPaneLayout
						.createParallelGroup()
						.addGroup(
								contentPaneLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												contentPaneLayout
														.createParallelGroup()
														.addComponent(
																button1,
																GroupLayout.DEFAULT_SIZE,
																207,
																Short.MAX_VALUE)
														.addComponent(label1)
														.addGroup(
																contentPaneLayout
																		.createSequentialGroup()
																		.addComponent(
																				label2)
																		.addPreferredGap(
																				LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				comboBox1,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.DEFAULT_SIZE,
																				GroupLayout.PREFERRED_SIZE))
														.addGroup(
																contentPaneLayout
																		.createSequentialGroup()
																		.addComponent(
																				label3)
																		.addPreferredGap(
																				LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				comboBox2,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.DEFAULT_SIZE,
																				GroupLayout.PREFERRED_SIZE)))
										.addContainerGap()));
		contentPaneLayout
				.setVerticalGroup(contentPaneLayout
						.createParallelGroup()
						.addGroup(
								contentPaneLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(label1)
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												contentPaneLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(label2)
														.addComponent(
																comboBox1,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												contentPaneLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(label3)
														.addComponent(
																comboBox2,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED,
												28, Short.MAX_VALUE)
										.addComponent(button1)));
		pack();
		setLocationRelativeTo(getOwner());

	}

	private JLabel label1;
	private JLabel label2;
	@SuppressWarnings("rawtypes")
	private JComboBox comboBox1;
	private JLabel label3;
	@SuppressWarnings("rawtypes")
	private JComboBox comboBox2;
	private JButton button1;

	}
}
