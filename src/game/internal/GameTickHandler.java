package game.internal;

public class GameTickHandler
{
    private static int id = 0;

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
                    }
                    catch (InterruptedException ignore)
                    {
                        break;
                    }
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
        id++;

    }

}
