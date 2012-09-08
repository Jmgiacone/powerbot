package ForceSand;

import org.powerbot.concurrent.strategy.Strategy;
import org.powerbot.game.api.ActiveScript;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.widget.Bank;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.Area;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.map.TilePath;
import org.powerbot.game.api.wrappers.node.SceneObject;

import javax.swing.*;
import java.awt.event.*;

/**
 *
 * @author Jordan
 */
@Manifest(name = "ForceSand", description = "Fills buckets with sand quickly and efficiently for profit",
        authors = {"Jdog653"}, version = 0.01)
public class ForceSand extends ActiveScript
{
    private ForceSandGUI gui;
    private final int BUCKET_OF_SAND_ID = 1783, EMPTY_BUCKET_ID = 1925, SAND_PILE_ID = 20989;
    private final Area 
            YANILLE_BANK = new Area(
                new Tile[] {
                    new Tile(2607, 3097, 0), 
                    new Tile(2616, 3097, 0), 
                    new Tile(2616, 3087, 0), 
                    new Tile(2607, 3087, 0)}),
            YANILLE_SAND_PILE = new Area(
                new Tile[] {
                    new Tile(2541, 3108, 0), 
                    new Tile(2539, 3106, 0), 
                    new Tile(2538, 3106, 0), 
                    new Tile(2538, 3098, 0), 
                    new Tile(2546, 3098, 0), 
                    new Tile(2546, 3108, 0)});
    private final TilePath 
            YANILLE_BANK_TO_SAND_PILE = new TilePath(
                new Tile[] {
                    new Tile(2611, 3092, 0), 
                    new Tile(2608, 3092, 0), 
                    new Tile(2605, 3093, 0), 
                    new Tile(2602, 3095, 0), 
                    new Tile(2599, 3096, 0), 
                    new Tile(2596, 3097, 0), 
                    new Tile(2593, 3097, 0), 
                    new Tile(2590, 3097, 0), 
                    new Tile(2587, 3097, 0), 
                    new Tile(2584, 3097, 0), 
                    new Tile(2581, 3098, 0), 
                    new Tile(2578, 3095, 0), 
                    new Tile(2576, 3092, 0), 
                    new Tile(2573, 3090, 0), 
                    new Tile(2570, 3089, 0), 
                    new Tile(2567, 3089, 0), 
                    new Tile(2564, 3090, 0), 
                    new Tile(2561, 3090, 0), 
                    new Tile(2558, 3090, 0), 
                    new Tile(2556, 3091, 0), 
                    new Tile(2553, 3092, 0), 
                    new Tile(2550, 3092, 0), 
                    new Tile(2547, 3093, 0), 
                    new Tile(2545, 3096, 0), 
                    new Tile(2543, 3099, 0), 
                    new Tile(2543, 3102, 0)});

     private String botState;
     @Override
     protected void setup()
     {
         provide(new BankItems());
         provide(new fillBucketsWithSand());
         provide(new WalkToBank());
         provide(new walkToSandPit());
         /*if (gui == null) {
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     gui = new ForceSandGUI(0.01);
                     gui.pack();
                     gui.setVisible(true);
                 }
             });
         }*/
     }
    private class WalkToBank extends Strategy implements Runnable
    {
        @Override
        public void run()
        {
            botState = "Walking back to Bank";
            System.out.println(botState);
            YANILLE_BANK_TO_SAND_PILE.reverse().traverse();
        }

        @Override
        public boolean validate()
        {
            return !YANILLE_BANK.contains(Players.getLocal().getLocation()) &&
            Inventory.getCount(BUCKET_OF_SAND_ID) > 0 &&
                    Inventory.getCount(EMPTY_BUCKET_ID) == 0;
        }
    }

    private class BankItems extends Strategy implements Runnable
    {

        @Override
        public void run()
        {
            botState = "Banking" ;
            System.out.println(botState);
            Bank.open();

            if(Bank.isOpen())
            {
                Bank.depositInventory();

                if(Bank.getItemCount(EMPTY_BUCKET_ID) == 0)
                {
                    Bank.close();
                    System.out.println("Finished, no empties left");
                    Game.logout(true);
                    Time.sleep(500);
                    stop();
                }
                else
                {
                    Bank.withdraw(EMPTY_BUCKET_ID, 0);
                    Bank.close();
                }
            }
        }

        @Override
        public boolean validate()
        {
            return YANILLE_BANK.contains(Players.getLocal().getLocation()) &&
                    Inventory.getCount(EMPTY_BUCKET_ID) == 0;
        }

    }

    private class walkToSandPit extends Strategy implements Runnable
    {
        @Override
        public void run()
        {
            botState = "Walking to Pit";
            System.out.println(botState);
            YANILLE_BANK_TO_SAND_PILE.traverse();
        }

        @Override
        public boolean validate()
        {
            return !YANILLE_SAND_PILE.contains(Players.getLocal().getLocation()) &&
                    Inventory.getCount(EMPTY_BUCKET_ID) > 0 &&
                    Inventory.getCount(BUCKET_OF_SAND_ID) == 0;
        }

    }

    private class fillBucketsWithSand extends Strategy implements Runnable
    {
        int i, checkIdle;
        @Override
        public void run()
        {
            botState = "Filling Buckets";
            System.out.println(botState);
            SceneObject sand = SceneEntities.getNearest(SAND_PILE_ID);

            for(i = 0; i < 28; i++)
            {
                if(Inventory.getItems()[i].getId() == EMPTY_BUCKET_ID)
                {
                    break;
                }
            }

            Inventory.getItems()[i].getWidgetChild().click(true);

            sand.interact("use");

            while (Inventory.getCount(EMPTY_BUCKET_ID) > 0)
            {
                Time.sleep(500);
                //antiBan();
                if (Players.getLocal().getAnimation() == -1)
                {
                        checkIdle++;
                        //antiBan();
                        Time.sleep(500);
                        if (Players.getLocal().getAnimation() == 895)
                        {
                                checkIdle = 0;
                                //antiBan();
                        }

                        // Filling was interrupted, now resuming from last filled item
                        if (checkIdle > 8)
                        {
                                //antiBan();
                                System.out.println("IDLE");
                                while (!(Inventory.getItems()[i].getId() == EMPTY_BUCKET_ID))
                                {
                                        if (Inventory.getItems()[i].getId() == EMPTY_BUCKET_ID)
                                        {
                                                System.out.println("match found");
                                                break;
                                        }
                                        i++;

                                }

                                Inventory.getItems()[i].getWidgetChild().click(true);

                                sand.interact("use");

                                Time.sleep(2000);
                        }
                }
            }
        }

        @Override
        public boolean validate()
        {
            return YANILLE_SAND_PILE.contains(Players.getLocal().getLocation()) &&
                    Inventory.getCount(BUCKET_OF_SAND_ID) == 0 &&
                    Inventory.getCount(EMPTY_BUCKET_ID) > 0;
        }

    }
}