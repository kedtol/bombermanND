package game.internal.window;

import game.internal.Game;
import game.internal.GameTickHandler;
import game.internal.network.Client;
import game.internal.network.NetworkPlayer;
import game.internal.network.Server;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MainFrame extends JFrame
{
    private GamePanel gp;
    private MenuPanel mp;
    private boolean usingMenu = true;

    private JPanel currentp;

    private Server server;
    private Client client;

    private Dimension size;

    public MainFrame(Dimension size)
    {
        this.size = size;
        mp = new MenuPanel(this);
        gp = new GamePanel(size,this,mp);
        currentp = mp;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Bomberman");

        this.add(mp);

        this.pack();
        this.setSize(size);

        this.setVisible(true);
    }

    public void changePanels(JPanel jPanel)
    {
        if (currentp != null)
            this.remove(currentp);

        currentp = jPanel;
        this.add(jPanel);

        this.pack();
        this.setSize(size);

        this.setVisible(true);

    }

    public void startNewGame(Game game)
    {
        changePanels(gp);
        gp.setGame(game);
        GameTickHandler.loop(game);
        game.start();
    }

    public void loadGame()
    {
        Game game = new Game();
        game.load();
        gp.setGame(game);
        game.start();
    }

}
