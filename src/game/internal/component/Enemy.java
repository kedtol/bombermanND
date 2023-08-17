package game.internal.component;

import game.internal.Game;
import game.internal.network.Client;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class Enemy extends Bomberman
{
    /* The AI plan:
    * fetches nearby interests (2d array with same functions as the camera)
    * evaluates them (dijkstra+bfs)
    * executes the best plan (load plan)
    * while executing collects further data
    * */

    private int state = 0; // AI state (0 = fetch, 1 = evaluate, 2 = execute)
    transient private ArrayList<Field> explored; // known fields
    private boolean escaped = false;
    private boolean freshBombPlaced = false;
    private boolean ai = false;

    public Enemy(BufferedImage image, String imageName, Field field, Game game)
    {
        super(image,imageName, field, game);
    }

    public void setupSync(Client client)
    {
        super.setupSync(client);
        if (client == null)
            ai = true;
    }

    @Override
    public void tickAction()
    {
        if (game != null && ai)
            ai();

        super.tickAction();
    }

    public void ai()
    {
        Random random = new Random();
        switch (state)
        {
            case 0: // fetch
                //explored = new ArrayList<>();
                //field.deepExplore(explored,game.getDimensions());
                state = 1;
            break; // <- nem szükséges

            case 1: //exploration
                int axis = random.nextInt(game.getDimensions());
                boolean positive = random.nextBoolean();


                if ((!field.inDanger(game.getDimensions()) && freshBombPlaced))
                {
                    freshBombPlaced = false;
                    escaped = true;
                }

                if (placedBombs == 0)
                    escaped = false;

                if (field.getNeighbor(axis, positive) != null)
                    if (!freshBombPlaced && escaped)
                    {
                        if (!field.getNeighbor(axis, positive).onFire() && !field.getNeighbor(axis, positive).inDanger(game.getDimensions()))
                            move(axis, positive);
                    }
                    else
                    {
                        if (!field.getNeighbor(axis, positive).onFire())
                            move(axis,positive);
                    }


                if (field.onlyOneFreeNeighbor(game.getDimensions()))
                {
                    freshBombPlaced = true;
                    placeBomb();
                }
            break;

            case 2:
                // can be implemented
            break;

        }
    }

    @Override
    public void onFieldEntry()
    {
        //state = 3;
        super.onFieldEntry();
    }

}
