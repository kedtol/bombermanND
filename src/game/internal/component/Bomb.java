package game.internal.component;

import game.internal.Game;

import java.awt.image.BufferedImage;

public class Bomb extends Entity
{
    private boolean kicked;
    private int axis;
    private boolean positive;
    private int explosionSize;
    private final Alarm fuse;

    private boolean exploded = false;

    private final Bomberman bomberman;

    public Bomb(BufferedImage image,String imageName, Field field, Game game,Bomberman bomberman, int explosionSize)
    {
        super(image,imageName, field,game);
        int startFuse = 3000;
        fuse = new Alarm(startFuse);
        fuse.restart();
        this.explosionSize = explosionSize;
        kicked = false;
        this.bomberman = bomberman;
    }

    @Override
    public void tickAction()
    {
        fuse.tick();

        if (fuse.isFinished())
        {
            if (!exploded)
            {
                explode();
            }
            else
            {
                for (int i = 0; i < 4; i++)
                    field.initiateExtinguishment(i,explosionSize);
                field.removeGameObject(this);
                bomberman.bombExploded();
                kill();
            }
        }

        if (kicked && !exploded)
        {
            move(axis, positive);
        }

        super.tickAction();
    }

    @Override
    public boolean kick(int axis,boolean positive)
    {
        kicked = true;
        this.axis = axis;
        this.positive = positive;
        return true;
    }

    public boolean collide(GameObject go,int axis,boolean positive)
    {
        kicked = false;
        return super.collide(go,axis,positive);
    }

    @Override
    public void onFieldEntry()
    {
        if (field.onFire())
            explode();
    }

    public void move(int axis, boolean positive)
    {
        if (moveSpeed.isFinished())
        {
            super.move(axis, positive);
            moveSpeed.restart();
        }
    }

    public int getPriority()
    {
        return explosionSize+1;
    }

    public int getType()
    {
        return 2;
    }

    public boolean isContactable()
    {
        return true;
    }

    @Override
    public GameObject explode()
    {
        kicked = false;
        field.initiateExplosion(game.getDimensions(),explosionSize-1,this);

        exploded = true;

        int explosionFuse = 600;
        fuse.setStart(explosionFuse);
        fuse.restart();

        return this;
    }
}
