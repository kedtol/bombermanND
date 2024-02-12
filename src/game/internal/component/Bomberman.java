package game.internal.component;

import game.internal.Game;
import game.internal.network.NetworkPlayer;

import java.awt.image.BufferedImage;
public abstract class Bomberman extends Entity
{
    private int health;
    private int maxBombs;
    protected int placedBombs;
    private int bombSize;

    protected int selectedAxisX = 0;
    protected int selectedAxisY = 1;

    Bomberman(BufferedImage image,String imageName, Field field,Game game)
    {
        super(image,imageName, field,game);
        bombSize = 1;
        maxBombs = 1;
        health = 3;
        speed = 8;
        canKick = true;
        moveSpeed.setStart((int)(1000/speed));
        placedBombs = 0;
    }

    public int getSelectedAxisX()
    {
        return selectedAxisX;
    }
    public int getSelectedAxisY()
    {
        return selectedAxisY;
    }
    public int getHealth(){return health;}
    public int getPriority()
    {
        return -5;
    }
    public int getType()
    {
        return 0;
    }
    public boolean isContactable()
    {
        return true;
    }
    public int getBombSize()
    {
        return bombSize;
    }

    public GameObject explode() //-bomba szekvenciaszál-
    {
        health--;

        if (health <= 0)
        {
            kill();
            return null;
        }

        return this;
    }
    protected void placeBomb()
    {
        if (placedBombs < maxBombs)
        {
            //Bomb newBomb = game.createBomb(this);
            game.bombermanSendBomb(this);

            //if (newBomb != null)
            //    placedBombs++;
        }
    }
    public void bombExploded() {placedBombs--;}

    public void bombPlaced() {placedBombs++;}

    public void powerUp(int id)
    {
        switch (id)
        {
            case 1:
                maxBombs++;
            break;

            case 2:
                bombSize++;
            break;

            case 3:
                speed++;
                moveSpeed.setStart((int)(1000/speed));
            break;

            case 4:
                canKick = true;
            break;

        }

    }
    @Override
    public void onFieldEntry()
    {
        if (field.onFire())
            health--;
        if (health <= 0)
        {
            kill();
            field.removeGameObject(this);
        }
    }

    @Override
    public boolean kick(int axis,boolean positive)
    {
        return false;
    }

    public NetworkPlayer getNp()
    {
        return null;
    }
}
