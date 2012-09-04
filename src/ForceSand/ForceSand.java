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
    private final int BUCKET_OF_SAND_ID = 0, EMPTY_BUCKET_ID = 0, SAND_PILE_ID = 0;
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
     @Override
     protected void setup()
     {
        ForceSandGUI g = new ForceSandGUI(0.01);
     }
     private class ForceSandGUI extends JDialog
     {
        private JPanel contentPane;
        private JButton buttonOK;
        private JButton buttonCancel;
        private JComboBox comboBox1;
        private String location;

        public ForceSandGUI(double ver)
        {
            setContentPane(contentPane);
            setModal(true);
            setTitle("ForceSand v" + ver);
            getRootPane().setDefaultButton(buttonOK);

            buttonOK.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    onOK();
                }
            });

            buttonCancel.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    onCancel();
                }
            });

// call onCancel() when cross is clicked
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter()
            {
                public void windowClosing(WindowEvent e)
                {
                    onCancel();
                }
            });

// call onCancel() on ESCAPE
            contentPane.registerKeyboardAction(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    onCancel();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        }

        private void onOK()
        {
            location = (String)comboBox1.getSelectedItem();

            provide(new BankItems());
            provide(new fillBucketsWithSand());
            provide(new WalkToBank());
            provide(new walkToSandPit());
            dispose();
        }

        public String getSandboxLocation()
        {
            return location;
        }
        private void onCancel()
        {
            dispose();
            stop();
        }

        private void createUIComponents() {
            comboBox1 = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[] {
                    "Dorgesh-Kaan",
                    "Entrana",
                    "Rellekka",
                    "Yanille",
                    "Zanaris"}));
        }
    }

    private class WalkToBank extends Strategy implements Runnable
    {
        @Override
        public void run() 
        {
            YANILLE_BANK_TO_SAND_PILE.reverse().traverse();
        }
        
        @Override
        public boolean validate()
        {
            return Inventory.getCount(BUCKET_OF_SAND_ID) > 0 &&
                    Inventory.getCount(EMPTY_BUCKET_ID) == 0;
        }
    }
    
    private class BankItems extends Strategy implements Runnable
    {

        @Override
        public void run() 
        {
            Bank.open();
            
            if(Bank.isOpen())
            {
                Bank.depositInventory();
                
                if(Bank.getItemCount(EMPTY_BUCKET_ID) == 0)
                {
                    Bank.close();
                    Game.logout(true);
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
                    Inventory.getCount(BUCKET_OF_SAND_ID) > 0 &&
                    Inventory.getCount(EMPTY_BUCKET_ID) == 0;
        }
        
    }
    
    private class walkToSandPit extends Strategy implements Runnable
    {
        @Override
        public void run() 
        {
            YANILLE_BANK_TO_SAND_PILE.traverse();
        }
        
        @Override
        public boolean validate()
        {
            return YANILLE_BANK.contains(Players.getLocal().getLocation()) &&
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
                        if (checkIdle > 4) 
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