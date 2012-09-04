package Scripts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import org.powerbot.concurrent.strategy.Strategy;
import org.powerbot.game.api.ActiveScript;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.tab.Skills;
import org.powerbot.game.api.methods.widget.Bank;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.Area;
import org.powerbot.game.api.wrappers.Identifiable;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.map.TilePath;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.bot.event.MessageEvent;
import org.powerbot.game.bot.event.listener.MessageListener;
import org.powerbot.game.bot.event.listener.PaintListener;

@Manifest(name = "Force Flour", 
authors = "Jdog653", 
version = 0.01, 
description = "Gathers wheat and fills pots with Flour for profit")
public class ForceFlour extends ActiveScript implements PaintListener, MouseListener//, MouseMotionListener
{

	private final Tile 
			GE_BANK_TILE = new Tile(3150, 3476, 0),
			DRAYNOR_BANK_TILE = new Tile(3093, 3243, 0),
			
			GUILD_FIELD_ENTRANCE = new Tile(3142, 3456, 0),
			CHEFS_GUILD_ENTRANCE = new Tile(3143, 3443, 0),
			DRAYNOR_MILL_ENTRANCE = new Tile(3167, 3301, 0),
			DRAYNOR_FIELD_ENTRANCE = new Tile(3165, 3294, 0),
			
			DRAYNOR_FIELD_MIDDLE = new Tile(3161, 3295, 0),
			DRAYNOR_MILL_MID = new Tile(3167, 3305, 0),
			CHEFS_GUILD_MIDDLE = new Tile(3143, 3446, 0),
			CHEF_FIELD_MIDDLE = new Tile(3141, 3460, 0);
	
	private final Area DRAYNOR_WHEAT_FIELD = new Area(new Tile[]{
			new Tile(3164, 3298, 0),
			new Tile(3156, 3307, 0),
			new Tile(3155, 3307, 0),
			new Tile(3135, 3305, 0),
			new Tile(3153, 3297, 0),
			new Tile(3155, 3295, 0),
			new Tile(3157, 3295, 0),
			new Tile(3162, 3290, 0)}),
			
			DRAYNOR_BANK = new Area(new Tile(3096, 3246, 0), new Tile(3092, 3240, 0)),
			GE_BANK = new Area(new Tile(3155, 3479, 0), new Tile(3142, 3472, 0)),
			
			CHEFS_GUILD_FIELD = new Area(new Tile[] {
					new Tile(3139, 3458, 0),
					new Tile(3138, 3461, 0),
					new Tile(3138, 3463, 0),
					new Tile(3140, 3463, 0),
					new Tile(3141, 3464, 0),
					new Tile(3144, 3464, 0),
					new Tile(3144, 3461, 0),
					new Tile(3143, 3460, 0),
					new Tile(3143, 3458, 0)});
	
	private final Area[] 
			DRAYNOR_MILL = {
				new Area(new Tile(3165, 3308, 0), new Tile(3169, 3305, 0)), 
				new Area(new Tile(3165, 3308, 1), new Tile(3169, 3305, 1)),
				new Area(new Tile(3165, 3308, 2), new Tile(3169, 3305, 2))}, 
				
			CHEFS_GUILD_MILL = {
			new Area(new Tile[] {
					new Tile(3144, 3444, 0),
					new Tile(3142, 3444, 0), 
					new Tile(3138, 3448, 0),
					new Tile(3139, 3451, 0),
					new Tile(3140, 3453, 0),
					new Tile(3147, 3454, 0),
					new Tile(3149, 3452, 0),
					new Tile(3149, 3447, 0),
					new Tile(3146, 3449, 0),
					new Tile(3146, 3446, 0)}),
			new Area(new Tile[] {
					new Tile(3144, 3444, 1),
					new Tile(3142, 3444, 1), 
					new Tile(3138, 3448, 1),
					new Tile(3139, 3451, 1),
					new Tile(3140, 3453, 1),
					new Tile(3147, 3454, 1),
					new Tile(3149, 3452, 1),
					new Tile(3149, 3447, 1),
					new Tile(3146, 3449, 1),
					new Tile(3146, 3446, 1)}),
			new Area(new Tile[] {
					new Tile(3144, 3444, 2),
					new Tile(3142, 3444, 2), 
					new Tile(3138, 3448, 2),
					new Tile(3139, 3451, 2),
					new Tile(3140, 3453, 2),
					new Tile(3147, 3454, 2),
					new Tile(3149, 3452, 2),
					new Tile(3149, 3447, 2),
					new Tile(3146, 3449, 2),
					new Tile(3146, 3446, 2)})};
	
