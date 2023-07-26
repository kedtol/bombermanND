package game.internal.network;

import game.internal.Game;
import game.internal.GameTickHandler;

import java.io.PrintWriter;
import java.util.ArrayList;

public class NetworkThreadHandler
{
    private static final ArrayList<NetworkInterface> networkInterfaces = new ArrayList<>();
    private static Thread thread;
    public static void loop()
    {
        System.out.println("NETWORK THREAD STARTED");

        thread = new Thread("NetworkTick")
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        sleep(1);
                        for (NetworkInterface ne: networkInterfaces)
                            ne.mainLoop();
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

    public static void addNetworkInterface(NetworkInterface n)
    {
        networkInterfaces.add(n);
    }

    public static void killNetworkInterfaces()
    {
        thread.interrupt();

        for (NetworkInterface ne : networkInterfaces)
        {
            ne.kill();
        }

        networkInterfaces.clear();

    }
}
