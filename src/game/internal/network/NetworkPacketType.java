package game.internal.network;

public enum NetworkPacketType
{
    CLIENT_INTRODUCE,
    CLIENT_RECEIVE_STRING,
    CLIENT_CHANGE_COLOR,
    CLIENT_UPDATE_PLAYERLIST,
    CLIENT_START_GAME, // deprecated im using client_generate_game now
    CLIENT_GENERATE_GAME, // sends the seed
    CLIENT_SPAWN_PLAYERS, // spawns the whole np list
    CLIENT_MOVE, // uses entity id -> deprecated
    CLIENT_MOVE_ENTITY, // moves the entity bearing the given networkID
    CLIENT_PLACE_BOMB, // UUID for player UUID for bomb
    CLIENT_UPDATE_ENTITYLIST, // unused -> casting corrupted (something something serialize)
    CLIENT_UPDATE_GAME, //unused -> corrupts the socket
    CLIENT_PLACE_POWERUP, // deprecated -> im using seeds now
    CLIENT_KILL_ENTITY, // kills the entity bearing the given networkID
    CLIENT_KICK_BOMB, // bomb with given UUID receives a kick (server only)
    CLIENT_EXPLODE_BOMB, // NO NEED -> KILL_ENTITY does this better
    SERVER_RECEIVE_STRING,
    SERVER_DISCONNECT_CLIENT,
    SERVER_ACCEPT_NEW_CLIENT,
    SERVER_REQUESTED_COLOR_CHANGE,
    SERVER_REQUESTED_MOVEMENT, // the given networkPlayer wants to move
    SERVER_REQUESTED_BOMB, // player with UUID wants to place a bomb

}
