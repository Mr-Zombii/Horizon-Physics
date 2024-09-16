package me.zombii.horizon.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import finalforeach.cosmicreach.rendering.entities.IEntityModel;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class BasicModelInstance implements IEntityModelInstance {

    Model model;
    ModelInstance instance;
    public static ModelBatch batch;

    static {
        Threads.runOnMainThread(() -> {
            batch = new ModelBatch();
        });
    }

    public BasicModelInstance(Model model) {
        this.model = model;
        Threads.runOnMainThread(() -> {
            this.instance = new ModelInstance(model);
        });
    }

    @Override
    public IEntityModel getModel() {
        return new IEntityModel() {
            @Override
            public IEntityModelInstance getNewModelInstance() {
                EntityModelInstance instance = new EntityModelInstance();
                instance.setEntityModel(this);
                return instance;
            }
        };
    }

    @Override
    public void setTint(float v, float v1, float v2, float v3) {}

    @Override
    public void render(Entity entity, Camera camera, Matrix4 matrix4) {
        Gdx.gl.glDepthFunc(GL11.GL_ALWAYS);
//        batch.begin(camera);
//        batch.render(instance);
//        batch.end();
        Gdx.gl.glDepthFunc(GL20.GL_LESS);
    }

    @Override
    public Color getCurrentAmbientColor() {
        return Color.WHITE.cpy();
    }

    @Override
    public void setCurrentAnimation(String s) {

    }

    @Override
    public void setEntityModel(IEntityModel iEntityModel) {

    }
}
