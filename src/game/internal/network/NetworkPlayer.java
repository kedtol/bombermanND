package game.internal.network;

import game.internal.component.Color;

import java.io.Serializable;

public class NetworkPlayer implements Serializable
{
    private int id;
    private Color color;
    private String name;

    private boolean ai;

    public NetworkPlayer(Color c, String n,int id, boolean ai)
    {
        this.color = c;
        this.name = n;
        this.id = id;
        this.ai = ai;
    }

    public String getName()
    {
        return name;
    }

    public Color getColor()
    {
        return color;
    }

    public int getId() {return id;}

    public boolean getAI() {return ai;}

    public void setId(int id){this.id = id;}

    public void setColor(Color color)
    {
        this.color = color;
    }
}
