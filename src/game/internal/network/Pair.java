package game.internal.network;

import java.io.Serializable;

public class Pair<L,R> implements Serializable //packet with infinite payload?
{
    public L left;
    public R right;

}
