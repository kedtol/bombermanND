package game.internal.component;

import game.internal.Game;
import game.internal.network.Client;
import game.internal.network.NetworkPlayer;
import game.io.Bind;
import game.io.InputHandler;

import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;


public class Player extends Bomberman
{
    private final Alarm bombPlantCooldown = new Alarm(400);
    private final Alarm rotateCooldown = new Alarm(200);
    private ArrayList<Bind> binds;
    private NetworkPlayer np;

    public Player(BufferedImage image, String imageName, Field field, Game game, ArrayList<Bind> binds, NetworkPlayer np)
    {
        super(image,imageName, field, game);
        this.binds = binds;
        this.np = np;
    }

    public void setBinds(ArrayList<Bind> binds)
    {
        this.binds = binds;
    }

    public NetworkPlayer getNp()
    {
        return np;
    }

    public void draw(Graphics g, int x, int y)
    {
        super.draw(g,x,y);
        g.setColor(Color.red);

    }

    @Override
    public void tickAction()
    {
        super.tickAction();
        bombPlantCooldown.tick();
        rotateCooldown.tick();

        if (binds != null)
        {
            if (InputHandler.isButtonPressed(binds.get(0)))
                super.move(selectedAxisY, true);

            if (InputHandler.isButtonPressed(binds.get(1)))
                super.move(selectedAxisY, false);

            if (InputHandler.isButtonPressed(binds.get(2)))
                super.move(selectedAxisX, false);

            if (InputHandler.isButtonPressed(binds.get(3)))
                super.move(selectedAxisX, true);

            if (bombPlantCooldown.isFinished())
            {
                if (InputHandler.isButtonPressed(binds.get(4)))
                {
                    super.placeBomb();
                    bombPlantCooldown.restart();
                }
            }

            if (InputHandler.isButtonPressed(binds.get(5)))
            {
                if (rotateCooldown.isFinished())
                {
                    if (selectedAxisX < game.getDimensions() - 1)
                        selectedAxisX++;
                    else
                        selectedAxisX = 0;

                    if (selectedAxisY < game.getDimensions() - 1)
                        selectedAxisY++;
                    else
                        selectedAxisY = 0;

                    rotateCooldown.restart();
                }
            }
        }

    }


}
