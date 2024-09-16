package me.zombii.horizon.mesh;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.rendering.MeshData;
import finalforeach.cosmicreach.rendering.RenderOrder;
import finalforeach.cosmicreach.rendering.SharedQuadIndexData;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJson;
import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import finalforeach.cosmicreach.rendering.entities.IEntityModel;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.rendering.meshes.GameMesh;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Sky;

import java.util.concurrent.atomic.AtomicReference;

public class SingleBlockMesh implements IEntityModelInstance {

    GameShader shader;
    AtomicReference<BlockState> state;
    public boolean needsRemeshing = true;

    GameMesh mesh;

    public SingleBlockMesh(AtomicReference<BlockState> state) {
        this.state = state;

        shader = ChunkShader.DEFAULT_BLOCK_SHADER;
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
    public void setTint(float v, float v1, float v2, float v3) {

    }

    Vector3 sunDirection = new Vector3();

    Matrix4 rotTmp = new Matrix4();

    @Override
    public void render(Entity _entity, Camera camera, Matrix4 tmp) {
        rotTmp.idt();
        rotTmp.set(tmp.getRotation(new Quaternion()));
        Sky.currentSky.getSunDirection(sunDirection);
        sunDirection.rot(rotTmp);

        if (needsRemeshing) {
            MeshData data = new MeshData(shader, RenderOrder.DEFAULT);
            state.get().addVertices(data, 0, 0, 0);

            if (BlockModelJson.useIndices) {
                mesh = data.toIntIndexedMesh(true);
            } else {
                mesh = data.toSharedIndexMesh(true);
                if (mesh != null) {
                    int numIndices = (mesh.getNumVertices() * 6) / 4;
                    SharedQuadIndexData.allowForNumIndices(numIndices, false);
                }
            }
        }

        renderBlock(camera, tmp);

    }

    public void renderBlock(Camera camera, Matrix4 tmp) {
        if (mesh != null) {
            if (!BlockModelJson.useIndices) {
                SharedQuadIndexData.bind();
            }

            Vector3 batchPos = new Vector3(-.5f, -.5f, -.5f);
            try {
                this.shader.bind(camera);
                this.shader.bindOptionalMatrix4("u_projViewTrans", camera.combined);
//                this.shader.bindOptionalUniform4f("tintColor", Sky.currentSky.currentAmbientColor.cpy());
                this.shader.bindOptionalMatrix4("u_modelMat", tmp);
                this.shader.bindOptionalUniform3f("u_batchPosition", batchPos);
                this.shader.bindOptionalUniform3f("u_sunDirection", sunDirection);

                mesh.bind(this.shader.shader);
                mesh.render(this.shader.shader, GL20.GL_TRIANGLES);
                mesh.unbind(this.shader.shader);

                this.shader.unbind();
            } catch (Exception ignore) {}

            if (!BlockModelJson.useIndices) {
                SharedQuadIndexData.unbind();
            }

        }
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
