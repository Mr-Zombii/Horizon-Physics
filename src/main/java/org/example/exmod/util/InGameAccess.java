package org.example.exmod.util;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface InGameAccess {

    ShapeRenderer getShapeRenderer();
    PerspectiveCamera getRawWorldCamera();

}
