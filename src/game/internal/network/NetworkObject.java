package game.internal.network;
import java.io.Serializable;
import java.util.*;
public class NetworkObject implements Serializable
{
    public UUID networkID;

    public NetworkObject()
    {
        networkID = UUID.randomUUID();
    }
}
