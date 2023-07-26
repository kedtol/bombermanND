package game.internal.network;

import game.internal.window.LobbyPanel;

import java.io.IOException;

public interface NetworkInterface
{
    void mainLoop();
    void kill();

    void receivePackets();

    void packetInterpreter(NetworkPacket packet);

}
