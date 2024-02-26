package game.internal;

import game.internal.component.*;
import game.internal.component.Color;
import game.internal.network.*;
import game.io.Bind;
import game.io.InputHandler;
import game.io.ResourceLoader;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;
import java.util.stream.LongStream;

import static game.internal.network.NetworkPacketType.*;

public class Game implements Serializable
{
    private List<Entity> entities = Collections.synchronizedList(new ArrayList<>());
    private final ArrayList<Entity> newComers = new ArrayList<>();
    private Camera camera1 = null;
    private Camera camera2 = null;
    private ArrayList<Field> map = new ArrayList<>();
    private int dimensions;
    private boolean gameStarted = false;
    private transient ResourceLoader resourceLoader;
    private transient ArrayList<BufferedImage> powerUps;
    private transient ArrayList<String> powerUpKeys;
    private int players;
    private transient long seed = 0;
    private transient Server server = null;
    private transient Client client = null;
    private boolean killed = false;

    public Game()
    {
        softInit();
    }

    public void kill()
    {
        killed = true;
    }

    public boolean isKilled()
    {
        return killed;
    }

    public void softInit()
    {
        client = null;
        server = null;
        entities = Collections.synchronizedList(new ArrayList<>());
        resourceLoader = new ResourceLoader();
        InputHandler inputHandler = new InputHandler();
        inputHandler.bindSetup();
        resourceLoader.loadTextures();

        powerUps = new ArrayList<>();
        powerUpKeys = new ArrayList<>();

        camera1 = new Camera(null,6,280,300);

        powerUpKeys.add("void");
        for (int i = 1; i < 6; i++)
            powerUpKeys.add("powerup_"+i);

        for (String k: powerUpKeys)
            powerUps.add( resourceLoader.getImage(k));

    }

    public void save()
    {
        gameStarted = false;
        try {
            FileOutputStream f = new FileOutputStream("gamesave.dat");
            ObjectOutputStream out = new ObjectOutputStream(f);
            out.writeObject(map);
            out.writeObject(entities);
            for (Field field : map)
                    field.serializeNeighbors(out);
            out.writeObject(camera1);
            out.writeObject(camera2);
            out.writeObject(dimensions);
            out.close();
        }
        catch(IOException ex) { ex.printStackTrace(); }
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(map);
        oos.writeObject(entities);
        oos.writeObject(camera1);
        oos.writeObject(camera2);
        oos.writeObject(dimensions);
        for (Field field : map)
            field.serializeNeighbors(oos);

    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        softInit();
        ois.defaultReadObject();
        map = (ArrayList<Field>) ois.readObject();
        entities = (List<Entity>) ois.readObject();
        camera1 = (Camera) ois.readObject();
        camera2 = (Camera) ois.readObject();
        dimensions = (int) ois.readObject();
        for (Field field : map)
        {
            field.loadNeighbors(ois);
            field.getGameObjects().forEach(g-> g.setImage(resourceLoader));
        }
        //entities.forEach(e-> e.setGame(this));
        for (Entity e : entities)
        {
            e.setGame(this);
        }


    }

    public void load()
    {
        gameStarted = false;
        try { FileInputStream f =
                new FileInputStream("gamesave.dat");
            ObjectInputStream in =
                    new ObjectInputStream(f);
            map = (ArrayList<Field>) in.readObject();
            entities = (List<Entity>) in.readObject();

            for (Field field : map)
            {
                field.loadNeighbors(in);
                field.getGameObjects().forEach(g-> g.setImage(resourceLoader));
            }

            entities.forEach(e-> e.setGame(this));
            camera1 = (Camera) in.readObject();
            camera2 = (Camera) in.readObject();
            dimensions = (int) in.readObject();

            in.close();


        } catch(IOException | ClassNotFoundException ex) {
        ex.printStackTrace();
        }
    }

