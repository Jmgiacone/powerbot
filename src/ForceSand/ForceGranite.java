package ForceSand;

import org.powerbot.concurrent.strategy.Strategy;
import org.powerbot.game.api.ActiveScript;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.widget.Bank;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.bot.event.listener.PaintListener;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

@Manifest(name = "ForceGranite",
        description = "Splits Granite blocks down to their smallest for wicked gold",
        version = 0.01,
        authors = "Jdog653")
public class ForceGranite extends ActiveScript implements PaintListener
{
    private final int FIVE_KG_GRANITE_ID = 6983,
            TWO_KG_GRANITE_ID = 6981,
            FIVE_HUNDRED_G_GRANITE_ID = 6979,
            CHISELING_ANIMATION = 11146;


    private long startTime = System.currentTimeMillis();
    private final Image mouse = getImage("http://www.rw-designer.com/cursor-view/17047.png");
    private int graniteCount = 0, fiveKgGranitePrice, fiveHundredGGranitePrice;

    private int getFirstInInventory(final int id)
    {
        for(int i = 0; i < Inventory.getItems().length; i++)
        {
            if(Inventory.getItems()[i].getId() == id)
            {
                return i;
            }
        }

        return -1;
    }
    @Override
    protected void setup()
    {
        fiveHundredGGranitePrice = getPrice(FIVE_HUNDRED_G_GRANITE_ID);
        fiveKgGranitePrice = getPrice(FIVE_KG_GRANITE_ID);

        System.out.println("5Kg: " + fiveKgGranitePrice +
                "\n500g: "+ fiveHundredGGranitePrice +
                "\nProfit: " + ((10 * fiveHundredGGranitePrice) - fiveKgGranitePrice));
        provide(new CrushFiveKGGranite());
        provide(new CrushTwoKGGranite());
        provide(new BankItems());
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
                    new InputStreamReader(new URL("http://open.tip.it/json/ge_single_item?item="
                                    .concat(Integer.toString(id))).
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
    private void crushBlock(final int id)
    {
        //5kg -> 2kg: 136314880
        //2Kg -> 500g: 272629760
        int idle = 10001;
        while(Inventory.getCount(id) > 0)
        {
            if(Players.getLocal().getAnimation() == -1)
            {
                idle++;
            }
            else if(Players.getLocal().getAnimation() == CHISELING_ANIMATION)
            {
                idle = 0;
                System.out.println("Chipping");
                Time.sleep(500, 550);
            }

            if(idle > 100000)
            {
                if(idle == 10001)
                {
                    System.out.println("First run, not idling");
                }
                else
                {
                    System.out.println("IDLE:"+idle+"\n5 Kg: "
                            +Inventory.getCount(FIVE_KG_GRANITE_ID)+"\n2 Kg: " +
                            Inventory.getCount(TWO_KG_GRANITE_ID) + "\n500g: " +
                            Inventory.getCount(FIVE_HUNDRED_G_GRANITE_ID));
                }
                if(Widgets.get(905, 14).isOnScreen())
                {
                    System.out.println("Granite Widget is onscreen");

                    Widgets.get(905, 14).click(true);
                }
                else
                {
                    System.out.println("Granite Widget is not onscreen\nClicking...");
                    Inventory.getItems()[getFirstInInventory(id)].getWidgetChild().click(true);
                }
                Time.sleep(1000, 1050);
                idle = 0;
            }
        }
    }

    /**
     * @param g Graphics class
     * Draws a mouse using an image from a pre-defined URL
     */
    public void drawMouse(Graphics g)
    {
        int mouseY = (int) Mouse.getLocation().getY();
        int mouseX = (int) Mouse.getLocation().getX();
        g.drawImage(mouse, mouseX - 8, mouseY - 8, null);
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
    private String perHour(int x, int pph)
    {
        //Profit per hour = (profit * 3600000d) / timeRan
        return insertComma((int)(((x * (graniteCount / pph)) * 3600000d) /
                (System.currentTimeMillis() - startTime)));
    }
    @Override
    public void onRepaint(Graphics g1)
    {
        Graphics2D g = (Graphics2D) g1;
        drawMouse(g);
        g.setColor(new Color(255,222,173, 150));
        g.fillRect(550, 440, 185, 72);
        long millis = System.currentTimeMillis() - startTime, hours = millis / (1000 * 60 * 60), minutes, seconds;
        millis -= hours * (1000 * 60 * 60);
        minutes = millis / (1000 * 60);
        millis -= minutes * (1000 * 60);
        seconds = millis / 1000;

        g.setColor(Color.BLACK);
        g.drawString("Time: " + hours + ":" + minutes + ":" + seconds, 550, 455);
        g.drawString("Total 500g Granite: " + graniteCount, 550, 470);

        g.drawString("Granite per Hour: " + perHour(1, 1), 550, 485);
        g.drawString("Profit per Hour: " + perHour(((10 * fiveHundredGGranitePrice) - fiveKgGranitePrice), 10), 550, 500);
    }

    private class CrushTwoKGGranite extends Strategy implements Runnable
    {

        @Override
        public void run()
        {
            System.out.println("Crushing 2kg");

            crushBlock(TWO_KG_GRANITE_ID);


            graniteCount += Inventory.getCount(FIVE_HUNDRED_G_GRANITE_ID);
        }

        public boolean validate()
        {
            return Inventory.getCount(FIVE_KG_GRANITE_ID) == 0&&
                    Inventory.getCount(TWO_KG_GRANITE_ID) > 0;
        }
    }
    private class CrushFiveKGGranite extends Strategy implements Runnable
    {
        @Override
        public void run()
        {
            System.out.println("Crushing 5Kg");
            //Crush ALL the Granite!

            crushBlock(FIVE_KG_GRANITE_ID);
        }

        public boolean validate()
        {
            return Inventory.getCount(FIVE_KG_GRANITE_ID) > 0;
        }
    }
    private class BankItems extends Strategy implements Runnable
    {

        @Override
        public void run()
        {
             System.out.println("Opening Bank");

             Bank.open();
             if(Bank.isOpen())
             {
                 Time.sleep(400, 500);
                 System.out.println("Banking");
                 Bank.depositInventory();


                 if(Bank.getItemCount(FIVE_KG_GRANITE_ID) == 0)
                 {
                     //No 500Kg Granite, stopping script
                     System.out.println("No More 5Kg's, stopping");
                     Bank.close();
                     Time.sleep(200, 400);
                     Game.logout(true);
                     stop();
                 }
                 else
                 {
                     //Withdraw 2 500Kg Granite
                     Bank.withdraw(FIVE_KG_GRANITE_ID, 2);
                 }

                 Time.sleep(250,300);
                 Bank.close();
             }
        }

        public boolean validate()
        {
            return Inventory.getCount(FIVE_KG_GRANITE_ID) == 0 &&
                    (Inventory.getCount(FIVE_HUNDRED_G_GRANITE_ID) == 20 ||
                    Inventory.getCount(FIVE_HUNDRED_G_GRANITE_ID) == 10);
        }
    }
}
