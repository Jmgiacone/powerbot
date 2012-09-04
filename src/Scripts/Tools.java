package Scripts;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import org.powerbot.game.api.*;
import org.powerbot.game.api.methods.input.Keyboard;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.Identifiable;
import org.powerbot.game.api.wrappers.widget.Widget;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
@Manifest(authors = "Jdog653 and 9Ox", 
	name = "RSBot Tool", 
	description = "Class Tools contains useful methods for developers", 
	version = 0.01)
public class Tools 
{
	/**
	 * WidgetChilds for each of the skill tabs.
	 */
	public static final WidgetChild 
	        SKILLS_TAB = new Widget(548).getChild(105),
			ATTACK = new Widget(320).getChild(1),
			STRENGTH = new Widget(320).getChild(4),
			HIT_POINTS = new Widget(320).getChild(2),
			MINING = new Widget(320).getChild(3),
			AGILITY = new Widget(320).getChild(10),
			SMITHING = new Widget(320).getChild(16),
			DEFENCE = new Widget(320).getChild(17),
			HERBLORE = new Widget(320).getChild(23),
			FISHING = new Widget(320).getChild(29),
			RANGE = new Widget(320).getChild(35),
			THIEVING = new Widget(320).getChild(41),
			COOKING = new Widget(320).getChild(47),
			PRAYER = new Widget(320).getChild(53),
			CRAFTING = new Widget(320).getChild(59),
			FIREMAKING = new Widget(320).getChild(65),
			MAGIC = new Widget(320).getChild(66),
			FLETCHING = new Widget(320).getChild(72),
			WOODCUTTING = new Widget(320).getChild(78),
			RUNECRAFTING = new Widget(320).getChild(79),
			SLAYER = new Widget(320).getChild(85),
			FARMING = new Widget(320).getChild(91),
			CONSTRUCTION = new Widget(320).getChild(97),
			HUNTER = new Widget(320).getChild(103),
			SUMMONING = new Widget(320).getChild(109),
			DUNGEONEERING = new Widget(320).getChild(115),
			
			//Other WidgetChilds
			COMBAT_TAB = new Widget(548).getChild(103),
		    AUTO_RETALIATE_BUTTON = new Widget(884).getChild(13),
		    FRIEND_CHAT_BUTTON = new Widget(548).getChild(59),
			FRIEND_CHAT_JOIN_BUTTON = new Widget(1109).getChild(23),
			FRIEND_CHAT_TEXT_SCREEN = new Widget(752).getChild(4),
			CHECK_BUTTON = new Widget(1109).getChild(20);
	/**
	 * Turns on the Auto - Retaliate button 
	 */
	public void turnOnAutoRetaliate() {
		if (!AUTO_RETALIATE_BUTTON.isOnScreen()) {
			COMBAT_TAB.click(true);
		}
		if (AUTO_RETALIATE_BUTTON.getText().contains("(Off)")) {
			AUTO_RETALIATE_BUTTON.click(true);
		} else {
			System.out.println("AutoRetaliate already on");
			Time.sleep(200);
			Mouse.move(Random.nextInt(0,200),Random.nextInt(0,200));
		}
	}
	
	/**
	 * Joins the friend chat with the given name
	 * @param o The friend chat to join
	 */
	public void joinFriendChat(String o) {
		if (!FRIEND_CHAT_JOIN_BUTTON.isOnScreen()) {
			System.out.println("clicked friendchat tab successfully");
			FRIEND_CHAT_BUTTON.click(true);
		} else {
			if (!FRIEND_CHAT_TEXT_SCREEN.isOnScreen()) {
				System.out.println("clicked join friendchat button successfully");
				FRIEND_CHAT_JOIN_BUTTON.click(true);
			} else {
				Time.sleep(1000);
			}
		}
		Time.sleep(2000);
		if (FRIEND_CHAT_TEXT_SCREEN.isOnScreen()) {
			System.out.println("typed desired friendchat name successfully");
			Keyboard.sendText(o, true);
		}
		Mouse.move(100,22);
		Time.sleep(500);
		if (CHECK_BUTTON.isOnScreen()) {
			System.out.println("You are in your desired friendchat!");
		}
	}
	
	/**
	 * Turns off the Auto - Retaliate button 
	 */
	public void turnOffAutoRetaliate() {
		if (!AUTO_RETALIATE_BUTTON.isOnScreen()) {
			COMBAT_TAB.click(true);
		}
		if (AUTO_RETALIATE_BUTTON.getText().contains("(On)")) {
			AUTO_RETALIATE_BUTTON.click(true);
		} else {
			System.out.println("AutoRetaliate already off");
			Time.sleep(200);
			Mouse.move(Random.nextInt(0,200),Random.nextInt(0,200));
		}
	}		
	
	/**
	 * Moves the Mouse to a random point on the screen
	 */
	public void moveMouseRandomly() 
	{
		Mouse.move(Random.nextInt(0,500), Random.nextInt(0,500));
	}
	
	public void rightClickMouse() {
		moveMouseRandomly();
		Time.sleep(Random.nextInt(0, 500));
		Mouse.click(false);
		Time.sleep(Random.nextInt(0, 500));
		moveMouseRandomly();
	}
	
	//Widget 320 [child 0-150] = hover mouse over skills
	/**
	 * Directs the Mouse to click on the skills tab and hover over the selected skill. (Tab constants are located in Tools)
	 * @param o The Skill to be checked (Tab constants are located in Tools)
	 */
	public void checkSkill(WidgetChild o) 
	{
		SKILLS_TAB.click(true);
		o.hover();
		Time.sleep(Random.nextInt(1000, 4000));
		Mouse.move(Random.nextInt(0, 200) + 1,
				Random.nextInt(0, 200) + 1);
	}
	
	/**
	 * Checks the price of the item with the given ID via this website: http://services.runescape.com/m=itemdb_rs/
	 * @param id The item ID of the wanted item.
	 * @return The price
	 * @throws IOException If it's dumb
	 */
	public static int getPriceOfItem(int id) throws IOException 
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
	public static boolean containsID(Identifiable[] x, int id) 
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
	 * Returns an image from the given URL	
	 * @param url The web address of the raw image
	 * @return The image at the given URL
	 */
	public static Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {
			return null;
		}
	}
	/**
	 * Either randomly moves the mouse, or opens the inventory to prevent Jagex from realizing that you're botting
	 */
	public static void antiBan() {
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
}
