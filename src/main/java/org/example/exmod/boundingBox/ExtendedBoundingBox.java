package org.example.exmod.boundingBox;

import com.badlogic.gdx.math.collision.OrientedBoundingBox;

public interface ExtendedBoundingBox {

    boolean hasInnerBounds();
    OrientedBoundingBox getInnerBounds();
    void setInnerBounds(OrientedBoundingBox boundingBox);
    boolean shouldCollideWith();
    void shouldCollideWith(boolean b);

}
