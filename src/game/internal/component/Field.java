package game.internal.component;

import game.internal.Game;
import game.io.ResourceLoader;

import java.awt.*;
import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Field implements Serializable
{
    private List<GameObject> gameObjects;
    private boolean onFire;
    private transient ArrayList<FieldPair> neighbors;

    public Field()
    {
        gameObjects = Collections.synchronizedList(new ArrayList<>()); // így nem lesz thread gond
        neighbors = new ArrayList<>();
    }

    public void serializeNeighbors(ObjectOutputStream oos) throws IOException
    {
        oos.writeObject(neighbors);
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException
    {
        List<GameObject> players = new ArrayList<>();

        for (GameObject go : gameObjects)
        {
            if ( go.getImageName().contains("player")) // cursed type check, i know
                players.add(go);
        }

        gameObjects.removeAll(players);

        oos.defaultWriteObject();
        oos.writeObject(onFire);
        oos.writeObject(gameObjects);

        gameObjects.addAll(players);
    }

    private void readObject(ObjectInputStream ois)
            throws ClassNotFoundException, IOException {

        ois.defaultReadObject();

        onFire = (boolean) ois.readObject();
        gameObjects = (List<GameObject>) ois.readObject();



    }


    public void loadNeighbors(ObjectInputStream ois) throws IOException, ClassNotFoundException
    {
        neighbors = (ArrayList<FieldPair>) ois.readObject();
    }
    public void addGameObject(GameObject go)
    {
        gameObjects.add(go);
    }

    public Field getNeighbor(int axis, boolean positive)
    {
        if (neighbors != null)
            if (neighbors.size() > axis)
                if (neighbors.get(axis) != null)
                    return neighbors.get(axis).get(positive);
        return null;
    }

    public boolean placeBomb(Bomb bomb)
    {
        int solidCount = 0;
        for (GameObject g: gameObjects)
            if (g.getSolid())
                solidCount++;

        if (solidCount <2)
        {
            gameObjects.add(bomb);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean acceptGameObject(GameObject go,int axis,boolean positive)
    {
        boolean ret = true;
        ArrayList<GameObject> remove = new ArrayList<>();

        for (GameObject g : gameObjects)
        {
            if (g.getSolid())
            {
                ret = false;
                go.collide(g, axis, positive); // feature not implemented
            }
            else
                if (g.collide(go, axis, positive))
                    remove.add(g);
        }

        gameObjects.removeAll(remove);

        if (ret)
            gameObjects.add(go);

        return ret;
    }
    public void removeGameObjects()
    {
        gameObjects = Collections.synchronizedList(new ArrayList<>());
    }
    public void removeGameObject(GameObject go)
    {
        gameObjects.remove(go);
    }
    public List<GameObject> getGameObjects()
    {
        return gameObjects;
    }

    public void link(Field field, int axis, boolean positive,boolean singleLink)
    {
        while (neighbors.size() <= axis)
            neighbors.add(null);

        if (neighbors.get(axis) != null)
        {
            FieldPair currentPair = neighbors.get(axis);
            currentPair.set(positive,field);
            neighbors.set(axis,currentPair);
        }
        else
        {
            if (positive)
                neighbors.set(axis,new FieldPair(field,null));
            else
                neighbors.set(axis,new FieldPair(null,field));
        }

        if (!singleLink)
            field.link(this,axis,!positive,true);
    }

    public LinkedList<Field> explore(int axis, boolean positive, int hardCap)
    {
        Field next = neighbors.get(axis).get(positive);
        LinkedList<Field> explored;

        if (next == null || hardCap == 0) // ha negatív hard cappel hívod, akkor nincs "hardCap"
            explored = new LinkedList<>();
        else
            explored = next.explore(axis, positive, hardCap - 1);

        explored.add(this);
        return explored;
    }
    /*public void deepExplore(ArrayList<Field> explored, int maxDimensions)
    {
        // veszélyesen mohó algoritmus
        // potential ai upgrade
        if (explored.contains(this))
            return;

        if (gameObjects.size() > 0)
            explored.add(this);

        if (isSolid() && explored.size() != 1)
            return;


        for (int i = 0; i < maxDimensions; i++)
        {
            Field nextp = neighbors.get(i).get(true);
            Field nextn = neighbors.get(i).get(false);
            if (nextn != null)
                nextn.deepExplore(explored,maxDimensions);

            if (nextp != null)
                nextp.deepExplore(explored,maxDimensions);
        }
    }*/
    public void draw(Graphics g, int x, int y, int xAxis, int yAxis)
    {
        synchronized (gameObjects)
        {
            for (GameObject go : gameObjects)
            {
                if (go != null)
                    go.draw(g, x, y);
            }
        }
        g.setColor(Color.orange);
        if (onFire)
            g.fillRect(x, y, 32, 32);

        FieldPair xAxisNeighbors = null;
        FieldPair yAxisNeighbors = null;
        if (yAxis < neighbors.size())
            yAxisNeighbors = neighbors.get(yAxis);
        if (xAxis < neighbors.size())
            xAxisNeighbors = neighbors.get(xAxis);

        if (xAxisNeighbors == null)
        {
            g.fillRect(x + 27, y, 5, 32);
            g.fillRect(x, y, 5, 32);
        }
        else
        {
            if (xAxisNeighbors.get(true) == null)
                g.fillRect(x + 27, y, 5, 32);
            if (xAxisNeighbors.get(false) == null)
                g.fillRect(x, y, 5, 32);
        }

        if (yAxisNeighbors == null)
        {
            g.fillRect(x, y, 32, 5);
            g.fillRect(x, y + 27, 32, 5);
        }
        else
        {
            if (yAxisNeighbors.get(true) == null)
                g.fillRect(x, y, 32, 5);
            if (yAxisNeighbors.get(false) == null)
                g.fillRect(x, y + 27, 32, 5);
        }
    }

    public void initiateExplosion(int dimension, int hardCap,Bomb source)
    {
        explode(0, true, 0,source);

        for (int i = 0; i < dimension; i++)
        {
            if (i < neighbors.size())
            {
                if (neighbors.get(i).get(true) != null)
                    neighbors.get(i).get(true).explode(i, true, hardCap,source);
                if (neighbors.get(i).get(false) != null)
                    neighbors.get(i).get(false).explode(i, false, hardCap,source);
            }
        }
    }
    public void explode(int axis, boolean positive, int hardCap,Bomb source)
    {
        boolean blocked = false;
        onFire = true;
        ArrayList<GameObject> mirror = new ArrayList<>();

        for (GameObject go : gameObjects)
        {
            if (go != source)
            {
                blocked = !go.redirectExplosion();
                GameObject exploded = go.explode();
                if ((go.getSolid() && blocked) && exploded == go)
                    onFire = false;
                mirror.add(exploded);
            }
        }

        gameObjects = mirror;
        gameObjects.removeIf(Objects::isNull);

        if (hardCap > 0 && !blocked)
        {
            Field next = neighbors.get(axis).get(positive);
            if (next != null)
                next.explode(axis,positive,hardCap-1,source);
        }
    }

    public boolean spawnPoint()
    {
        int freeAxis = 0;
        for (FieldPair fp: neighbors)
        {
            GameObject positive = null;
            GameObject negative = null;

            if (fp.get(true) != null && fp.get(true).gameObjects.size() > 0)
                positive = fp.get(true).gameObjects.get(0);

            if (fp.get(false) != null && fp.get(false).gameObjects.size() > 0)
                negative = fp.get(false).gameObjects.get(0);

            if (positive != null && negative != null)
                if ((positive.explode() != positive || negative.explode() != negative))
                    freeAxis++;
        }
        if (gameObjects.size() > 0 && gameObjects.get(0).explode() == gameObjects.get(0))
            freeAxis = 0;

        if (freeAxis > 1)
        {
            removeGameObjects();
            for (FieldPair fp: neighbors)
            {
                Field positive = null;
                Field negative = null;

                if (fp.get(true) != null && fp.get(true).gameObjects.size() > 0)
                    positive = fp.get(true);

                if (fp.get(false) != null && fp.get(false).gameObjects.size() > 0)
                    negative = fp.get(false);

                if (positive.gameObjects.get(0).explode() != positive.gameObjects.get(0))
                    positive.removeGameObjects();
                if( negative.gameObjects.get(0).explode() != negative.gameObjects.get(0))
                    negative.removeGameObjects();


            }
            return true;
        }
        return false;
    }

    public void initiateExtinguishment(int axis,int hardCap)
    {
        if (axis < neighbors.size())
        {
            extinguish(axis, true, hardCap);
            if (neighbors.get(axis).get(false) != null)
                neighbors.get(axis).get(false).extinguish(axis, false, hardCap);
        }
    }
    public void extinguish(int axis,boolean positive,int hardCap)
    {
        boolean blocked = false;
        onFire = false;

        for (GameObject go : gameObjects)
        {
            blocked = !go.redirectExplosion();
        }

        if (hardCap > 0 && !blocked)
        {
            Field next = neighbors.get(axis).get(positive);
            if (next != null)
                next.extinguish(axis,positive,hardCap-1);
        }
    }

    public boolean onFire()
    {
        return onFire;
    }

    public boolean inDanger(int dimension)
    {
        for (GameObject go : gameObjects)
        {
            if (go.getPriority() > 0 && go.getType() == 2)
                return true;
        }
        for (int i = 0; i < dimension; i++)
        {
            if (i < neighbors.size())
            {
                if (getNeighbor(i,true) != null)
                    if (getNeighbor(i,true).scanDanger(i,true) > 0)
                        return true;

                if (getNeighbor(i,false) != null)
                    if (getNeighbor(i,false).scanDanger(i,false) > 0)
                        return true;
            }
        }
        return false;
    }

    public boolean onlyOneFreeNeighbor(int dimension)
    {
        int free = 0;
        for (int i = 0; i < dimension; i++)
        {
            if (i < neighbors.size())
            {
                if (neighbors.get(i).get(true).gameObjects.size() == 0)
                    free++;
                else
                    if (!neighbors.get(i).get(true).gameObjects.get(0).getSolid())
                        free--;
                if (neighbors.get(i).get(false).gameObjects.size() == 0)
                    free++;
                else
                    if (!neighbors.get(i).get(false).gameObjects.get(0).getSolid())
                        free--;
            }
        }
        return free == 1;
    }

    public int scanDanger(int axis, boolean positive)
    {
        boolean blocked = false;
        int danger = 0;

        for (GameObject go : gameObjects)
        {
            blocked = go.getSolid();
            if (go.getPriority() > danger && go.getType() == 2)
                danger = go.getPriority();
        }

        if (!blocked && danger <= 0)
        {
            Field next = neighbors.get(axis).get(positive);
            if (next != null)
                return next.scanDanger(axis, positive) - 1;
        }
        return danger-1;
    }

}
