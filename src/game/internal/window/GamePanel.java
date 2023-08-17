package game.internal.window;

import game.internal.Game;
import game.internal.GameTickHandler;
import game.internal.network.NetworkThreadHandler;
import game.io.InputHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class GamePanel extends JPanel implements ActionListener
{
    private final Dimension size;
    private Game game = null;
    private final Timer timer = new Timer(15,this);

    private JButton qb = new JButton("quit");
    private MainFrame mf;
    private JPanel mPanel;

    public GamePanel(Dimension size,MainFrame mf, JPanel mPanel)
    {
        this.mPanel = mPanel;
        GameTickHandler.loop(game);
        qb.setFocusable(false);
        qb.addActionListener(new nsAction());
        this.add(qb);
        this.add(InputHandler.bindLabel);
        this.size = size;
        this.mf = mf;
        this.setBackground(Color.black);
        timer.start();

    }

    public void setGame(Game game)
    {
        this.game = game;
        //GameTickHandler.setGame(game);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        // engine draw output
        if (game != null)
            game.drawCycle(g);


        g.setColor(Color.red);

    }

    class nsAction implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            setGame(null);
            NetworkThreadHandler.killNetworkInterfaces();

            mf.changePanels(mPanel);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        repaint();
    }
}
