package game.internal.network;

import game.internal.Game;
import game.internal.component.*;
import game.internal.window.GamePanel;
import game.internal.window.LobbyPanel;
import game.internal.window.MainFrame;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static game.internal.network.NetworkPacketType.*;

public class Client implements NetworkInterface
{
    private Socket server;
    private NetworkPlayer player;
    private Thread receiveThread = null;
    private LobbyPanel lobby = null;
    private MainFrame mf = null;
    private Game game = null;
    private ArrayList<NetworkPlayer> players = new ArrayList<>();

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
            sendPacket(new NetworkPacket(SERVER_DISCONNECT_CLIENT,false,null));

            if (server != null)
            {
                server.close();
                server = null;
            }

            if (game != null)
                game.kill();

            if (receiveThread != null)
            {
                //receiveThread.interrupt(); // No need to interrupt the thread - the main thread already did that
                receiveThread = null;
            }

            System.out.println("Client: client killed!");
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
            switch (packet.type)
            {
                case CLIENT_RECEIVE_STRING: // sending a string
                    System.out.println("Client: the server said: "+ packet.content);
                break;

                case CLIENT_INTRODUCE: // please introduce yourself
                    sendPacket(new NetworkPacket(SERVER_ACCEPT_NEW_CLIENT,false,player));
                break;

                case CLIENT_CHANGE_COLOR: // please change color
                    player.setColor((Color) packet.content);
                break;

                case CLIENT_UPDATE_PLAYERLIST: // update lobby playerlist
                    ArrayList<NetworkPlayer> nps = (ArrayList<NetworkPlayer>) packet.content;
                    players = nps;
                    lobby.updatePlayers(nps);
                break;

                case CLIENT_START_GAME: // game starts - deprecated
                    /*lobby.joinGame((Game) packet.content);
                    mf = lobby.getMainFrame();*/
                break;

                case CLIENT_GENERATE_GAME: // game starts
                    Pair<Integer,Pair<Integer,Long>> payload = (Pair<Integer, Pair<Integer, Long>>) packet.content;
                    lobby.generateGame(payload.left,payload.right.left,payload.right.right);
                    mf = lobby.getMainFrame();
                break;

                case CLIENT_SPAWN_PLAYERS: // spawn player
                    player = (NetworkPlayer) packet.content;
                    game.spawnPlayers(players);
                    //game.assumeControl();
                break;

                case CLIENT_MOVE: // spawn player
                    // UUID, axis, positive
                    Pair<UUID, Pair<Integer,Boolean>> payload_0 = (Pair<UUID, Pair<Integer,Boolean>>) packet.content;
                    game.entityReceiveMovement(payload_0.left,payload_0.right.left,payload_0.right.right);
                break;

                case CLIENT_PLACE_BOMB:
                    Pair<UUID,UUID> payload_1 = (Pair<UUID, UUID>) packet.content;
                    game.receiveBomb(payload_1.left,payload_1.right);
                break;

                case CLIENT_KILL_ENTITY:
                    UUID np = (UUID) packet.content;
                    game.killEntity(np);
                break;

                case CLIENT_EXPLODE_BOMB:
                    UUID np1 = (UUID) packet.content;
                    game.explodeEntity(np1);
                break;

                case CLIENT_KICK_BOMB:
                    Pair<UUID,Pair<Integer,Boolean>> payload_3 = (Pair<UUID,Pair<Integer,Boolean>>)packet.content;
                    game.kickBomb(payload_3.left,payload_3.right.left,payload_3.right.right);
                break;
            }

        }
    }

    public void sendPacket(NetworkPacket packet)
    {
        packet.server = false;
        if (server != null)
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
        else
        {
            // packet for a null server?
        }
    }

    public void setLobby(LobbyPanel lobbyPanel)
    {
        this.lobby = lobbyPanel;
    }

    public void setGame(Game game)
    {
        this.game = game;
    }

    public NetworkPlayer getPlayer()
    {
        return player;
    }
}


