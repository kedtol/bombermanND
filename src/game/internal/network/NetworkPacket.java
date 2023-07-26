package game.internal.network;

import java.io.Serial;
import java.io.Serializable;

public class NetworkPacket implements Serializable
{
    public int id;
    public boolean server;
    public Object content;

    public int source = -1;

    public NetworkPacket(int id, boolean server, Object o)
    {
        this.id = id;
        this.server = server;
        this.content = o;
    }

}
