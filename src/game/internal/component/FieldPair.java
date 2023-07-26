package game.internal.component;

import java.io.Serializable;

// nem fogom implementálni a javafx pair osztályát...
// vagy a kotlin pair osztályát általánosba (<k,v>), ha csak egyszer kell használnom
public class FieldPair implements Serializable
{
    private Field positive;
    private Field negative;

    FieldPair(Field positive, Field negative)
    {
        this.positive = positive;
        this.negative = negative;
    }

    public Field get(boolean positive)
    {
        if (positive)
            return this.positive;
        else
            return this.negative;
    }

    public void set(boolean positive,Field field)
    {
        if (positive)
            this.positive = field;
        else
            this.negative = field;
    }
}
