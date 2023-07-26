package game.internal.component;

import java.awt.image.BufferedImage;

public class Wall extends GameObject
{

    public Wall(BufferedImage image,String imageName, boolean solid)
    {
        super(image, imageName, solid);
    }

    @Override
    public GameObject explode()
    {
        return this;
    }

    @Override
    public boolean redirectExplosion()
    {
        return false;
    }

    @Override
    public int getPriority()
    {
        return 0;
    }

    @Override
    public int getType()
    {
        return 0;
    }

    @Override
    public boolean isContactable()
    {
        return false;
    }
}
