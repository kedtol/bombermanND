package game.internal.network;

import game.internal.Game;
import game.internal.component.Bomb;
import game.internal.component.Color;
import game.internal.component.Field;
import game.internal.component.FieldPair;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static game.internal.network.NetworkPacketType.*;

public class Server implements NetworkInterface
{
    private final List<Socket> clients = Collections.synchronizedList(new ArrayList<>());
    private final ArrayList<NetworkPlayer> players = new ArrayList<>();
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private List<Thread> receiveThreads = new ArrayList<>();
    private Game game = null;
    private boolean dead = false;

    private int aiCount;

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
            receivePackets(); // RECEIVE THREADS
        }
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
        for (int i = 0; i < clients.size(); i++)
        {
            receivePacketsFromClient(i);
        }
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
                                        sendPacket(client,new NetworkPacket(CLIENT_INTRODUCE,true,null));
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

                oos.writeObject(new NetworkPacket(CLIENT_RECEIVE_STRING, "Hello client_id_" + i));

                i++;
            }
        }
    }

    public void sendPacket(Socket socket, NetworkPacket packet)
    {
        packet.server = true;
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
                                //System.out.println("SERVER RECEIVE THREAD" + id + " ALIVE");
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
                                        sendPacket(null, new NetworkPacket(CLIENT_UPDATE_PLAYERLIST, players));
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
        NetworkPlayer np;
        if (!packet.server)
        {
            switch (packet.type)
            {
                case SERVER_RECEIVE_STRING: // sending a string
                    System.out.println("Server: the client"+packet.source+" said: "+ packet.content);
                break;

                case SERVER_DISCONNECT_CLIENT: // disconnecting
                    killClient(packet.source);
                    np = players.get(packet.source);
                    players.remove(np);
                    sendPacket(null,new NetworkPacket(CLIENT_UPDATE_PLAYERLIST,players));
                    if (game != null)
                        kill();
                break;

                case SERVER_ACCEPT_NEW_CLIENT: // joining introduction
                    np = (NetworkPlayer) packet.content;
                    np.setId(packet.source);
                    Color uc = getUnoccupiedColor();
                    if (uc != null)
                    {
                        np.setColor(uc);
                        sendPacket(clients.get(packet.source), new NetworkPacket(CLIENT_CHANGE_COLOR, null));
                    }
                    players.add(np);
                    sendPacket(null,new NetworkPacket(CLIENT_UPDATE_PLAYERLIST,players));
                break;

                case SERVER_REQUESTED_COLOR_CHANGE: // color change request
                    np = players.get(packet.source);
                    uc = getUnoccupiedColor();
                    if (uc != null)
                    {
                        np.setColor(uc);
                        sendPacket(clients.get(packet.source), new NetworkPacket(CLIENT_CHANGE_COLOR, uc));
                        sendPacket(null, new NetworkPacket(CLIENT_UPDATE_PLAYERLIST,  players));
                    }
                break;

                case SERVER_REQUESTED_MOVEMENT: // movement request
                    Pair<Integer, Pair<Integer,Boolean>> payload = (Pair<Integer, Pair<Integer,Boolean>>) packet.content;
                    for (int i = 0; i < clients.size(); i++)
                        if (i != packet.source)
                            sendPacket(clients.get(i),new NetworkPacket(CLIENT_MOVE,payload));
                    game.entityReceiveMovement(payload.left,payload.right.left,payload.right.right);
                break;

                case SERVER_REQUESTED_BOMB: // bomb place request
                    UUID payload_1 = (UUID) packet.content;

                    Bomb b = game.receiveBomb(payload_1,null);
                    if (b != null)
                    {
                        Pair<UUID,UUID> payload_1_1 = new Pair<>();
                        payload_1_1.left = payload_1;
                        payload_1_1.right = b.networkID;
                        sendPacket(null, new NetworkPacket(CLIENT_PLACE_BOMB, payload_1_1));
                    }
                break;
            }
        }
    }

    public void setGame(Game g)
    {
        game = g;
    }

    private Color getUnoccupiedColor()
    {
        Set<Color> occupiedColors = new HashSet<>();
        occupiedColors.add(Color.AI);
        for (NetworkPlayer ne : players)
        {
            occupiedColors.add(ne.getColor());
        }
        Random r = new Random();
        int len = Arrays.stream(Color.values()).filter(c->!occupiedColors.contains(c)).toList().size();
        Optional<Color> choosen = Arrays.stream(Color.values()).filter(c->!occupiedColors.contains(c)).skip(r.nextInt(len)).findFirst();
        return choosen.orElse(null);
    }

    public void spawnPlayers()
    {

        synchronized (clients)
        {
            for (int i = 0; i < aiCount; i++)
            {
                NetworkPlayer np = new NetworkPlayer(Color.AI,"AI",players.size(),true);
                players.add(np);
            }
            sendPacket(null,new NetworkPacket(CLIENT_UPDATE_PLAYERLIST,players));

            for (int i = 0; i < clients.size(); i++)
            {
                NetworkPlayer np = players.get(i);
                sendPacket(clients.get(i),new NetworkPacket(CLIENT_SPAWN_PLAYERS,np));
            }


        }
    }

    public void setAiCount(int cc)
    {
        aiCount = cc;
    }
}