    public void generateMap(int w,int h)
    {
        map = new ArrayList<>();
        entities = new ArrayList<>();
        Field[][] mapArray = new Field[w][h];
        dimensions = 2;
        Random r = new Random(seed);
        //creating the main components
        for (int i = 0; i < w; i++)
        {
            for (int j = 0; j < h; j++)
            {
                mapArray[i][j] = new Field();

                int c = r.nextInt(200);
                int type = 0;

                if (c < 100)
                {

                    if (c < 6)
                        type = 1;
                    if (c >= 6 && c < 15)
                        type = 4;
                    if (c >= 15 && c < 45)
                        type = 2;
                    if (c >= 55)
                        type = 3;
                }


                if (i % 2 == 0 && j % 2 == 0)
                    if (r.nextInt(10) > 4)
                        mapArray[i][j].acceptGameObject(new Wall(resourceLoader.getImage("wall"),"wall",true),-1,false);
                    else
                        mapArray[i][j].acceptGameObject(new Wall(resourceLoader.getImage("wall_moss"),"wall_moss",true),-1,false);
                else
                    if (i == 0 || j == 0 || i == w-1 || j == h-1)
                        if (r.nextInt(10) > 4)
                            mapArray[i][j].acceptGameObject(new Wall(resourceLoader.getImage("wall"),"wall",true),-1,false);
                        else
                            mapArray[i][j].acceptGameObject(new Wall(resourceLoader.getImage("wall_moss"),"wall_moss",true),-1,false);
                    else
                        mapArray[i][j].acceptGameObject(new Box(resourceLoader.getImage("box"),"box",true,powerUps,powerUpKeys,type,mapArray[i][j]),-1,false);
                map.add(mapArray[i][j]);
            }
        }
        //linking the components
        for (int i = 0; i < w; i++)
        {
            for (int j = 0; j < h; j++)
            {
                // axis table (x->0,y->1) (negative->positive)
                // negative ||
                // positive \/
                if (j > 0)
                    mapArray[i][j].link(mapArray[i][j - 1], 1, true, false);
                if (j < h-1)
                    mapArray[i][j].link(mapArray[i][j+1], 1, false, false);
                if (i > 0)
                    mapArray[i][j].link(mapArray[i-1][j], 0, false, false);
                if (i < w-1)
                    mapArray[i][j].link(mapArray[i+1][j], 0, true, false);



            }

        }

        /*Random r = new Random();
        while (r.nextInt(3000)< 2980) //force map corruption
        {
            map[r.nextInt(w)][r.nextInt(h)].link(map[r.nextInt(w)][r.nextInt(h)], r.nextInt(2), r.nextBoolean(), true);
        }*/

    }

    public void generateMap3d(int x,int y, int z)
    {
        map = new ArrayList<>();
        entities = new ArrayList<>();
        dimensions = 3;
        Field[][][] mapArray = new Field[x][y][z];
        Random r = new Random(seed);

        int c = r.nextInt(200);
        int type = 0;

        if (c < 100)
        {

            if (c < 6)
                type = 1;
            if (c >= 6 && c < 15)
                type = 4;
            if (c >= 15 && c < 45)
                type = 2;
            if (c >= 55)
                type = 3;
        }

        //creating the main components
        for (int i = 0; i < x; i++)
        {
            for (int j = 0; j < y; j++)
            {
                for (int k = 0; k < z; k++)
                {

                    mapArray[i][j][k] = new Field();

                    if (i % 2 == 0 && j % 2 == 0 && k % 2 == 0)
                        if (r.nextInt(10) > 4)
                            mapArray[i][j][k].acceptGameObject(new Wall(resourceLoader.getImage("wall"),"wall", true),-1,false);
                        else
                            mapArray[i][j][k].acceptGameObject(new Wall(resourceLoader.getImage("wall_moss"),"wall_moss", true),-1,false);
                    else
                        if (i == 0 || j == 0 || k == 0 || i == x - 1 || j == y - 1 || k == z - 1)
                            if (r.nextInt(10) > 4)
                                mapArray[i][j][k].acceptGameObject(new Wall(resourceLoader.getImage("wall"),"wall", true),-1,false);
                            else
                                mapArray[i][j][k].acceptGameObject(new Wall(resourceLoader.getImage("wall_moss"),"wall_moss", true),-1,false);
                        else
                            mapArray[i][j][k].acceptGameObject(new Box(resourceLoader.getImage("box"),"box", true,powerUps,powerUpKeys,type,mapArray[i][j][k]),-1,false);
                    map.add(mapArray[i][j][k]);
                }
            }
        }
        //linking the components
        for (int i = 0; i < x; i++)
        {
            for (int j = 0; j < y; j++)
            {
                for (int k = 0; k < z; k++)
                {
                    // axis table (x->0,y->1) (negative->positive)
                    // negative ||
                    // positive \/

                    if (j > 0)
                        mapArray[i][j][k].link(mapArray[i][j - 1][k], 1, true, false);
                    if (j < x - 1)
                        mapArray[i][j][k].link(mapArray[i][j + 1][k], 1, false, false);
                    if (i > 0)
                        mapArray[i][j][k].link(mapArray[i - 1][j][k], 0, false, false);
                    if (i < y - 1)
                        mapArray[i][j][k].link(mapArray[i + 1][j][k], 0, true, false);
                    if (k > 0)
                        mapArray[i][j][k].link(mapArray[i][j][k - 1], 2, false, false);
                    if (k < z - 1)
                        mapArray[i][j][k].link(mapArray[i][j][k+ 1], 2, true, false);


                }

            }

        }

        /*Random r = new Random();
        while (r.nextInt(3000)< 2999) //force map corruption
        {
            mapArray[r.nextInt(x)][r.nextInt(y)][r.nextInt(z)].link(mapArray[r.nextInt(x)][r.nextInt(y)][r.nextInt(z)], r.nextInt(3), r.nextBoolean(), true);
        }*/

    }

