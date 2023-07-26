package game.internal.component;

import java.awt.*;
import java.io.Serializable;
import java.util.LinkedList;

public class Camera implements Serializable
{
    private final Bomberman bomberman;
    private final int projectionSize;
    private final int projectionX;
    private final int projectionY;

    public Camera(Bomberman player, int projectionSize, int projectionX, int projectionY)
    {
        this.bomberman = player;
        this.projectionSize = projectionSize;
        this.projectionX = projectionX;
        this.projectionY = projectionY;
    }

    public void draw(Graphics g)
    {
        Field f = bomberman.getField();
        f.draw(g,projectionX,projectionY, bomberman.getSelectedAxisX(), bomberman.getSelectedAxisY()); // center
        drawYFieldsInAxis(f,true,projectionX,g);
        drawYFieldsInAxis(f,false,projectionX,g);
        drawXFieldsInAxis(f,true,g); //x+
        drawXFieldsInAxis(f,false,g); //x-
        g.drawString( bomberman.getSelectedAxisX()+".X "+ bomberman.getSelectedAxisY()+ ".Y ",projectionX,projectionY-230);
        g.setColor(Color.red);
        g.drawString("pHealth: "+ bomberman.getHealth(),projectionX,projectionY-250);
    }

    private LinkedList<Field> getFieldsInAxis(Field field, int axis,boolean positive)
    {
        Field next = field.getNeighbor(axis,positive);
        if (next != null)
            return next.explore(axis, positive, projectionSize);
        else
            return null;
    }

    private void drawYFieldsInAxis(Field field, boolean positive,int x, Graphics g)
    {
        Field next = field.getNeighbor(bomberman.getSelectedAxisY(),positive);
        if (next != null)
        {
            LinkedList<Field> fieldsInAxis = next.explore(bomberman.getSelectedAxisY(), positive, projectionSize);
            int y = 1;
            int sign = 1;
            if (!positive)
                sign = -1;
            while (fieldsInAxis.size() > 0)
            {
                Field selected = fieldsInAxis.removeLast();
                selected.draw(g, x, projectionY-32*y*sign, bomberman.getSelectedAxisX(), bomberman.getSelectedAxisY());
                y++;
            }
        }
    }

    private void drawXFieldsInAxis(Field field, boolean positive, Graphics g)
    {
        if (field != null)
        {
            LinkedList<Field> xpositve = getFieldsInAxis(field, bomberman.getSelectedAxisX(),positive);
            int x = 1;
            int sign = 1;
            if (!positive)
                sign = -1;

            if (xpositve != null)
                while (xpositve.size() > 0)
                {
                    Field selected = xpositve.removeLast();
                    int x1 = (x * 32) * sign + projectionX;
                    selected.draw(g, x1, projectionY, bomberman.getSelectedAxisX(), bomberman.getSelectedAxisY());
                    drawYFieldsInAxis(selected, true, x1, g); //y+
                    drawYFieldsInAxis(selected, false, x1, g); //y-
                    x++;
                }
        }

    }

}
