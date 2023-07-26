package game.internal.window;

import game.internal.Game;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame
{
    private GamePanel gp;
    private MenuPanel mp;
    private boolean usingMenu = true;

    private JPanel currentp;

    private Dimension size;

    public MainFrame(Dimension size)
    {
        this.size = size;
        gp = new GamePanel(size,this,mp);
        mp = new MenuPanel(this);
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

    public void startNewGame(int dim,int size, int pc, int cc)
    {
        Game game = new Game();
        switch (dim)
        {
            case 2 -> game.generateMap(size, size);
            case 3 -> game.generateMap3d(size, size, size);
            case 4 -> game.generateMap4d(size, size, size, size);
        }

        for (int i = 0; i < pc; i++)
            game.addPlayer(i);

        for (int i = 0; i < cc; i++)
            game.addEnemy();

        gp.setGame(game);
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
