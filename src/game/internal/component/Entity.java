package game.internal.component;

import game.internal.Game;
import game.internal.network.Client;
import game.internal.network.NetworkPacket;

import java.awt.image.BufferedImage;
import java.util.UUID;

import static game.internal.network.NetworkPacketType.CLIENT_KILL_ENTITY;

public abstract class Entity extends GameObject
{
    protected Field field;
    protected Game game;
    private boolean alive = true;
    protected boolean canKick;
    protected final Alarm moveSpeed;
    protected double speed;
    protected Client client;

    Entity(BufferedImage image,String imageName,Field field,Game game)
    {
        super(image, imageName, true);
        this.game = game;
        this.field = field;
        canKick = true;
        speed = 5;
        moveSpeed = new Alarm((int)(1000/speed));
        field.acceptGameObject(this,-1,false);
    }

    public void setGame(Game game)
    {
        this.game = game;
    }

    public void move(int axis, boolean positive)
    {
        if (moveSpeed.isFinished())
        {

            Field next = field.getNeighbor(axis, positive);
            if (next != null)
            {
                if (next.acceptGameObject(this, axis, positive))
                {
                    field.removeGameObject(this);
                    field = next;
                    onFieldEntry();
                    game.entitySendMovement(this,axis,positive);
                    moveSpeed.restart();
                }
                else
                {
                    // YOU CANT SEND a KICK PACKET, if player doesn't know if it's performing a kick or not
                    // -> the bomb should send one
                    // -> THE SERVER SHOULD DETECT THE kick
                    // -> one small issue: the collision detection is local (client first)
                }
            }
        }
    }

    public void moveNetwork(int axis, boolean positive)
    {
        /*if (moveSpeed.isFinished()) // client speed hack <- insert here
        {*/
            Field next = field.getNeighbor(axis, positive);
            if (next != null)
            {
                if (next.acceptGameObject(this, axis, positive))
                {
                    field.removeGameObject(this);
                    field = next;
                    onFieldEntry();
                    moveSpeed.restart();
                }
            }
       // }
    }

    public boolean collide(GameObject go,int axis,boolean positive)
    {
        if (canKick)
            return go.collisionAccept(this,axis,positive);
        else
            return false;
    }

    public boolean collisionAccept(GameObject go,int axis,boolean positive)
    {
        if (axis >= 0)
            return kick(axis,positive);
        else
            return false;
    }

    public boolean redirectExplosion()
    {
        return true;
    }

    public abstract void onFieldEntry();

    public Field getField()
    {
        return field;
    }

    public void tickAction()
    {
        moveSpeed.tick();
    }

    public final boolean live()
    {
        if (!alive)
            return true;

        tickAction();
        return false;
    }

    public void kill()
    {
        if (game.getClient() == null) // THE SERVER SIDE LOGIC
        {
            alive = false;
            field.removeGameObject(this);
            game.getServer().sendPacket(null, new NetworkPacket(CLIENT_KILL_ENTITY, this.networkID));
        }
    }

    public void networkKill() // FOR CLIENTS!
    {
        alive = false;
        field.removeGameObject(this);
    }

    public abstract boolean kick(int axis,boolean positive);

}
