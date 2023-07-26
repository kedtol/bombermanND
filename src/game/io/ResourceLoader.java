package game.io;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class ResourceLoader
{
    private final HashMap<String, BufferedImage> images = new HashMap<>();
    public void loadTextures()
    {

        File path = new File("img/game/io"); // a getResource csak packagen belül lát valamiért
        // (szóval emulálni kell a package feelinget a resource mappaszerkezeténél is)

        for (File file : Objects.requireNonNull(path.listFiles())) // imádom ezeket a kotlin type not null függvényeket
        {
            String fname = file.getName();
            if (fname.endsWith(".png"))
            {
                try
                {
                    BufferedImage image = ImageIO.read(Objects.requireNonNull(ResourceLoader.class.getResource(fname)));
                    if (image != null)
                        images.put(fname.substring(0, fname.length() - 4), image);
                    System.out.println("Image loaded: " + file.getName());
                }
                catch (IOException e)
                {
                    System.out.println("Image load error at: " + file.getName());
                }
            }
        }

    }

    public BufferedImage getImage(String key)
    {
        return images.get(key);
    }

}
