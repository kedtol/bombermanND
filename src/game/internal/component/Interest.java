package game.internal.component;

public interface Interest
{
    int getPriority(); // 0 > get away from, 0 < collect
    int getType(); // 0 = contact, 1 = explode, 2  >= range

    boolean isContactable();

}
