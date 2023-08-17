package game.internal.network;

import java.io.Serial;
import java.io.Serializable;

public class NetworkPacket implements Serializable
{
    public NetworkPacketType type;
    public boolean server;
    public Object content;

    public int source = -1;

    public NetworkPacket(NetworkPacketType type, boolean server, Object o)
    {
        this.type = type;
        this.server = server;
        this.content = o;
    }

    public NetworkPacket(NetworkPacketType type, Object o)
    {
        this.type = type;
        this.server = false;
        this.content = o;
    }

}
