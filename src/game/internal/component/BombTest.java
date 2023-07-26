package game.internal.component;

import game.internal.Game;
import org.junit.Test;

import static org.junit.Assert.*;

public class BombTest
{

    @Test
    public void explode()
    {
        Field f = new Field();
        Game g = new Game();
        g.generateMap(10,10);
        Bomb b = new Bomb(null,"b",f,g,null,0);
        f.acceptGameObject(b,0,false);
        b.explode();
        assertTrue(f.onFire());

    }

}