package ForceSand;

import javax.swing.*;
import java.awt.event.*;

public class ForceSandGUI extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox comboBox1;
    private String location;

    public ForceSandGUI(double ver) {
        setContentPane(contentPane);
        setModal(true);
        setTitle("ForceSand v" + ver);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK()
    {
        location = (String)comboBox1.getSelectedItem();

        /*provide(new BankItems());
        provide(new fillBucketsWithSand());
        provide(new WalkToBank());
        provide(new walkToSandPit());*/
        dispose();
    }

    public String getSandboxLocation()
    {
        return location;
    }
    private void onCancel()
    {
        //stop();
        dispose();
    }

    public static void main(String[] args) {
        ForceSandGUI dialog = new ForceSandGUI(0.01);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
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
