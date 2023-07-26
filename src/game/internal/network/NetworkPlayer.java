package game.internal.network;

import java.awt.*;
import java.io.Serializable;

public class NetworkPlayer implements Serializable
{
    private Color color;
    private String name;

    public NetworkPlayer(Color c, String n)
    {
        this.color = c;
        this.name = n;
    }

    public String getName()
    {
        return name;
    }

    public Color getColor()
    {
        return color;
    }

    public void setColor(Color color)
    {
        this.color = color;
    }
}