	private String botState;
	private final int EMPTY_POT_ID = 1931, POT_OF_FLOUR_ID = 1933, WHEAT_ID = 1947, CHEFS_HAT_ID = 1949; 
	private long startTime;
	private int checkIdle, fullPrice = 0, emptyPrice = 0, refreshPrices = 0, count = 0;
	private final TilePath 
	
	FIELD_TO_GUILD = new TilePath(new Tile[] {
			new Tile(3136, 3452, 0),
			CHEFS_GUILD_ENTRANCE,
			CHEFS_GUILD_MIDDLE}),
	GUILD_TO_GE = new TilePath(new Tile[] {
			new Tile(3153, 3455, 0),
			new Tile(3162, 3464, 0),
			new Tile(3153, 3473, 0),
			GE_BANK_TILE}), 
	GE_TO_FIELD = new TilePath(new Tile[] {
			new Tile(3149, 3470, 0),
			new Tile(3158, 3469, 0),
			new Tile(3158, 3461, 0),
			new Tile(3148, 3459, 0),
			GUILD_FIELD_ENTRANCE,
			CHEF_FIELD_MIDDLE}),
			
	//Draynor TilePaths		
	DRAYNOR_MILL_TO_BANK = new TilePath(new Tile[] {
			DRAYNOR_MILL_MID,
			new Tile(3166, 3289, 0),
			new Tile(3160, 3276, 0),
			new Tile(3151, 3265, 0),
			new Tile(3135, 3264, 0),
			new Tile(3118, 3263, 0),
			new Tile(3108, 3252, 0),
			new Tile(3092, 3250, 0),
			DRAYNOR_BANK_TILE}),
			
	//Look this over		
	DRAYNOR_BANK_TO_FIELD = new TilePath(new Tile[] {
			DRAYNOR_BANK_TILE,
			new Tile(3103, 3256, 0),
			new Tile(3116, 3262, 0),
			new Tile(3131, 3265, 0),
			new Tile(3146, 3264, 0),
			new Tile(3159, 3270, 0),
			new Tile(3161, 3286, 0),
			new Tile(3166, 3294, 0),
			DRAYNOR_FIELD_ENTRANCE,
			DRAYNOR_FIELD_MIDDLE}),
	
	DRAYNOR_FIELD_TO_MILL  = new TilePath(new Tile[] {
			DRAYNOR_FIELD_MIDDLE,
			DRAYNOR_FIELD_ENTRANCE,
			DRAYNOR_MILL_ENTRANCE,
			DRAYNOR_MILL_MID});
	
	
	private final int[] 
			WHEAT_IDS= {15507, 15506, 15508}, 
			CLOSED_GATE_IDS = {45210, 45212, 15510, 15512}, 
			LADDER_IDS = {36795, 36796, 36797, 24073, 24074, 24075}, 
			CLOSED_DOORS_IDS = {45964, 45966, 2712},
			HOPPER_IDS = {70034, 24071}, 
			LEVER_IDS = {2718, 24072}, 
			BIN_IDS = {36880, 954};
	private boolean chefGuild = false, showHide = true, putInWheat = true, useLevers = false, pickWheat;
	
	Color c = new Color(65, 105, 225, 70);
	private final Image img1 = getImage("http://i.imgur.com/o9EAB.png");
	private final Color color1 = new Color(51, 102, 255);
	private final Color color2 = new Color(0, 0, 0);
	private final BasicStroke stroke1 = new BasicStroke(1);
	private final Font font1 = new Font("Arial", 0, 9);

	private TilePath 
		FIELD_TO_MILL, 
		MILL_TO_BANK, 
		BANK_TO_FIELD;
	
	@SuppressWarnings("unused")
	private Tile 
		FIELD_ENTRANCE, 
		FIELD_MID,
		MILL_MID,
		MILL_ENTRANCE,
		BANK_TILE;
	
	private Area 
		BANK, 
		WHEAT_FIELD;
	
