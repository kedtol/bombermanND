package game.internal.network;

import game.internal.window.LobbyPanel;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server implements NetworkInterface
{
    private final List<Socket> clients = Collections.synchronizedList(new ArrayList<>());
    private final ArrayList<NetworkPlayer> players = new ArrayList<>();
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private List<Thread> receiveThreads = new ArrayList<>();
    private List<Color> colors = List.of(Color.red,Color.blue,Color.gray,Color.green,Color.pink,Color.orange,Color.yellow);


    private boolean dead = false;

    public Server() throws IOException
    {
        serverSocket = new ServerSocket(29869);

    }


    @Override
    public void mainLoop() // NETWORK THREAD
    {
        if (!dead)
        {
            lookForClients(); // ACCEPT THREAD
            receivePackets(); // RECEIVE THREAD


            for (int i = 0; i < clients.size(); i++)
            {
                receivePacketsFromClient(i);
            }
        }
        //System.out.println("OPTIMIZED?");
        /*try
        {
          //  checkClients();
            //greetClients();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }*/
    }

    @Override
    public void kill()
    {
        try
        {
            dead = true;
            for (Thread t : receiveThreads)
            {
                if (t != null)
                {
                    t.interrupt();
                    System.out.println("DOWN");
                }
            }
            receiveThreads = null;


            if (acceptThread != null)
                acceptThread.interrupt();

            acceptThread = null;

            for (Socket c : clients)
            {
                if (c != null)
                    c.close();
            }

            if (serverSocket != null)
                serverSocket.close();

            clients.clear();
            serverSocket = null;
        }
        catch (IOException e)
        {
            System.out.println("Failed to kill Network process (SERVER or clients)");
            e.printStackTrace();
        }
    }

    @Override
    public void receivePackets()
    {

    }

    private void lookForClients()
    {
        if (acceptThread == null)
        {
            acceptThread = new Thread("NetworkServerAccept")
            {
                @Override
                public void run()
                {
                    while (true)
                    {
                        System.out.println("Server accept thread alive");
                        try
                        {
                            sleep(50);
                            if (serverSocket != null)
                            {
                                Socket client = serverSocket.accept();


                                if (client != null)
                                {
                                    //receiveThread.interrupt();
                                    //receiveThread = null;
                                    synchronized (clients)
                                    {
                                        sendPacket(client,new NetworkPacket(1,true,null));
                                        clients.add(client);
                                        clients.notify();

                                    }

                                    receiveThreads.add(null);
                                }
                                System.out.println("Server: A client joined!");

                            }
                        }
                        catch (InterruptedException | IOException ignore)
                        {
                            System.out.println("Server: AcceptThread interrupted!");
                            break;
                        }
                    }
                    System.out.println("huh!");
                }
            };

            acceptThread.setDaemon(true);
            acceptThread.start();
        }
    }

    private void checkClients() throws IOException
    {
        for (Socket c : clients)
        {
            if (c.isClosed() || !c.isBound())
            {
                clients.remove(c);
            }

        }
    }

    private void greetClients() throws IOException
    {
        int i = 0;
        for (Socket c : clients)
        {
            if (!c.isClosed() && c.isBound())
            {
                OutputStream os = c.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);

                oos.writeObject(new NetworkPacket(0, true, "Hello client_id_" + i));

                i++;
            }
        }
    }

    private void sendPacket(Socket socket, NetworkPacket packet)
    {
        List<Socket> targets = new ArrayList<>();
        if (socket == null)
            targets.addAll(clients);
        else
            targets.add(socket);

        for (Socket s : targets)
        {
            if (!s.isClosed() && s.isBound())
            {
                try
                {
                    OutputStream os = s.getOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(os);

                    oos.writeObject(packet);
                    oos.flush();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

    }

    public void receivePacketsFromClient(int id)
    {
        if (receiveThreads != null)
        {
            if (receiveThreads.size() > id && receiveThreads.get(id) == null)
            {
                if (clients.get(id) != null)
                {
                    receiveThreads.set(id, new Thread("NetworkServerReceive" + id)
                    {
                        @Override
                        public void run()
                        {
                            while (true)
                            {
                                System.out.println("SERVER RECEIVE THREAD" + id + " ALIVE");
                                try
                                {
                                    sleep(10);

                                    Socket selc = clients.get(id);
                                    if (selc != null)
                                    {
                                        ObjectInputStream ois = new ObjectInputStream(selc.getInputStream());
                                        NetworkPacket packet = (NetworkPacket) ois.readObject();
                                        packet.source = id;
                                        packetInterpreter(packet);
                                    }
                                }
                                catch (IOException | ClassNotFoundException e)
                                {
                                    System.out.println("Server: packet read error!");
                                    if (receiveThreads != null)
                                    {
                                        killClient(id);
                                        sendPacket(null, new NetworkPacket(3, true, players));
                                    }
                                    this.interrupt();
                                    e.printStackTrace();
                                }
                                catch (InterruptedException e)
                                {
                                    System.out.println("Server: ReceiveThread" + id + " interrupted!");
                                    break;
                                }
                            }
                        }

                    });
                    receiveThreads.get(id).setDaemon(true);
                    receiveThreads.get(id).start();
                }
            }
        }

    }

    public void killClient(int id)
    {
        synchronized (clients)
        {
            Socket c = clients.get(id);
            clients.remove(c);
            receiveThreads.get(id).interrupt();
            receiveThreads.set(id,null);
            System.out.println("killed connection: "+id);
        }
    }

    @Override
    public void packetInterpreter(NetworkPacket packet)
    {
        if (!packet.server)
        {
            switch (packet.id)
            {
                case 0: // sending a string
                    System.out.println("Server: the client"+packet.source+" said: "+ packet.content);
                break;

                case 1: // disconnecting
                    killClient(packet.source);
                    NetworkPlayer np = players.get(packet.source);
                    players.remove(np);
                    sendPacket(null,new NetworkPacket(3,true,players));
                break;

                case 2: // joining introduction
                    np = (NetworkPlayer) packet.content;
                    players.add(np);
                    sendPacket(null,new NetworkPacket(3,true,players));
                break;

                case 3: // color change request
                    np = players.get(packet.source);
                    int id = colors.indexOf(np.getColor());
                    if (id+1 == colors.size())
                        id = 0;
                    else
                        id++;
                    np.setColor(colors.get(id));
                    sendPacket(null,new NetworkPacket(3,true,players));
                break;


            }
        }
    }

}
