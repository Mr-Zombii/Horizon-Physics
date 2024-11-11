package me.zombii.horizon.util;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.BlockSelection;
import finalforeach.cosmicreach.gamestates.InGame;

public interface InGameAccess {

    ShapeRenderer getShapeRenderer();
    PerspectiveCamera getRawWorldCamera();
    Vector3 getPlayerFacing();
    BlockSelection getBlockSelection();

    static InGameAccess getAccess() {
        return (InGameAccess) InGame.IN_GAME;
    }

}