	private Area[] MILL_FLOORS;

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

				Time.sleep(200, 600);

				Mouse.move(Random.nextInt(0, 500) + 1,
						Random.nextInt(0, 500) + 1);
			}
			break;
		case 2:
			if (Random.nextInt(0, 13) == 2) {
				Camera.setAngle(Random.nextInt(0, 360) + 1);
				Time.sleep(400, 1200);
			}
			break;
		case 3:
			if (Random.nextInt(0, 24) == 6) {
				offScreen();

				Time.sleep(Random.nextInt(600, Random.nextInt(1200, 2000)));
			}
			break;
		default:
			break;
		}
	}
	private boolean containsOnly(Identifiable[] x, int id)
	{
		int count = 0;
		for(Identifiable y : x)
		{
			if(y.getId() == id)
			{
				count++;
			}
		}
		
		return count == x.length;
	}
	private String insertComma(int x)
    {
        String s = x + "";
        
        if(s.length() <= 3)
        {
            return s;
        }
        
        return insertComma(x / 1000) + "," + s.substring(s.length() - 3);
        
    }
	
	private void drawMouse(Graphics g1, final Color color) 
	{
		int mouseY = (int) Mouse.getLocation().getY();
		int mouseX = (int) Mouse.getLocation().getX();
		g1.drawOval(mouseX - 7, mouseY - 7, 14, 14);
		g1.setColor(c);
		g1.fillOval(mouseX - 7, mouseY - 7, 14, 14);
		g1.setColor(new Color(245, 255, 250, 140));
		g1.fillOval(mouseX - 1, mouseY - 1, 3, 2);
	}
	
	private Image getImage(String url) 
	{
	    Image im = null;
	    int i = 0;
	    
	    while(im == null && i < 50)
	    {
			try 
			{
				im = ImageIO.read(new URL(url));
			} 
			catch (MalformedURLException e) 
			{
				System.out.println("Try #" + (i + 1));
			}
			catch(IOException e)
			{
				System.out.println("Try " + (i + 1));
			}
			i++;
	    }
	    
	    return im;
	}
	private int getPriceOfItem(int id) throws IOException 
	{
		String price;
		URL url = new URL("http://services.runescape.com/m=itemdb_rs/viewitem.ws?obj=" + id);
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) 
		{
			if (line.contains("<td>")) 
			{
				price = line.substring(line.indexOf(">") + 1,
						line.indexOf("/") - 1);
				price = price.replace(",", "");
				try 
				{
					return Integer.parseInt(price);
				} 
				catch (NumberFormatException e) 
				{
					return 0;
				}
			}
		}
		return -1;
	}
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

	private boolean containsID(int[] x, int id) 
	{
		for (int i : x) 
		{
			if (i == id) 
			{
				return true;
			}
		}

		return false;
	}
	
	private int getFirstInventoryLocation(Identifiable[] y, int id)
	{
		for(int i = 0; i < y.length; i++)
		{
			if(y[i].getId() == id)
			{
				return i;
			}
		}
		
		return -1;
	}
	
	private int getAmountOfId(Identifiable[] x, int id)
	{
		int count = 0;
		for(Identifiable y : x)
		{
			if(y.getId() == id)
			{
				count++;
			}
		}
		
		return count;
	}
	@Override
	protected void setup() 
	{
		if(Skills.getRealLevel(Skills.COOKING) < 30)
		{
			chefGuild = false;
			
			MILL_MID = DRAYNOR_MILL_MID;
			FIELD_TO_MILL = DRAYNOR_FIELD_TO_MILL; 
			MILL_TO_BANK = DRAYNOR_MILL_TO_BANK; 
			BANK_TO_FIELD = DRAYNOR_BANK_TO_FIELD;
			
			FIELD_ENTRANCE = DRAYNOR_FIELD_ENTRANCE; 
			FIELD_MID = DRAYNOR_FIELD_MIDDLE; 
			BANK = DRAYNOR_BANK;
			WHEAT_FIELD = DRAYNOR_WHEAT_FIELD;
			MILL_FLOORS = DRAYNOR_MILL;
		}
		else if(containsID(Inventory.getItems(), CHEFS_HAT_ID) || containsID(Players.getLocal().getAppearance(), CHEFS_HAT_ID))
		{
			chefGuild = true;
			
			Inventory.getItems()[getFirstInventoryLocation(Inventory.getItems(), CHEFS_HAT_ID)].getWidgetChild().click(true);

			MILL_MID = CHEFS_GUILD_MIDDLE;
			FIELD_TO_MILL = FIELD_TO_GUILD; 
			MILL_TO_BANK = GUILD_TO_GE; 
			BANK_TO_FIELD = GE_TO_FIELD;
			
			FIELD_ENTRANCE = GUILD_FIELD_ENTRANCE; 
			FIELD_MID = CHEF_FIELD_MIDDLE; 
			BANK = GE_BANK;
			WHEAT_FIELD = CHEFS_GUILD_FIELD;
			MILL_FLOORS = CHEFS_GUILD_MILL;
		}
		
		try
		{
			emptyPrice = getPriceOfItem(EMPTY_POT_ID);
			fullPrice = getPriceOfItem(POT_OF_FLOUR_ID);
		}
		catch(Exception e)
		{
			System.out.println("Failed to fetch prices");
		}
		
		startTime = System.currentTimeMillis();
		
		provide(new PickWheat());
		provide(new GoToTop());
		provide(new WalkToMill());
		provide(new GrindWheat());
		provide(new GoDown());
		provide(new FillPots());
		provide(new OpenGate());
		provide(new goToWheat());
		provide(new GoToBank());
		provide(new BankItems());
		provide(new OpenDoor());
	}

	private class BankItems extends Strategy implements Runnable 
	{
		@Override
		public void run() 
		{
			botState = "BANK_ITEMS";
			
			//Open the bank
			Bank.open();
			Time.sleep(500);
			if (Bank.isOpen()) 
			{
				Bank.depositInventory();
				if (Bank.getItem(EMPTY_POT_ID) == null) 
				{
					System.out.println("Finished, no Pots left");
					Game.logout(false);
					stop();
				} 
				else 
				{
					Bank.withdraw(EMPTY_POT_ID, 14);
				}
			}
		}

		public boolean validate() 
		{
			return BANK.contains(Players.getLocal().getLocation()) &&
					!containsOnly(Inventory.getItems(), EMPTY_POT_ID);
		}
	}
	private class goToWheat extends Strategy implements Runnable
	{

		@Override
		public void run() 
		{
			System.out.println("Walking to the wheat field");

			botState = "WALK_TO_FIELD";
			BANK_TO_FIELD.traverse();
			
		}
		
		public boolean validate()
		{
			return getAmountOfId(Inventory.getItems(), EMPTY_POT_ID) > 0 &&
						(!WHEAT_FIELD.contains(Players.getLocal().getLocation()) || Players.getLocal().getLocation().equals(FIELD_MID)) &&
						!MILL_FLOORS[2].contains(Players.getLocal().getLocation()) &&
						getAmountOfId(Inventory.getItems(), POT_OF_FLOUR_ID) == 0 &&
						getAmountOfId(Inventory.getItems(), WHEAT_ID) == 0 &&
					    !MILL_FLOORS[0].contains(Players.getLocal().getLocation());
		}
	}
	private class GoToBank extends Strategy implements Runnable
	{

		@Override
		public void run() 
		{
			System.out.println("Walking to the bank");

			botState = "WALK_TO_BANK";
			MILL_TO_BANK.traverse();
		}
		
		public boolean validate()
		{
			return (Players.getLocal().getLocation().equals(BANK_TILE) || !BANK.contains(Players.getLocal().getLocation())) &&
					getAmountOfId(Inventory.getItems(), POT_OF_FLOUR_ID) > 0 &&
					getAmountOfId(Inventory.getItems(), EMPTY_POT_ID) == 0 &&
					getAmountOfId(Inventory.getItems(), WHEAT_ID) == 0;
		}
		
	}
	private class GoToTop extends Strategy implements Runnable
	{

		@Override
		public void run() 
		{
			botState = "GO_UP";
			System.out.println("GO_TO_TOP");
			
			SceneEntities.getNearest(LADDER_IDS).interact("Climb-up");
			
		}
		
		public boolean validate()
		{
			return getAmountOfId(Inventory.getItems(), WHEAT_ID) > 0 && 
					Players.getLocal().getPlane() != 2 && 
					(MILL_FLOORS[0].contains(Players.getLocal().getLocation()) || MILL_FLOORS[1].contains(Players.getLocal().getLocation()));
		}
		
	}
	private class FillPots extends Strategy implements Runnable
	{

		@Override
		public void run() 
		{
			botState = "FILL_POTS";
			SceneObject bin = SceneEntities.getNearest(BIN_IDS);
			
			System.out.println("FIll the pots!");
			
			bin.interact("Take-Flour");
			while (getAmountOfId(Inventory.getItems(), EMPTY_POT_ID) > 0) 
			{
				Time.sleep(500);
				antiBan();
				if (Players.getLocal().getAnimation() == -1) 
				{
					checkIdle++;
					antiBan();
					Time.sleep(500);
					if (Players.getLocal().getAnimation() == 1734) 
					{
						checkIdle = 0;
						System.out.println("Filling");
						antiBan();
					}

					if (checkIdle > 10) 
					{
						System.out.println("Too much idle");
						antiBan();

						bin.interact("Take-Flour");
					
						Time.sleep(2000);
					}
				}
		    }
			
			for(Identifiable i : Inventory.getItems())
			{
				if(i.getId() == POT_OF_FLOUR_ID)
				{
					count++;
				}
			}
		}
		
		public boolean validate()
		{
			return MILL_FLOORS[0].contains(Players.getLocal().getLocation()) &&
					getAmountOfId(Inventory.getItems(), WHEAT_ID) == 0 &&
					getAmountOfId(Inventory.getItems(), EMPTY_POT_ID) > 0;
		}
		
	}
	private class GoDown extends Strategy implements Runnable
	{

		@Override
		public void run() 
		{
			botState = "GO_DOWN";

			SceneObject ladder = SceneEntities.getNearest(LADDER_IDS);
			
			if(!ladder.isOnScreen())
			{
				Camera.turnTo(ladder);
			}
			
			ladder.interact("Climb-down");
		}
		
		public boolean validate()
		{
			return getAmountOfId(Inventory.getItems(), WHEAT_ID) == 0 &&
					(MILL_FLOORS[2].contains(Players.getLocal().getLocation()) || MILL_FLOORS[1].contains(Players.getLocal().getLocation())) && 
					getAmountOfId(Inventory.getItems(), EMPTY_POT_ID) > 0;
		}
		
	}
	private class GrindWheat extends Strategy implements Runnable, MessageListener
	{

		@Override
		public void run() 
		{
			botState = "GRIND_WHEAT";
			
			
			SceneObject hopper = SceneEntities.getNearest(HOPPER_IDS), levers = SceneEntities.getNearest(LEVER_IDS);
			if(!hopper.isOnScreen())
			{
				Camera.turnTo(hopper);
			}
			
			if(chefGuild)
			{
				Walking.walk(new Tile(3141, 3452, 0));
			}

			if(putInWheat)
			{
				System.out.println("Selecting Wheat");
				Inventory.getItems()[getFirstInventoryLocation(Inventory.getItems(), WHEAT_ID)].getWidgetChild().click(true);
				System.out.println("Using wheat on hopper");
				hopper.interact("Use", "Wheat -> Hopper");
			}
			//Time.sleep(3500);
			
			if(useLevers)
			{
			System.out.println("Operating Levers");
			levers.interact("Operate");
			}
			//Time.sleep(3500);
			
		}
		
		public boolean validate()
		{
			return MILL_FLOORS[2].contains(Players.getLocal().getLocation()) && 
					getAmountOfId(Inventory.getItems(), WHEAT_ID) > 0 &&
					getAmountOfId(Inventory.getItems(), EMPTY_POT_ID) > 0;
		}

		@Override
		public void messageReceived(MessageEvent e) 
		{
			if(e.getMessage().equalsIgnoreCase("You put the wheat in the hopper.") || 
					e.getMessage().equalsIgnoreCase("There is already wheat in the hopper."))
			{
				useLevers = true;
				putInWheat = false;
			}
			else if(e.getMessage().equalsIgnoreCase("You operate the hopper. The wheat slides down the chute.") || 
					e.getMessage().equalsIgnoreCase("You operate the empty hopper. Nothing interesting happens."))
			{
				useLevers = false;
				putInWheat = true;
			}
			
		}
		
	}
	private class WalkToMill extends Strategy implements Runnable
	{

		@Override
		public void run() 
		{
			botState = "WALK_TO_MILL";
			
			System.out.println("Walking to the Mill");
			FIELD_TO_MILL.traverse();
			
		}
		
		public boolean validate()
		{
			return (Players.getLocal().getLocation().equals(MILL_MID) || !MILL_FLOORS[0].contains(Players.getLocal().getLocation())) && 
					getAmountOfId(Inventory.getItems(), WHEAT_ID) > 0 && 
					Players.getLocal().getPlane() == 0 &&
					getAmountOfId(Inventory.getItems(), EMPTY_POT_ID) > 0 &&
					getAmountOfId(Inventory.getItems(), WHEAT_ID) == getAmountOfId(Inventory.getItems(), EMPTY_POT_ID);
		}
	}
	private class OpenDoor extends Strategy implements Runnable
	{

		@Override
		public void run() 
		{
			SceneObject doors = SceneEntities.getNearest(CLOSED_DOORS_IDS);
			
			botState = "OPEN_DOOR";
			if(!doors.isOnScreen())
			{
				Camera.turnTo(doors);
			}
			
			System.out.println("Opening the doors");
			doors.interact("Open");
		}
		
		public boolean validate()
		{
			if(SceneEntities.getNearest(CLOSED_DOORS_IDS) == null)
			{
				return false;
			}
			
			if(!chefGuild)
			{
				if(MILL_FLOORS[0].contains(Players.getLocal().getLocation()))
				{
					return getAmountOfId(Inventory.getItems(), WHEAT_ID) == 0 && 
							getAmountOfId(Inventory.getItems(), EMPTY_POT_ID) == 0 &&
							getAmountOfId(Inventory.getItems(), POT_OF_FLOUR_ID) > 0 &&
							Calculations.distanceTo(DRAYNOR_MILL_ENTRANCE) > Calculations.distanceTo(SceneEntities.getNearest(CLOSED_DOORS_IDS));
				}
			
				//Make a constant for the middle of both mills.
				return getAmountOfId(Inventory.getItems(), WHEAT_ID) > 0 && 
						getAmountOfId(Inventory.getItems(), EMPTY_POT_ID) > 0 &&
						getAmountOfId(Inventory.getItems(), WHEAT_ID) == getAmountOfId(Inventory.getItems(), EMPTY_POT_ID) &&
						Calculations.distanceTo(DRAYNOR_MILL_MID) > Calculations.distanceTo(SceneEntities.getNearest(CLOSED_DOORS_IDS));
			}
			
			//In the Chef Guild, you want out
			if(MILL_FLOORS[0].contains(Players.getLocal().getLocation()))
			{
				return getAmountOfId(Inventory.getItems(), WHEAT_ID) == 0 && 
						getAmountOfId(Inventory.getItems(), EMPTY_POT_ID) == 0 &&
						getAmountOfId(Inventory.getItems(), POT_OF_FLOUR_ID) > 0;
			}
			
			return getAmountOfId(Inventory.getItems(), WHEAT_ID) > 0 && 
					getAmountOfId(Inventory.getItems(), EMPTY_POT_ID) > 0 &&
					getAmountOfId(Inventory.getItems(), WHEAT_ID) == getAmountOfId(Inventory.getItems(), EMPTY_POT_ID) &&
					Players.getLocal().getLocation().equals(CHEFS_GUILD_ENTRANCE);
		}
	 }
	private class OpenGate extends Strategy implements Runnable
	{

		@Override
		public void run() 
		{
			SceneObject gate = SceneEntities.getNearest(CLOSED_GATE_IDS);
			
			botState = "OPEN_GATE";
			if(!gate.isOnScreen())
			{
				Camera.turnTo(gate);
			}
			
			System.out.println("Opening the gate");
			gate.interact("Open");
			Time.sleep(1500);
			
			
			
		}
		
		public boolean validate()
		{
			if(SceneEntities.getNearest(CLOSED_GATE_IDS) == null)
			{
				return false;
			}
			
			if(WHEAT_FIELD.contains(Players.getLocal().getLocation()))
			{
				return Calculations.distanceTo(FIELD_ENTRANCE) > Calculations.distanceTo(SceneEntities.getNearest(CLOSED_GATE_IDS)) &&
						getAmountOfId(Inventory.getItems(), EMPTY_POT_ID) > 0 &&
						getAmountOfId(Inventory.getItems(), WHEAT_ID) == getAmountOfId(Inventory.getItems(), EMPTY_POT_ID) &&
						getAmountOfId(Inventory.getItems(), POT_OF_FLOUR_ID) == 0;
			}
			
			return Calculations.distanceTo(FIELD_MID) > Calculations.distanceTo(SceneEntities.getNearest(CLOSED_GATE_IDS)) &&
					getAmountOfId(Inventory.getItems(), EMPTY_POT_ID) > 0 &&
					getAmountOfId(Inventory.getItems(), WHEAT_ID) == 0 &&
					getAmountOfId(Inventory.getItems(), POT_OF_FLOUR_ID) == 0;
		}
		
	}
	private class PickWheat extends Strategy implements Runnable, MessageListener
	{

		@Override
		public void run() 
		{
			//You pick some wheat.
			botState = "PICK_WHEAT";
			if(pickWheat)
			{
				System.out.println("Picking wheat");
				SceneEntities.getNearest(WHEAT_IDS).interact("Pick");
				pickWheat = false;
			}
		}
		
		
		public boolean validate()
		{
			return WHEAT_FIELD.contains(Players.getLocal().getLocation()) 
					&& getAmountOfId(Inventory.getItems(), WHEAT_ID) < getAmountOfId(Inventory.getItems(), EMPTY_POT_ID)
					&& getAmountOfId(Inventory.getItems(), EMPTY_POT_ID) > 0 &&
					getAmountOfId(Inventory.getItems(), POT_OF_FLOUR_ID) == 0;
		}


		@Override
		public void messageReceived(MessageEvent e) 
		{
			if(e.getMessage().equalsIgnoreCase("You pick some wheat."))
			{
				pickWheat = true;
			}
		}
		
	}
	
	@Override
	public void mouseClicked(MouseEvent e) 
	{
		int x = e.getX();
		int y = e.getY();

		if (x >= 6 && x < 6 + 85 && y >= 509 && y < 509 + 15) {
			if (showHide) {
				showHide = false;
			} else {
				showHide = true;
			}
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
	@Override
	public void onRepaint(Graphics g1) 
	{
		Graphics2D g = (Graphics2D) g1;
		long millis = System.currentTimeMillis() - startTime;
		long hours = millis / (1000 * 60 * 60);
		millis -= hours * (1000 * 60 * 60);
		long minutes = millis / (1000 * 60);
		millis -= minutes * (1000 * 60);
		long seconds = millis / 1000;

		g.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
		
		//Pulls prices from online every 10,000 repaints
		if(refreshPrices >= 10000)
		{
			try
			{
				System.out.println("Fetching Prices");
				emptyPrice = getPriceOfItem(EMPTY_POT_ID);
				fullPrice = getPriceOfItem(POT_OF_FLOUR_ID);
			}
			catch(IOException e)
			{
				//NaBrO (Sodium HypoBromide)
			}
			refreshPrices = 0;
		}
		if (showHide) 
		{
			g1.drawImage(img1, 0, 388, null);
			g1.setColor(Color.BLACK);
			g1.drawString(hours + ":" + minutes + ":" + seconds, 200, 471);
			g1.drawString("" + botState, 10, 408);
			g1.drawString(insertComma(count), 415, 487);
			g1.drawString("" + (fullPrice - emptyPrice), 200, 502);
			g1.drawString(insertComma((fullPrice - emptyPrice) * count), 415, 471);
			//Profit per hour = (profit * 3600000d) / timeRan
			g1.drawString(insertComma((int)((((fullPrice - emptyPrice) * count) * 3600000d) / (System.currentTimeMillis() - startTime))), 415,502);
		}
		g1.setColor(color1);
		g1.fillRect(6, 509, 85, 15);
		g1.setColor(color2);
		((Graphics2D) g1).setStroke(stroke1);
		g1.drawRect(6, 509, 85, 15);
		drawMouse(g1, c);
		g.setFont(font1);
		g.drawString("Hide/Show paint", 15, 520);
		
		refreshPrices++;
	}
}


