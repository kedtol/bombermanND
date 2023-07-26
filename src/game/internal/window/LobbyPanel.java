package game.internal.window;

import game.internal.network.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class LobbyPanel extends JPanel
{
    private JButton ccButton = new JButton("change color");

    private JButton sButton = new JButton("Start");
    private JComboBox<Integer> pcb = new JComboBox<Integer>(new Integer[]{1,2,3,4,5,6});
    private JComboBox<Integer> ccb = new JComboBox<Integer>(new Integer[]{0,1,2,3,4,5,6});
    private JComboBox<Integer> db = new JComboBox<Integer>(new Integer[]{2,3,4});
    private JComboBox<Integer> sb = new JComboBox<Integer>(new Integer[]{11,21,41});

    private int colorc = 0;

    private JButton lButton = new JButton("leave");

    private boolean host;
    private Color color;
    private Server server;
    private Client client;

    private ArrayList<NetworkPlayer> players = new ArrayList<>();

    private MainFrame mf;
    private JPanel mPanel;

    public LobbyPanel(MainFrame mf, JPanel mPanel,boolean host)
    {
        this.mPanel = mPanel;
        this.mf = mf;
        this.host = host;
        ccButton.addActionListener(new ccAction());
        lButton.addActionListener(new lAction());
        buildPanel();

    }

    private void buildPanel()
    {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5,5,5,5);

        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.anchor = GridBagConstraints.CENTER;

        this.add(new JLabel("Lobby"),gbc);
        gbc.gridy++;

        for (NetworkPlayer np : players)
        {
            JLabel nameText = new JLabel(np.getName());
            nameText.setForeground(np.getColor());
            this.add(nameText,gbc);
            gbc.gridy++;
        }

        gbc.gridx = 0;
        if (host)
        {
            this.add(sButton,gbc);
            gbc.gridx++;
            this.add(new JLabel("dimensions:"),gbc);
            gbc.gridx++;
            this.add(db,gbc);
            gbc.gridx++;
            this.add(new JLabel("size:"),gbc);
            gbc.gridx++;
            this.add(sb,gbc);
            gbc.gridx++;
            this.add(new JLabel("AI:"),gbc);
            gbc.gridx++;
            this.add(ccb,gbc);
            gbc.gridx = 0;
            gbc.gridy++;
        }

        this.add(ccButton,gbc); // color change button
        gbc.gridy++;

        gbc.gridx = 0;
        gbc.gridy++;

        this.add(lButton,gbc); // leave button

        this.add(Box.createVerticalStrut(50));
        this.revalidate();
        this.repaint();
    }

    public void setup()
    {
        NetworkThreadHandler.loop();
        client.setLobby(this);
    }

    public void updatePlayers(ArrayList<NetworkPlayer> nplist)
    {
        this.removeAll();
        players.clear();
        players.addAll(nplist);
        buildPanel();
    }
    public void setClient(Client client)
    {
        this.client = client;
    }

    public void setServer(Server server)
    {
        this.server = server;
    }

    public void backToMenu()
    {
        if (server == null)
        {
            NetworkThreadHandler.killNetworkInterfaces();
            client = null;
            server = null;
            mf.changePanels(mPanel);
        }
    }

    class ccAction implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            client.sendPacket(new NetworkPacket(3,false,null));
        }
    }


    class lAction implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            NetworkThreadHandler.killNetworkInterfaces();
            client = null;
            server = null;
            mf.changePanels(mPanel);
        }
    }
}
