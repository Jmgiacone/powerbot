package ForceSand;

import org.powerbot.concurrent.strategy.Strategy;
import org.powerbot.game.api.ActiveScript;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.widget.Bank;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.Area;
import org.powerbot.game.api.wrappers.Identifiable;
import org.powerbot.game.api.wrappers.Tile;

@Manifest(name = "ForceGranite",
        description = "Splits Granite blocks down to their smallest form for profit",
        version = 0.01,
        authors = "Jdog653")
public class ForceGranite extends ActiveScript
{
    private final int FIVE_KG_GRANITE_ID = 6983,
            TWO_KG_GRANITE_ID = 6981,
            FIVE_HUNDRED_G_GRANITE_ID = 6979,
            CHISELING_ANIMATION = 11146;

    private final Area VARROCK_WEST_BANK = new Area(
            new Tile[] {
                new Tile(3177, 3447, 0),
                new Tile(3194, 3447, 0),
                new Tile(3194, 3431, 0),
                new Tile(3177, 3431, 0) });

    private Area BANK;

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
        BANK = VARROCK_WEST_BANK;
        provide(new CrushFiveKGGranite());
        provide(new CrushTwoKGGranite());
        provide(new BankItems());
    }

    private void crushBlock(final int id)
    {
        if(Widgets.get(905, 14).isOnScreen())
        {
            System.out.println("Granite Widget is onscreen");
            Widgets.get(905, 14).click(true);
        }
        else
        {
            //11146
            System.out.println("Granite Widget is not onscreen");
            Inventory.getItems()[getFirstInInventory(id)].getWidgetChild().click(true);
        }

        int idle = 0;
        while(Inventory.getCount(id) > 0)
        {
            if(Players.getLocal().getAnimation() == -1)
            {
                idle++;
            }
            else if(Players.getLocal().getAnimation() == 11146)
            {
                idle = 0;
                System.out.println("Lol, not actually Idle");
            }

            if(idle > 350)
            {
                System.out.println("IDLE: " +idle);
                if(Widgets.get(905, 14).isOnScreen())
                {
                    System.out.println("Granite Widget is onscreen");
                    Widgets.get(905, 14).click(true);
                    Time.sleep(300);
                    idle = 0;
                }
                else
                {
                    System.out.println("Granite Widget is not onscreen");
                    Inventory.getItems()[getFirstInInventory(id)].getWidgetChild().click(true);
                    Time.sleep(200);
                }
            }
        }
    }
    private class CrushTwoKGGranite extends Strategy implements Runnable
    {

        @Override
        public void run()
        {
            System.out.println("Crushing 2kg");

            crushBlock(TWO_KG_GRANITE_ID);
        }

        public boolean validate()
        {
            return Inventory.getCount(TWO_KG_GRANITE_ID) > 0 &&
                    Inventory.getCount(FIVE_KG_GRANITE_ID) == 0;
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
                 System.out.println("Banking");
                 Bank.depositInventory();

                 if(Bank.getItemCount(FIVE_KG_GRANITE_ID) == 0)
                 {
                     //No 500Kg Granite, stopping script
                     Bank.close();
                     Time.sleep(Random.nextInt(200, 400));
                     stop();
                 }
                 else
                 {
                     //Withdraw 2 500Kg Granite
                     Bank.withdraw(FIVE_KG_GRANITE_ID, 2);
                 }

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
