package game.internal.component;

import game.internal.Game;
import game.internal.network.Client;

import java.awt.image.BufferedImage;
import java.util.UUID;

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

    public void setupSync(Client client)
    {
        this.client = client;
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
        alive = false;
        field.removeGameObject(this);
    }

    public abstract boolean kick(int axis,boolean positive);

}
