package game.internal.window;

import game.internal.network.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MenuPanel extends JPanel
{
    private JButton hButton = new JButton("host");
    private JButton jButton = new JButton("join");
    private JButton exButton = new JButton("exit");


    private JTextField ntf = new JTextField("player");
    private JTextField itf = new JTextField("127.0.0.1");

    private JTextField ptf = new JTextField("29869");
    private MainFrame mf;
    private MenuPanel self = this; // i call this the ducktape method

    public MenuPanel(MainFrame mf)
    {

        this.mf = mf;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        hButton.addActionListener(new hAction());
        jButton.addActionListener(new jAction());
        exButton.addActionListener(new exAction());

        gbc.insets = new Insets(5,5,5,5);

        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.anchor = GridBagConstraints.CENTER;

        this.add(new JLabel("Bomberman"),gbc);
        gbc.gridy++;
        this.add(new JLabel("Name:"),gbc);
        gbc.gridx++;
        this.add(ntf,gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        this.add(jButton,gbc);
        gbc.gridx++;
        this.add(new JLabel("Ip:"),gbc);
        gbc.gridx++;
        this.add(itf,gbc);
        gbc.gridx++;
        this.add(new JLabel("Port:"),gbc);
        gbc.gridx++;
        this.add(ptf,gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        this.add(hButton,gbc);
        gbc.gridy++;

        this.add(exButton,gbc);

        this.add(Box.createVerticalStrut(50));


    }

    class hAction implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            try
            {
                Server gameServer = new Server();
                Client gameClient = new Client("127.0.0.1","29869",new NetworkPlayer(Color.red,ntf.getText()));
                LobbyPanel lp = new LobbyPanel(mf,self,true);
                lp.setClient(gameClient);
                lp.setServer(gameServer);
                NetworkThreadHandler.addNetworkInterface(gameServer);
                NetworkThreadHandler.addNetworkInterface(gameClient);
                mf.changePanels(lp);
                lp.setup();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

        }
    }

    class jAction implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            try
            {
                Client gameClient = new Client(itf.getText(),ptf.getText(),new NetworkPlayer(Color.red,ntf.getText()));
                LobbyPanel lp = new LobbyPanel(mf,self,false);
                lp.setClient(gameClient);
                NetworkThreadHandler.addNetworkInterface(gameClient);
                mf.changePanels(lp);
                lp.setup();
            }
            catch (IOException e)
            {
                System.out.println("Connection Failed.");
                throw new RuntimeException(e);
            }

        }
    }

    class exAction implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            System.exit(0);
        }
    }
}
