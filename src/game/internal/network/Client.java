package game.internal.network;

import game.internal.window.LobbyPanel;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class Client implements NetworkInterface
{
    private Socket server;
    private NetworkPlayer player;
    private Thread receiveThread = null;
    private LobbyPanel lobby = null;

    public Client(String serverIp, String serverPort, NetworkPlayer player) throws IOException
    {
        server = new Socket(serverIp,Integer.parseInt(serverPort));
        System.out.println("Client: Joined!");
        this.player = player;
    }

    @Override
    public void mainLoop()
    {
        receivePackets();
    }

    @Override
    public void kill()
    {
        try
        {
            sendPacket(new NetworkPacket(1,false,null));

            if (server != null)
            {
                server.close();
            }
            server = null;

            if (receiveThread != null)
                receiveThread.interrupt();

            receiveThread = null;
            System.out.println("Client dead");
        }
        catch (IOException e)
        {
            System.out.println("Failed to kill Network process (Client)");
            e.printStackTrace();
        }

    }

    public void receivePackets()
    {
        if (receiveThread == null && server != null)
        {
            receiveThread = new Thread("NetworkClientReceive")
            {
                @Override
                public void run()
                {
                    while (true)
                    {
                        try
                        {
                            sleep(10);
                            if (server != null)
                            {
                                ObjectInputStream ois = new ObjectInputStream(server.getInputStream());
                                NetworkPacket packet = (NetworkPacket)ois.readObject();
                                packetInterpreter(packet);
                            }

                        }
                        catch (InterruptedException | IOException ignore)
                        {
                            System.out.println("Client: ReceiveThread interrupted!");
                            lobby.backToMenu();
                        }
                        catch (ClassNotFoundException e)
                        {
                            System.out.println("Wrong Packet type found");
                            throw new RuntimeException(e);
                        }
                    }
                }
            };
            receiveThread.setDaemon(true);
            receiveThread.start();
        }
    }

    public void packetInterpreter(NetworkPacket packet)
    {
        if (packet.server)
        {
            switch (packet.id)
            {
                case 0: // sending a string
                    System.out.println("Client: the server said: "+ packet.content);
                break;

                case 1: // please introduce yourself
                    sendPacket(new NetworkPacket(2,false,player));
                break;

                case 2: // please change color
                    player.setColor((Color) packet.content);
                break;

                case 3: // update lobby playerlist
                    lobby.updatePlayers((ArrayList<NetworkPlayer>) packet.content);
                break;

            }
        }
    }

    public void sendPacket(NetworkPacket packet)
    {
        try
        {
            OutputStream os = server.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);

            oos.writeObject(packet);
            oos.flush();
        }
        catch (IOException e)
        {

        }
    }

    public void setLobby(LobbyPanel lobbyPanel)
    {
        this.lobby = lobbyPanel;
    }


}


