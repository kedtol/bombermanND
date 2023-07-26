package game.io;

public enum Bind
{
    UP('W'),
    DOWN('S'),
    LEFT('A'),
    RIGHT('D'),
    PLANT((char)32),
    ROTATE('R'),

    UP2((char)38),
    DOWN2((char)40),
    LEFT2((char)37),
    RIGHT2((char)39),
    PLANT2('L'),
    ROTATE2('K');

    private final char keyCode;
    Bind(char keyCode)
    {
        this.keyCode = keyCode;
    }

    public char getKeyCode()
    {
        return keyCode;
    }

    public static char[] getActiveBinds()
    {
        char[] out = new char[Bind.values().length];

        for (int i = 0; i < out.length; i++)
            out[i] = Bind.values()[i].keyCode;

        return out;
    }

}
