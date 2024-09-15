package me.zombii.horizon.util;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.BlockSelection;

public interface InGameAccess {

    ShapeRenderer getShapeRenderer();
    PerspectiveCamera getRawWorldCamera();
    Vector3 getPlayerFacing();
    BlockSelection getBlockSelection();

}
