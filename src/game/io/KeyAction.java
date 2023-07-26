package game.io;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class KeyAction extends AbstractAction
{
    private final char key;
    private final boolean pressed;

    KeyAction(char key, boolean pressed)
    {
        this.key = key;
        this.pressed = pressed;
    }

    @Override
    public void actionPerformed(ActionEvent e) // lehetne ott létrehozni a classt, de no
    {
        InputHandler.inputReceive(key,pressed);
    }
}