    public void generateMap4d(int x,int y, int z,int w)
    {
        map = new ArrayList<>();
        entities = new ArrayList<>();
        Field[][][][] mapArray = new Field[x][y][z][w];
        dimensions = 4;
        Random r = new Random(seed);
        int c = r.nextInt(200);
        int type = 0;

        if (c < 100)
        {

            if (c < 6)
                type = 1;
            if (c >= 6 && c < 15)
                type = 4;
            if (c >= 15 && c < 45)
                type = 2;
            if (c >= 55)
                type = 3;
        }

        //creating the main components
        for (int i = 0; i < x; i++)
        {
            for (int j = 0; j < y; j++)
            {
                for (int k = 0; k < z; k++)
                {
                    for (int l = 0; l < w; l++)
                    {
                        mapArray[i][j][k][l] = new Field();

                        if (i % 2 == 0 && j % 2 == 0 && k % 2 == 0 && l % 2 == 0)
                            if (r.nextInt(10) > 4)
                                mapArray[i][j][k][l].acceptGameObject(new Wall(resourceLoader.getImage("wall"),"wall", true),-1,false);
                            else
                                mapArray[i][j][k][l].acceptGameObject(new Wall(resourceLoader.getImage("wall_moss"),"wall_moss", true),-1,false);
                        else
                            if (i == 0 || j == 0 || k == 0 || l == 0 || i == x - 1 || j == y - 1 || k == z - 1 || l == w - 1)
                                if (r.nextInt(10) > 4)
                                    mapArray[i][j][k][l].acceptGameObject(new Wall(resourceLoader.getImage("wall"),"wall", true),-1,false);
                                else
                                    mapArray[i][j][k][l].acceptGameObject(new Wall(resourceLoader.getImage("wall_moss"),"wall_moss", true),-1,false);
                            else
                                mapArray[i][j][k][l].acceptGameObject(new Box(resourceLoader.getImage("box"),"box", true,powerUps, powerUpKeys,type,mapArray[i][j][k][l]),-1,false);
                        map.add(mapArray[i][j][k][l]);
                    }
                }
            }
        }
        //linking the components
        for (int i = 0; i < x; i++)
        {
            for (int j = 0; j < y; j++)
            {
                for (int k = 0; k < z; k++)
                {
                    for (int l = 0; l < w; l++)
                    {

                        // axis table (x->0,y->1) (negative->positive)
                        // negative ||
                        // positive \/

                        if (j > 0)
                            mapArray[i][j][k][l].link(mapArray[i][j - 1][k][l], 1, true, false);
                        if (j < x - 1)
                            mapArray[i][j][k][l].link(mapArray[i][j + 1][k][l], 1, false, false);
                        if (i > 0)
                            mapArray[i][j][k][l].link(mapArray[i - 1][j][k][l], 0, false, false);
                        if (i < y - 1)
                            mapArray[i][j][k][l].link(mapArray[i + 1][j][k][l], 0, true, false);
                        if (k > 0)
                            mapArray[i][j][k][l].link(mapArray[i][j][k - 1][l], 2, false, false);
                        if (k < z - 1)
                            mapArray[i][j][k][l].link(mapArray[i][j][k + 1][l], 2, true, false);
                        if (l > 0)
                            mapArray[i][j][k][l].link(mapArray[i][j][k ][l- 1], 3, false, false);
                        if (l < w - 1)
                            mapArray[i][j][k][l].link(mapArray[i][j][k ][l+ 1], 3, true, false);


                    }
                }

            }

        }

       /* Random r = new Random();
        while (r.nextInt(3000)< 2999) //force map corruption
        {
            mapArray[r.nextInt(x)][r.nextInt(y)][r.nextInt(z)][r.nextInt(w)].link(mapArray[r.nextInt(x)][r.nextInt(y)][r.nextInt(z)][r.nextInt(w)], r.nextInt(3), r.nextBoolean(), true);
        }*/

    }

