package game.internal;

import java.util.ArrayList;
import java.util.List;

public class GameTickHandler
{
    private static int id = 0;
    private static List<Thread> gameTicks = new ArrayList<>();

    public static void loop(Game g)
    {
        Game game = g;
        Thread thread = new Thread("GameTick"+id)
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        sleep(1);
                        if (game != null)
                            game.mainLoop();
                        else
                            break;

                        if (game.isKilled())
                        {
                            System.out.println("GameTickHandler: interrupted thread: " + getName());
                            break;
                        }

                    }
                    catch (InterruptedException ignore)
                    {
                        break;
                    }
                }
            }
        };
        gameTicks.add(thread);
        thread.setDaemon(true);
        thread.start();
        id++;

    }

    public static void stop(String name)
    {
        for (Thread t : gameTicks)
        {
            if (t.getName().equals(name))
            {
                t.interrupt();
                System.out.println("GameTickHandler: interrupted thread: " + name);
            }
        }
    }

}
