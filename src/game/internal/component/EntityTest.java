package game.internal.component;

import org.junit.Test;

import static org.junit.Assert.*;


public class EntityTest
{

    @Test
    public void move()
    {
        Field field = new Field();
        Field field2 = new Field();
        field.link(field2,0,true,false);

        Player testp = new Player(null,"null",field,null,null);
        testp.move(0,true);
        assertEquals(testp.field,field2);
    }

    @Test
    public void moveThroughSolid()
    {
        Field field = new Field();
        Field field2 = new Field();
        field2.acceptGameObject(new Wall(null,"fal vagyok",true),0,false);
        field.link(field2,0,true,false);

        Player testp = new Player(null,"null",field,null,null);
        testp.move(0,true);
        assertNotEquals(testp.field,field2);
    }
}