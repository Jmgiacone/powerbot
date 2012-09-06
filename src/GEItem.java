import com.sun.org.glassfish.gmbal.Description;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class GEItem {
	private final int ID;
    private int price;
	private final String NAME, DESCRIPTION, TYPE;
    private String imageSmall, imageBig;
	private final boolean MEMBERS_ONLY;

    /**
     * Constructs a new GEItem
     * @param id The ID of the item
     * @param name Its' name
     * @param desc Item's description
     * @param type The Type of item it is
     * @param imageSmall A small icon
     * @param imageBig A large icon
     * @param membersOnly If the item is members only
     */
    public GEItem(int id, int price, String name, String desc, String type, String imageSmall, String imageBig, boolean membersOnly)
    {
        ID = id;
        this.price = price;
        NAME = name;
        DESCRIPTION = desc;
        TYPE = type;
        this.imageSmall = imageSmall;
        this.imageBig = imageBig;

        MEMBERS_ONLY = membersOnly;
    }
	/*
	 * Returns the integer value of a string in RS price notation (e.g. 171.7k -> 171700)
	 */
	public static int priceToInt(String price) {
		return Integer.parseInt(price.toUpperCase().replaceAll("\\.([0-9])", "$1").replace("K", "00").replace("M", "00000").replace("B", "00000000").replaceAll(",", "").trim());
	}
		
	/*
	 * Returns string with RS price notation, when given an integer (e.g. 171700 -> 171.7k)
	 */
	public static String priceToString(int normalCoins) {
		return priceToString(Integer.toString(normalCoins));
	}
	
	/*
	 * Returns string with RS price notation, when given an integer (e.g. 171700 -> 171.7k)
	 */
	public static String priceToString(String normalCoins) {
		if(normalCoins.length() < 6)
			return normalCoins;
		
		return normalCoins.replaceAll("([0-9])[0-9]{" + ((normalCoins.length() < 8) ? 2 : ((normalCoins.length() < 10) ? 5 : 8)) + "}$", ".$1" + ((normalCoins.length() < 8) ? "K" : ((normalCoins.length() < 10) ? "M" : "B")));
	}
	
	public Image downloadImageSmall() {
		try {
			return ImageIO.read(new URL(this.imageSmall));
		} catch (IOException e) {
			return null;
		}
	}
	
	public Image downloadImageBig() {
		try {
			return ImageIO.read(new URL(this.imageBig));
		} catch (IOException e) {
			return null;
		}
	}
	
	public final int getID() {
		return ID;
	}

    public String getName() {
		return NAME;
	}
	
	public String getDescription() {
		return DESCRIPTION;
	}

    public int getPrice() {
		return price;
	}
	
	public String getImageSmall() {
		return imageSmall;
	}

	public void setImageSmall(String imageSmall) {
		imageSmall = imageSmall;
	}

	public String getImageBig() {
		return imageBig;
	}

	public void setImageBig(String imageBig) {
		this.imageBig = imageBig;
	}

	public String getType() {
		return TYPE;
	}

	public boolean isMembersOnly() {
		return MEMBERS_ONLY;
	}
}