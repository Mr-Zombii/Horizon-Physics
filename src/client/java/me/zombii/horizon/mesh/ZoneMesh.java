package me.zombii.horizon.mesh;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.rendering.SharedQuadIndexData;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJson;
import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import finalforeach.cosmicreach.rendering.entities.IEntityModel;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Sky;
import me.zombii.horizon.threading.MeshingThread;
import me.zombii.horizon.util.Vec3i;
import me.zombii.horizon.world.PhysicsZone;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ZoneMesh implements IEntityModelInstance, IHorizonMesh {

    GameShader shader;
    PhysicsZone world;

    Map<Vec3i, AtomicReference<MeshingThread.VirtualChunkMeshMeta>> refMap = new HashMap<>();

    public ZoneMesh(PhysicsZone world) {
        this.world = world;
//        world.propagateLight();

        world.chunks.forEach((chunk) -> {
            AtomicReference<MeshingThread.VirtualChunkMeshMeta> ref = MeshingThread.post(world, chunk);
            refMap.put(new Vec3i(chunk.chunkX, chunk.chunkY, chunk.chunkZ), ref);
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

        world.chunks.forEach((chunk) -> {
            Vec3i pos = new Vec3i(chunk.chunkX, chunk.chunkY, chunk.chunkZ);

            AtomicReference<MeshingThread.VirtualChunkMeshMeta> ref = refMap.get(pos);

            if (chunk.blockData.isEntirely(Block.AIR.getDefaultBlockState()))
                return;

            if (((IPhysicChunk) chunk).needsRemeshing()) {
                if (ref == null) {
                    ref = new AtomicReference<>();
                    refMap.put(pos, ref);
                };
                MeshingThread.post(ref, world, chunk);
            };

            renderChunk(ref, chunk, camera, tmp);
        });
    }

    public void renderChunk(AtomicReference<MeshingThread.VirtualChunkMeshMeta> ref, Chunk chunk, Camera camera, Matrix4 tmp) {
        if (ref != null) {
            MeshingThread.VirtualChunkMeshMeta meta = ref.get();

            if (!BlockModelJson.useIndices) {
                SharedQuadIndexData.bind();
            }

            Vector3 batchPos = new Vector3(chunk.chunkX * 16, chunk.chunkY * 16, chunk.chunkZ * 16);
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

    @Override
    public void setShouldRefresh(boolean shouldRefresh) {

    }
}
