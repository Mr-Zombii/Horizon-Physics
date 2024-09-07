package org.example.exmod.mesh;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.github.puzzle.game.worldgen.structures.Structure;
import com.github.puzzle.util.Vec3i;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.rendering.MeshData;
import finalforeach.cosmicreach.rendering.SharedQuadIndexData;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJson;
import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import finalforeach.cosmicreach.rendering.entities.IEntityModel;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.rendering.meshes.GameMesh;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Sky;
import org.example.exmod.ExampleMod;
import org.example.exmod.entity.WorldCube;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class MutliBlockMesh implements IEntityModelInstance {

    Map<Vec3i, GameMesh> meshes = new HashMap<>();
    GameShader shader;
    WorldCube worldCube;

    Map<Vec3i, AtomicReference<Array<MeshData>>> references = new HashMap<>();

    public MutliBlockMesh(WorldCube entity69, Map<Vec3i, Structure> chunks) {
        this.worldCube = entity69;

        for (Vec3i pos : chunks.keySet()) {
            AtomicReference<Array<MeshData>> reference = new AtomicReference<>();
            ExampleMod.thread.meshChunk(chunks, pos, chunks.get(pos), reference);
            references.put(pos, reference);
        }

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

    @Override
    public void render(Entity entity, Camera camera, Matrix4 matrix4) {
        if (this.meshes != null) {
            if (!BlockModelJson.useIndices) {
                SharedQuadIndexData.bind();
            }

            this.shader.bind(camera);
            this.shader.bindOptionalMatrix4("u_projViewTrans", camera.combined);
            this.shader.bindOptionalUniform4f("tintColor", Sky.currentSky.currentAmbientColor.cpy());

            for (Vec3i pos : worldCube.chunks.keySet()) {
                GameMesh mesh = meshes.get(pos);
                if (mesh != null) {
                    Matrix4 tmp = matrix4.cpy();
                    tmp.translate(pos.x() * 16, pos.y() * 16, pos.z() * 16);
                    this.shader.bindOptionalMatrix4("u_modelMat", tmp);
                    mesh.bind(this.shader.shader);
                    mesh.render(this.shader.shader, GL20.GL_TRIANGLES);
                    mesh.unbind(this.shader.shader);
                } else {
                    Array<MeshData> data = references.get(pos).get();

                    if (data != null && !data.isEmpty()) {
                        if (BlockModelJson.useIndices) {
                            meshes.put(pos, data.get(0).toIntIndexedMesh(true));
                            shader = data.get(0).shader;
                        } else {
                            meshes.put(pos, data.get(0).toSharedIndexMesh(true));
                            shader = data.get(0).shader;
                            if (meshes != null) {
                                int numIndices = (meshes.get(pos).getNumVertices() * 6) / 4;
                                SharedQuadIndexData.allowForNumIndices(numIndices, false);
                            }
                        }
                    }
                }
                this.shader.unbind();
            }

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
