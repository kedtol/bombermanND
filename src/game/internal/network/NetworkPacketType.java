package game.internal.network;

public enum NetworkPacketType
{
    CLIENT_INTRODUCE,
    CLIENT_RECEIVE_STRING,
    CLIENT_CHANGE_COLOR,
    CLIENT_UPDATE_PLAYERLIST,
    CLIENT_START_GAME,
    CLIENT_SPAWN_PLAYERS,
    CLIENT_MOVE,
    CLIENT_PLACE_BOMB,
    CLIENT_UPDATE_ENTITYLIST, // unused -> casting corrupted (something something serialize)
    CLIENT_UPDATE_GAME, //unused -> corrupts the socket
    CLIENT_PLACE_POWERUP,
    CLIENT_KILL_ENTITY,
    SERVER_RECEIVE_STRING,
    SERVER_DISCONNECT_CLIENT,
    SERVER_ACCEPT_NEW_CLIENT,
    SERVER_REQUESTED_COLOR_CHANGE,
    SERVER_REQUESTED_MOVEMENT,
    SERVER_REQUESTED_BOMB;

}