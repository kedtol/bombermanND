package game.internal.component;

import game.internal.Game;
import game.internal.network.NetworkPacket;
import game.io.ResourceLoader;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import static game.internal.network.NetworkPacketType.CLIENT_PLACE_POWERUP;

public class Box extends GameObject
{
    transient private ArrayList<BufferedImage> powerUps;
    final private ArrayList<String> powerUpKeys;
    private Game game = null;
    private Field field = null;
    public Box(BufferedImage image, String imageName, boolean solid, ArrayList<BufferedImage> powerUps, ArrayList<String> powerUpKeys, Game game,Field field)
    {
        super(image, imageName, solid);
        this.powerUps = powerUps;
        this.powerUpKeys = powerUpKeys;
        this.game = game;
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
        PowerUp p = createPowerUp();
        if (game.getServer() != null)
        {
            game.getServer().sendPacket(null, new NetworkPacket(CLIENT_PLACE_POWERUP,p));
            return p;
        }
        else
        {
            game.addRequestedPowerUp(field);
            return null;
        }

    }

    @Override
    public boolean redirectExplosion()
    {
        return false;
    }

    private PowerUp createPowerUp()
    {
        Random random = new Random();
        int c = random.nextInt(100);
        int type = 0;

        if (c > 70)
        {
            c = random.nextInt(100);

            if (c < 6)
                type = 1;
            if (c >= 6 && c < 15)
                type = 4;
            if (c >= 15 && c < 45)
                type = 2;
            if (c >= 55)
                type = 3;
        }


        if (type == 0)
            return null;

        return new PowerUp(powerUps.get(type),powerUpKeys.get(type),type);
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
