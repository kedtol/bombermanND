package game.internal.component;

import java.awt.image.BufferedImage;

public class PowerUp extends GameObject
{
    int id;

    public PowerUp(BufferedImage image,String imageName, int id)
    {
        super(image, imageName, false);
        this.id = id;
    }

    public boolean collide(GameObject go,int axis,boolean positive)
    {
        go.powerUp(id);
        return true;
    }

    @Override
    public GameObject explode()
    {
        return this;
    }

    @Override
    public boolean redirectExplosion()
    {
        return true;
    }

    @Override
    public int getPriority()
    {
        return 4;
    }

    @Override
    public int getType()
    {
        return 0;
    }

    @Override
    public boolean isContactable()
    {
        return true;
    }
}
