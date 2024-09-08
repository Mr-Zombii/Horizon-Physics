package org.example.exmod.mesh;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.github.puzzle.util.Vec3i;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.ImmutablePair;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
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

    Map<Vec3i, Pair<GameMesh, GameShader>[]> meshPairs = new HashMap<>();
    GameShader shader;
    WorldCube worldCube;

    Map<Vec3i, AtomicReference<Array<MeshData>>> references = new HashMap<>();

    public MutliBlockMesh(WorldCube entity69) {
        this.worldCube = entity69;

        for (Vec3i pos : entity69.chunks.keySet()) {
            System.out.println(pos);
            AtomicReference<Array<MeshData>> reference = new AtomicReference<>();
            ExampleMod.thread.meshChunk(entity69.chunks, pos, entity69.chunks.get(pos), reference);
            ExampleMod.thread.meshChunk(entity69.chunks, pos, entity69.chunks.get(pos), reference);
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

    float rotZ = 0;
    float rotY = 0;

    @Override
    public void render(Entity _entity, Camera camera, Matrix4 matrix4) {
        if (_entity instanceof WorldCube entity) {
            if (this.meshPairs != null) {

                for (Vec3i pos : worldCube.chunks.keySet()) {
                    Pair<GameMesh, GameShader>[] meshes = meshPairs.get(pos);
                    if (meshes != null && meshes.length != 0) {
                        Matrix4 tmp = matrix4.cpy();
                        tmp.rotate(new Vector3(0, 0, 1), rotZ);
                        tmp.rotate(new Vector3(0, 1, 0), rotY);

                        Matrix4 s = new Matrix4();
                        tmp.rotate(new Vector3(0, 0, 1), rotZ);
                        tmp.rotate(new Vector3(0, 1, 0), rotY);
                        entity.entityBoundingBox.setTransform(s);

//                    tmp.translate(pos.x() * 16, pos.y() * 16, pos.z() * 16);

                        if (!BlockModelJson.useIndices) {
                            SharedQuadIndexData.bind();
                        }

                        Vector3 batchPos = new Vector3(pos.x() * 16, pos.y() * 16, pos.z() * 16);
                        try {
                            Pair<GameMesh, GameShader> defaultMesh = meshes[2];
                            this.shader = defaultMesh.getRight();

                            this.shader.bind(camera);
                            this.shader.bindOptionalMatrix4("u_projViewTrans", camera.combined);
                            this.shader.bindOptionalUniform4f("tintColor", Sky.currentSky.currentAmbientColor.cpy());
                            this.shader.bindOptionalMatrix4("u_modelMat", tmp);
                            this.shader.bindOptionalUniform3f("u_batchPosition", batchPos);

                            defaultMesh.getLeft().bind(this.shader.shader);
                            defaultMesh.getLeft().render(this.shader.shader, GL20.GL_TRIANGLES);
                            defaultMesh.getLeft().unbind(this.shader.shader);

                            this.shader.unbind();
                        } catch (Exception ignore) {}

                        try {
                            Pair<GameMesh, GameShader> partialTransparent = meshes[1];

                            this.shader = partialTransparent.getRight();
                            this.shader.bind(camera);
                            this.shader.bindOptionalMatrix4("u_projViewTrans", camera.combined);
                            this.shader.bindOptionalUniform4f("tintColor", Sky.currentSky.currentAmbientColor.cpy());
                            this.shader.bindOptionalMatrix4("u_modelMat", tmp);
                            this.shader.bindOptionalUniform3f("u_batchPosition", batchPos);

                            partialTransparent.getLeft().bind(this.shader.shader);
                            partialTransparent.getLeft().render(this.shader.shader, GL20.GL_TRIANGLES);
                            partialTransparent.getLeft().unbind(this.shader.shader);

                            this.shader.unbind();
                        } catch (Exception ignore) {}

                        try {
                            Pair<GameMesh, GameShader> fullyTransparent = meshes[0];
                            this.shader = fullyTransparent.getRight();
                            this.shader.bind(camera);
                            this.shader.bindOptionalMatrix4("u_projViewTrans", camera.combined);
                            this.shader.bindOptionalUniform4f("tintColor", Sky.currentSky.currentAmbientColor.cpy());
                            this.shader.bindOptionalMatrix4("u_modelMat", tmp);
                            this.shader.bindOptionalUniform3f("u_batchPosition", batchPos);
                            fullyTransparent.getLeft().bind(this.shader.shader);
                            fullyTransparent.getLeft().render(this.shader.shader, GL20.GL_TRIANGLES);
                            fullyTransparent.getLeft().unbind(this.shader.shader);

                            this.shader.unbind();
                        } catch (Exception ignore) {}

                        if (!BlockModelJson.useIndices) {
                            SharedQuadIndexData.unbind();
                        }

                    } else {
                        Array<MeshData> data = references.get(pos).get();

                        if (data != null && !data.isEmpty()) {
                            MeshData[] meshData = sortData(data);
                            Pair<GameMesh, GameShader>[] finalizedMeshes = finalizeMeshes(meshData);
                            meshPairs.put(pos, finalizedMeshes);
//                        if (BlockModelJson.useIndices) {
//                            meshes.put(pos, data.get(0).toIntIndexedMesh(true));
//                            shader = data.get(0).shader;
//                        } else {
//                            meshes.put(pos, data.get(0).toSharedIndexMesh(true));
//                            shader = data.get(0).shader;
//                            if (meshes != null) {
//                                int numIndices = (meshes.get(pos).getNumVertices() * 6) / 4;
//                                SharedQuadIndexData.allowForNumIndices(numIndices, false);
//                            }
//                        }
                        }
                    }
                }
                rotZ += 0.5f;
                rotY += 0.1f;
            }
        }
    }

    private Pair<GameMesh, GameShader>[] finalizeMeshes(MeshData[] meshData) {
        Pair<GameMesh, GameShader>[] finalizedMeshes = new ImmutablePair[3];
        for (int i = 0; i < 3; i++) {
            MeshData data = meshData[i];

            if (data != null) {
                if (BlockModelJson.useIndices) {
                    finalizedMeshes[i] = new ImmutablePair<>(data.toIntIndexedMesh(true), data.shader);
                } else {
                    finalizedMeshes[i] = new ImmutablePair<>(data.toSharedIndexMesh(true), data.shader);
                    if (finalizedMeshes[i].getLeft() != null) {
                        int numIndices = (finalizedMeshes[i].getLeft().getNumVertices() * 6) / 4;
                        SharedQuadIndexData.allowForNumIndices(numIndices, false);
                    }
                }
            }
        }
        return finalizedMeshes;
    }

    private MeshData[] sortData(Array<MeshData> data) {
        MeshData[] sortedData = new MeshData[3];
        for (int i = 0; i < data.size; i++) {
            MeshData meshData = data.get(i);
            switch (data.get(i).renderOrder) {
                case FULLY_TRANSPARENT -> {
                    sortedData[0] = meshData;
                }
                case PARTLY_TRANSPARENT -> {
                    sortedData[1] = meshData;
                }
                case DEFAULT -> {
                    sortedData[2] = meshData;
                }
            }
        }
        return sortedData;
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
