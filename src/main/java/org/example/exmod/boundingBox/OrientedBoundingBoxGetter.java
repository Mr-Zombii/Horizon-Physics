package org.example.exmod.boundingBox;

import com.badlogic.gdx.math.collision.OrientedBoundingBox;

public interface OrientedBoundingBoxGetter {

    boolean hasInnerBounds();
    OrientedBoundingBox getInnerBounds();
    void setInnerBounds(OrientedBoundingBox boundingBox);

}
