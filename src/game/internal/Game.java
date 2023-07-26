package game.internal;

import game.internal.component.*;
import game.io.Bind;
import game.io.InputHandler;
import game.io.ResourceLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public class Game
{
    private List<Entity> entities = Collections.synchronizedList(new ArrayList<>());
    private final ArrayList<Entity> newComers = new ArrayList<>();
    private Camera camera1;
    private Camera camera2;
    private ArrayList<Field> map = new ArrayList<>();
    private int dimensions;
    private boolean gameStarted = false;
    private final ResourceLoader resourceLoader = new ResourceLoader();
    private ArrayList<BufferedImage> powerUps;
    private ArrayList<String> powerUpKeys;
    private int players;

    public Game()
    {
        InputHandler inputHandler = new InputHandler();
        inputHandler.bindSetup();
        resourceLoader.loadTextures();

        powerUps = new ArrayList<>();
        powerUpKeys = new ArrayList<>();

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
        //creating the main components
        for (int i = 0; i < w; i++)
        {
            for (int j = 0; j < h; j++)
            {
                Random r = new Random();
                mapArray[i][j] = new Field();


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
                        mapArray[i][j].acceptGameObject(new Box(resourceLoader.getImage("box"),"box",true,powerUps,powerUpKeys),-1,false);
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

        //creating the main components
        for (int i = 0; i < x; i++)
        {
            for (int j = 0; j < y; j++)
            {
                for (int k = 0; k < z; k++)
                {
                    Random r = new Random();
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
                            mapArray[i][j][k].acceptGameObject(new Box(resourceLoader.getImage("box"),"box", true,powerUps,powerUpKeys),-1,false);
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

        //creating the main components
        for (int i = 0; i < x; i++)
        {
            for (int j = 0; j < y; j++)
            {
                for (int k = 0; k < z; k++)
                {
                    for (int l = 0; l < w; l++)
                    {
                        Random r = new Random();
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
                                mapArray[i][j][k][l].acceptGameObject(new Box(resourceLoader.getImage("box"),"box", true,powerUps, powerUpKeys),-1,false);
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
            synchronized (entities)
            {
                entities.removeIf(Entity::live);

                entities.addAll(newComers);
                newComers.clear();
            }
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

    public void addPlayer(int pnumber)
    {
        Random r = new Random();
        int field = 0;
        boolean placement = false;
        while (!placement)
        {
            field = r.nextInt(map.size());
            placement = map.get(field).spawnPoint();
        }
        Player player;

        if (pnumber == 0)
        {
            ArrayList<Bind> binds = new ArrayList<>(Arrays.asList(Bind.UP,Bind.DOWN,Bind.LEFT,Bind.RIGHT,Bind.PLANT,Bind.ROTATE));
            player = new Player(resourceLoader.getImage("player_blue"), "player_blue", map.get(field), this,binds);
            camera1 = new Camera(player,6,280,300);
        }
        else
        {
            ArrayList<Bind> binds = new ArrayList<>(Arrays.asList(Bind.UP2,Bind.DOWN2,Bind.LEFT2,Bind.RIGHT2,Bind.PLANT2,Bind.ROTATE2));
            player = new Player(resourceLoader.getImage("player_red"), "player_red", map.get(field), this,binds);
            camera2 = new Camera(player,6,850,300);
        }

        map.get(field).acceptGameObject(player,0,false);
        entities.add(player);
        players++;
    }

    public void addEnemy()
    {
        Random r = new Random();
        int field = 0;
        boolean placement = false;
        while (!placement)
        {
            field = r.nextInt(map.size());
            placement = map.get(field).spawnPoint();
        }
        Enemy enemy = new Enemy(resourceLoader.getImage("player_gray"),"player_gray",map.get(field),this);
        map.get(field).acceptGameObject(enemy,0,false);
        entities.add(enemy);
        players++;
    }

    public void drawCycle(Graphics g)
    {
        if (camera1 != null)
            camera1.draw(g);
        if (camera2 != null)
            camera2.draw(g);

        g.setColor(Color.red);
        if (players <= 1)
            g.drawString("GAME OVER!",10,10);
    }

    public Bomb createBomb(Field field,Bomberman b,int size)
    {
        Bomb newBomb = new Bomb(resourceLoader.getImage("bomb"),"bomb",field,this,b,size);
        if (field.placeBomb(newBomb))
        {
            newComers.add(newBomb);
            return newBomb;
        }
        return null;
    }

    public void playerDied()
    {
        players--;
    }


}
