package game.internal.component;

import game.internal.Game;
import game.internal.network.NetworkPacket;
import game.io.ResourceLoader;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Box extends GameObject
{
    transient private ArrayList<BufferedImage> powerUps;
    final private ArrayList<String> powerUpKeys;
    private Game game = null;
    private Field field = null;
    private int type;
    public Box(BufferedImage image, String imageName, boolean solid, ArrayList<BufferedImage> powerUps, ArrayList<String> powerUpKeys, int type,Field field)
    {
        super(image, imageName, solid);
        this.powerUps = powerUps;
        this.powerUpKeys = powerUpKeys;
        this.type = type;
        this.field = field;

    }

    public void setImage(ResourceLoader r)
    {
        powerUps = new ArrayList<>();

        for (String k : powerUpKeys)
            powerUps.add(r.getImage(k));

        super.setImage(r);
    }

    @Override
    public GameObject explode()
    {
        if (type == 0)
            return null;

        return new PowerUp(powerUps.get(type),powerUpKeys.get(type),type);
    }

    @Override
    public boolean redirectExplosion()
    {
        return false;
    }

    @Override
    public int getPriority()
    {
        return 2;
    }

    @Override
    public int getType()
    {
        return 1;
    }

    @Override
    public boolean isContactable()
    {
        return true;
    }
}