    public void mainLoop()
    {
        // több szekvenciaszál létezik
        // a bomba szekvenciaszáljáról nem törölhetem ki a bombermant, tehát mindenképp kell egy ALIVE tag.
        // a bomba száláról a bomberman eldönti, hogy halott, majd a saját szálán megöli magát.
        if (gameStarted)
        {

            int iter = 0;
            synchronized (entities)
            {

                Iterator<Entity> i = entities.listIterator();

                while(i.hasNext())
                {
                    Entity e = i.next();

                    if (e.live())
                    {
                        if (client == null)
                        {
                            //server.sendPacket(null, new NetworkPacket(CLIENT_KILL_ENTITY, e.networkID));
                        }

                        i.remove();
                    }

                    iter++;
                }


                //entities.removeIf(Entity::live);

                entities.addAll(newComers);
                newComers.clear();
            }

            //if (client == null && removed > 0)
             //   server.sendPacket(null,new NetworkPacket(CLIENT_UPDATE_GAME,this));

        }
    }

    public void start()
    {
    gameStarted = true;
}

    public int getDimensions()
    {
        return dimensions;
    }

    public void addPlayer(NetworkPlayer np,boolean controlled)
    {
        Random r = new Random(seed);
        int field = 0;
        boolean placement = false;
        while (!placement)
        {
            field = r.nextInt(map.size());
            placement = map.get(field).spawnPoint();
        }
        ArrayList<Bind> binds = null;

        if (controlled)
            binds = new ArrayList<>(Arrays.asList(Bind.UP, Bind.DOWN, Bind.LEFT, Bind.RIGHT, Bind.PLANT, Bind.ROTATE));

        Color c = np.getColor();
        Player player = new Player(resourceLoader.getImage(c.getName()), c.getName(), map.get(field), this,binds,np);

        if (controlled) // nagyon szeretlek (am nem fog lefutni)
            camera1 = new Camera(player,6,280,300);

        map.get(field).acceptGameObject(player,0,false);
        player.networkID = np.networkID;
        entities.add(player);
        players++;
    }

    public void addEnemy(NetworkPlayer ne)
    {
        Random r = new Random(seed);
        int field = 0;
        boolean placement = false;
        while (!placement)
        {
            field = r.nextInt(map.size());
            placement = map.get(field).spawnPoint();
        }
        Enemy enemy = new Enemy(resourceLoader.getImage("player_ai"),"player_ai",map.get(field),this);
        map.get(field).acceptGameObject(enemy,0,false);
        entities.add(enemy);
        if (ne != null)
            enemy.networkID = ne.networkID;
        players++;
    }

    public void drawCycle(Graphics g)
    {
        if (camera1 != null)
            camera1.draw(g);
        if (camera2 != null)
            camera2.draw(g);


        g.setColor(java.awt.Color.red);
        if (players <= 1)
            g.drawString("GAME OVER!",10,10);
    }

