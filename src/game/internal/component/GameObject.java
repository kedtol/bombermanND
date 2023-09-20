package game.internal.component;

import game.internal.network.NetworkObject;
import game.io.ResourceLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;

public abstract class GameObject extends NetworkObject implements Interest
{
    transient protected BufferedImage image;
    private final String imageName;
    private final boolean solid;

    public GameObject(BufferedImage image, String imageName, boolean solid)
    {
        this.image = image;
        this.imageName = imageName;
        this.solid = solid;
    }

    public void setImage(ResourceLoader r)
    {
        this.image = r.getImage(imageName);
    }

    public String getImageName()
    {
        return imageName;
    }

    public void draw(Graphics g, int x, int y)
    {
        if (image != null)
            g.drawImage(image, x, y, null);
    }
    public abstract GameObject explode();
    public abstract boolean redirectExplosion();
    public boolean collide(GameObject go,int axis,boolean positive)
    {
        return false;
    }
    public boolean collisionAccept(GameObject go,int axis,boolean positive)
    {
        return false;
    }
    public boolean getSolid()
    {
        return solid;
    }
    public void powerUp(int id){}

}
