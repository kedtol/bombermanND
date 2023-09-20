package game.internal.component;


public enum Color // i hate myself for this (WHY DID I NAME THIS "COLOR") - *java.awt.Color wants to know your location*
{
    RED (java.awt.Color.red,"player_red"),
    BLUE (new java.awt.Color(0,130,200),"player_blue"),
    GRAY (java.awt.Color.gray,"player_gray"),
    GREEN (java.awt.Color.green,"player_green"),
    PINK (java.awt.Color.pink,"player_pink"),
    ORANGE (new java.awt.Color(255,145,0),"player_orange"),
    YELLOW (new java.awt.Color(200,150,0),"player_yellow"),
    OLIVE (new java.awt.Color(150,150,0),"player_olive"),
    PURPLE (new java.awt.Color(140, 0, 180),"player_purple"),
    AI (java.awt.Color.black,"player_ai");

    private final java.awt.Color c;
    private final String resName;

    Color(java.awt.Color c, String resName)
    {
        this.c = c;
        this.resName = resName;
    }

    public java.awt.Color getAWT()
    {
        return c;
    }
    public String getName()
    {
        return resName;
    }
}