    public Bomb createBomb(Bomberman b, UUID BnetworkID)
    {
        if (b != null)
        {
            Bomb newBomb = new Bomb(resourceLoader.getImage("bomb"), "bomb", b.getField(), this, b, b.getBombSize());
            if (BnetworkID != null)
                newBomb.networkID = BnetworkID;

            if (b.getField().placeBomb(newBomb))
            {
                newComers.add(newBomb);
                return newBomb;
            }
        }

        return null;
    }

    public void playerDied()
    {
        players--;
    }

    public void setClient(Client client)
    {
        this.client = client;
        client.setGame(this);
    }
    public void setServer(Server server)
    {
        this.server = server;
        if (server != null)
            server.setGame(this);

    }

    public void entitySendMovement(Entity e,int axis,boolean positive)
    {
        Pair<Integer,Boolean> innerpayload = new Pair<>();
        innerpayload.left = axis;
        innerpayload.right = positive;
        Pair<UUID, Pair<Integer,Boolean>> payload = new Pair<>();
        payload.left = e.networkID;
        payload.right = innerpayload;
        if (client != null)
            client.sendPacket(new NetworkPacket(SERVER_REQUESTED_MOVEMENT,payload));
        else
            server.sendPacket(null,new NetworkPacket(CLIENT_MOVE,payload));
    }

    public void entityReceiveMovement(UUID networkEntity,int axis,boolean positive)
    {
        synchronized (entities)
        {
            for (Entity e : entities)
            {
                if (e.networkID.equals(networkEntity))
                    e.moveNetwork(axis, positive);
            }
        }
    }

    public void spawnPlayers(ArrayList<NetworkPlayer> nps)
    {
        for (NetworkPlayer ne : nps)
        {
            if (ne.getAI())
                addEnemy(ne);
            else
            {
                if (client != null && client.getPlayer().getId() == ne.getId())
                    addPlayer(ne, true);
                else
                    addPlayer(ne, false);
            }
        }
    }

    public void bombermanSendBomb(Bomberman b)
    {
        if (client != null)
        {
            client.sendPacket(new NetworkPacket(SERVER_REQUESTED_BOMB, b.networkID));
        }
        else
        {
            Bomb bomb = receiveBomb(b.networkID,null); // creating a new bomb
            if (bomb != null)
            {
                Pair<UUID,UUID> payload_1_1 = new Pair<>();
                payload_1_1.left = b.networkID;
                payload_1_1.right = bomb.networkID;
                server.sendPacket(null, new NetworkPacket(CLIENT_PLACE_BOMB, payload_1_1));
            }
            else
            {
                System.out.println("Couldn't create BOMB - could be a desync issue");
            }

        }

        //if (client != null) // full local bomb bag check -> possible vulnerability
        //{
            b.bombPlaced();
        //}
    }

    public void kickBomb(UUID BnetworkID, int axis, boolean positive)
    {
        synchronized (entities)
        {
            for (Entity e : entities)
            {
                if (e.networkID.equals(BnetworkID))
                    e.kick(axis,positive); // WHOA NOT INTENDED, hope it works
            }
        }
    }

    public Bomb receiveBomb(UUID BMnetworkID, UUID BnetworkID)
    {
        synchronized(entities)
        {
            for (Entity e : entities)
            {
                if (e.networkID.equals(BMnetworkID))
                    return createBomb((Bomberman) e,BnetworkID); // TODO: remove casting
            }
        }
        return null;
    }

    public Server getServer()
    {
        return server;
    }

    public void killEntity(UUID networkID)
    {
        synchronized (entities)
        {
            for (Entity e : entities)
            {
                if (e.networkID.equals(networkID))
                {
                    //if (server != null && client == null)
                   //     server.sendPacket(null,new NetworkPacket(CLIENT_KILL_ENTITY,networkID));
                    e.networkKill();
                }
            }
        }
    }

    public void explodeEntity(UUID networkID)
    {
        synchronized (entities)
        {
            for (Entity e : entities)
            {
                if (e.networkID.equals(networkID))
                    e.explode();
            }
        }
    }

    public void setSeed(long seed)
    {
        this.seed = seed;
    }

    public long getSeed()
    {
        return seed;
    }

    public Client getClient()
    {
        return client;
    }

}
