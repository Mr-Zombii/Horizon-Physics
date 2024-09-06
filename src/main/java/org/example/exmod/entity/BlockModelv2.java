package org.example.exmod.entity;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.VertexData;
import com.badlogic.gdx.math.Matrix4;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.rendering.MeshData;
import finalforeach.cosmicreach.rendering.RenderOrder;
import finalforeach.cosmicreach.rendering.SharedQuadIndexData;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJson;
import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import finalforeach.cosmicreach.rendering.entities.IEntityModel;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.rendering.meshes.IGameMesh;
import finalforeach.cosmicreach.rendering.shaders.GameShader;

public class BlockModelv2 implements IEntityModelInstance {

    IGameMesh mesh;
    GameShader shader;

    public BlockModelv2(BlockState blockState) {
        MeshData meshData = new MeshData(GameShader.getShaderForBlockState(blockState), RenderOrder.getRenderOrderForBlockState(blockState));
        shader = meshData.getShader();
//        blockState.addVertices(meshData, 0, 0, 0);
        blockState.addVertices(meshData, 1, 0, 0);
        blockState.addVertices(meshData, 0, 0, 1);
        blockState.addVertices(meshData, -1, 0, 0);
        blockState.addVertices(meshData, 0, 0, -1);
//        meshData.vertices.items

        for (int i = 0; i < meshData.vertices.items.length; i += 5) {
            float x = meshData.vertices.items[i];
            float y = meshData.vertices.items[i + 1];
            float z = meshData.vertices.items[i + 2];
            x -= 0.5f;
            y -= 0.5f;
            z -= 0.5f;
            meshData.vertices.items[i] = x;
            meshData.vertices.items[i + 1] = y;
            meshData.vertices.items[i + 2] = z;
        }
        if (BlockModelJson.useIndices) {
            mesh = meshData.toIntIndexedMesh(true);
        } else {
            mesh = meshData.toSharedIndexMesh(true);
            if (mesh != null) {
                int numIndices = mesh.getNumVertices() * 6 / 4;
                SharedQuadIndexData.allowForNumIndices(numIndices, false);
            }
        }
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
        if (this.mesh != null) {
            if (!BlockModelJson.useIndices) {
                SharedQuadIndexData.bind();
            }

            this.shader.bind(camera);
            this.shader.bindOptionalMatrix4("u_projViewTrans", camera.combined);
            this.shader.bindOptionalMatrix4("u_modelMat", matrix4);

            this.shader.bindOptionalUniform4f("tintColor", Color.WHITE.cpy());
            this.mesh.bind(this.shader.shader);
            this.mesh.render(this.shader.shader, 4);
            this.mesh.unbind(this.shader.shader);
            this.shader.unbind();
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
