package game.internal.component;

import java.io.Serializable;

public class Alarm implements Serializable
{
    private int current;
    private int start;

    public Alarm(int start)
    {
        this.start = start;
        //this.current = start;
    }

    public boolean tick()
    {
        if (current > 0)
            current--;
        else
            return true;
        return false;
    }

    public boolean isFinished()
    {
        if (current == 0)
            return true;
        else
            return false;
    }

    public void restart()
    {
        current = start;
    }

    public void setStart(int start)
    {
        this.start = start;
    }
}
