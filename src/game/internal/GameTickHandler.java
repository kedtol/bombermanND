package game.internal;

public class GameTickHandler
{
    private static Game game;

    public static void loop(Game g)
    {
        game = g;
        Thread thread = new Thread("GameTick")
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
    }

    public static void setGame(Game g)
    {
        game = g;
    }

}
