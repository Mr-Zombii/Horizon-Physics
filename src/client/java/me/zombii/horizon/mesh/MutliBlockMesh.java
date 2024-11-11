package me.zombii.horizon.mesh;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import me.zombii.horizon.util.Vec3i;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.rendering.SharedQuadIndexData;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJson;
import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import finalforeach.cosmicreach.rendering.entities.IEntityModel;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Sky;
import me.zombii.horizon.threading.MeshingThread;
import me.zombii.horizon.world.VirtualChunk;
import me.zombii.horizon.world.VirtualWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class MutliBlockMesh implements IEntityModelInstance {

    GameShader shader;
    VirtualWorld world;

    Map<Vec3i, AtomicReference<MeshingThread.VirtualChunkMeshMeta>> refMap = new HashMap<>();

    public MutliBlockMesh(VirtualWorld world) {
        this.world = world;
        world.propagateLight();

        world.forEachChunk((pos, chunk) -> {
            AtomicReference<MeshingThread.VirtualChunkMeshMeta> ref = MeshingThread.post(chunk);
            refMap.put(pos, ref);
        });

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
    Quaternion quaternion = new Quaternion();

    @Override
    public void render(Entity _entity, Camera camera, Matrix4 tmp) {
        if (refMap == null) return;

        rotTmp.idt();
        rotTmp.set(tmp.getRotation(new Quaternion()));
        Sky.currentSky.getSunDirection(sunDirection);
        sunDirection.rot(rotTmp);

        world.forEachChunk((pos, chunk) -> {
            AtomicReference<MeshingThread.VirtualChunkMeshMeta> ref = refMap.get(pos);

            if (chunk.isEntirely(Block.AIR.getDefaultBlockState()))
                return;

            if (chunk.needsRemeshing) {
                if (ref == null) {
                    ref = new AtomicReference<>();
                    refMap.put(pos, ref);
                };
                MeshingThread.post(ref, chunk);
            };
            renderChunk(ref, chunk, camera, tmp);
//            System.out.println(_entity.entityTypeId + " needs remeshing on chunk " + pos);

//                MeshingThread.post(refMap.get(pos), chunk);
//            }
        });
    }

    public void renderChunk(AtomicReference<MeshingThread.VirtualChunkMeshMeta> ref, VirtualChunk chunk, Camera camera, Matrix4 tmp) {
        if (ref != null) {
            MeshingThread.VirtualChunkMeshMeta meta = ref.get();

            if (!BlockModelJson.useIndices) {
                SharedQuadIndexData.bind();
            }

            Vector3 batchPos = new Vector3(chunk.chunkPos.x() * 16, chunk.chunkPos.y() * 16, chunk.chunkPos.z() * 16);
            try {
                this.shader = meta.defaultLayerShader;

                this.shader.bind(camera);
                this.shader.bindOptionalMatrix4("u_projViewTrans", camera.combined);
//                this.shader.bindOptionalUniform4f("tintColor", Sky.currentSky.currentAmbientColor.cpy());
                this.shader.bindOptionalMatrix4("u_modelMat", tmp);
                this.shader.bindOptionalUniform3f("u_batchPosition", batchPos);
                this.shader.bindOptionalUniform3f("u_sunDirection", sunDirection);

                meta.defaultLayerMesh.bind(this.shader.shader);
                meta.defaultLayerMesh.render(this.shader.shader, GL20.GL_TRIANGLES);
                meta.defaultLayerMesh.unbind(this.shader.shader);

                this.shader.unbind();
            } catch (Exception ignore) {}

            try {
                this.shader = meta.semiTransparentLayerShader;
                this.shader.bind(camera);
                this.shader.bindOptionalMatrix4("u_projViewTrans", camera.combined);
//                this.shader.bindOptionalUniform4f("tintColor", Sky.currentSky.currentAmbientColor.cpy());
                this.shader.bindOptionalMatrix4("u_modelMat", tmp);
                this.shader.bindOptionalUniform3f("u_batchPosition", batchPos);
                if (shader instanceof ChunkShader)
                    this.shader.bindOptionalUniform3f("u_sunDirection", sunDirection);

                meta.semiTransparentLayerMesh.bind(this.shader.shader);
                meta.semiTransparentLayerMesh.render(this.shader.shader, GL20.GL_TRIANGLES);
                meta.semiTransparentLayerMesh.unbind(this.shader.shader);

                this.shader.unbind();
            } catch (Exception ignore) {}

            try {
                this.shader = meta.transparentLayerShader;
                this.shader.bind(camera);
                this.shader.bindOptionalMatrix4("u_projViewTrans", camera.combined);
//                this.shader.bindOptionalUniform4f("tintColor", Sky.currentSky.currentAmbientColor.cpy());
                this.shader.bindOptionalMatrix4("u_modelMat", tmp);
                this.shader.bindOptionalUniform3f("u_batchPosition", batchPos);
                this.shader.bindOptionalUniform3f("u_sunDirection", sunDirection);

                meta.transparentLayerMesh.bind(this.shader.shader);
                meta.transparentLayerMesh.render(this.shader.shader, GL20.GL_TRIANGLES);
                meta.transparentLayerMesh.unbind(this.shader.shader);

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
