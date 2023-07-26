package game.internal.component;

import org.junit.*;

import java.util.LinkedList;

import static org.junit.Assert.*;

public class FieldTest
{
    private final Field field = new Field();


    @Test
    public void acceptGameObject()
    {
        Wall w = new Wall(null,"én tényleg egy fal vagyok",true);
        assertTrue(field.acceptGameObject(w,0,false));
    }

    @Test
    public void acceptGameObjectSolid()
    {
        Wall w = new Wall(null,"én tényleg egy fal vagyok",true);
        Wall w2 = new Wall(null,"én egy másik fal vagyok",true);
        assertEquals(field.getGameObjects().size(),0);
        assertTrue(field.acceptGameObject(w,0,false));
        assertFalse(field.acceptGameObject(w2,0,false));
    }

    @Test
    public void link()
    {
        Field field2 = new Field();
        field.link(field2,0,true,false);

        Field field3 = new Field();
        field.link(field3,1,true,false);

        Field field4 = new Field();
        field.link(field4,2,true,true);

        assertEquals(field.getNeighbor(0,true),field2); //axis 0
        assertEquals(field2.getNeighbor(0,false),field); // both directions

        assertEquals(field.getNeighbor(1,true),field3); // axis 1
        assertEquals(field3.getNeighbor(1,false),field); // both directions

        assertEquals(field.getNeighbor(2,true),field4); // axis 2 non-recursive
        assertNull(field4.getNeighbor(2, false));   // only one direction
    }

    @Test
    public void explore()
    {
        Field f2 = new Field();
        Field f3 = new Field();

        field.link(f2,0,true,false);
        f2.link(f3,0,true,false);
        LinkedList<Field> exploration = field.explore(0,true,-1);
        assertEquals(exploration.size(),3);
        assertEquals(exploration.get(2),field);
        assertEquals(exploration.get(1),f2);
        assertEquals(exploration.get(0),f3);
    }

    @Test
    public void initiateExplosion()
    {
        Field f2 = new Field();
        Field f3 = new Field();

        field.link(f2,0,true,false);
        f2.link(f3,0,true,false);
        Bomb bomb = new Bomb(null,"bomb",field,null,null,10);
        field.initiateExplosion(1,10,bomb);
        assertTrue(field.onFire());
        assertTrue(f2.onFire());
        assertTrue(f3.onFire());
    }

    @Test
    public void initiateExtinguishment()
    {
        Field f2 = new Field();
        Field f3 = new Field();

        field.link(f2,0,true,false);
        f2.link(f3,0,true,false);
        Bomb bomb = new Bomb(null,"bomb",field,null,null,10);
        field.initiateExplosion(1,10,bomb);
        field.initiateExtinguishment(0,10);
        assertFalse(field.onFire());
        assertFalse(f2.onFire());
        assertFalse(f3.onFire());
    }

    @Test
    public void explode()
    {
        Field f2 = new Field();
        Field f3 = new Field();

        field.link(f2,0,true,false);
        f2.link(f3,0,true,false);
        Bomb bomb = new Bomb(null,"bomb",field,null,null,10);
        f2.explode(0,true,2,bomb);
        assertTrue(f2.onFire());
        assertTrue(f3.onFire());
        assertFalse(field.onFire());
    }

    @Test
    public void placeBomb()
    {
        Bomb bomb = new Bomb(null,"bomb",field,null,null,10);
        field.placeBomb(bomb);
        assertEquals(field.getGameObjects().get(0),bomb);
        field.removeGameObjects();
        field.acceptGameObject(new Wall(null,"fal",true),0,false);
        field.getGameObjects().add(new Wall(null,"fal2",true));
        assertFalse(field.placeBomb(bomb)); // if there is two, then don't place

    }
}