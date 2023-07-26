package game.io;

import javax.swing.*;

public class InputHandler
{
    public static final JLabel bindLabel = new JLabel();
    private static boolean[] keyStateList;
    private static final char[] activeKeyCodes = Bind.getActiveBinds();
    public void bindSetup()
    {
        keyStateList = new boolean[activeKeyCodes.length];

        for (char activeKeyCode : activeKeyCodes)
        {

            ActionMap am = bindLabel.getActionMap();
            InputMap im = bindLabel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

            //press "event"
            KeyStroke ks = KeyStroke.getKeyStroke(activeKeyCode, 0, false);
            im.put(ks, activeKeyCode);
            am.put(activeKeyCode, new KeyAction(activeKeyCode, true));

            //release "event"
            ks = KeyStroke.getKeyStroke(activeKeyCode, 0, true);
            im.put(ks, activeKeyCode + "_");
            am.put(activeKeyCode + "_", new KeyAction(activeKeyCode, false));
        }

    }

    public static void inputReceive(char key, boolean pressed)
    {
        for (int i = 0; i < activeKeyCodes.length; i++)
        {
            if (key == activeKeyCodes[i])
                keyStateList[i] = pressed;
        }
    }

    public static boolean isButtonPressed(Bind bind)
    {
        for (int i = 0; i < activeKeyCodes.length; i++)
            if (activeKeyCodes[i] == bind.getKeyCode())
                return keyStateList[i];

        return false; // non-existent bind
    }


}
