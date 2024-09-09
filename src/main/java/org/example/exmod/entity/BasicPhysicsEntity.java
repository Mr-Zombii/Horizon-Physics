package org.example.exmod.entity;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.math.Vector3;
import com.github.puzzle.core.resources.PuzzleGameAssetLoader;
import com.github.puzzle.game.engine.items.ItemThingModel;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.Zone;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.example.exmod.model.BasicModelInstance;
import org.example.exmod.world.PhysicsWorld;

import java.util.UUID;

public class BasicPhysicsEntity extends Entity implements IPhysicEntity {

    PhysicsRigidBody body;

    Vector3 rotation;
    UUID uuid;
    Model model;

    Quaternion rot = new Quaternion();

    public BasicPhysicsEntity() {
        super("base:test");

        if (body == null) body = new PhysicsRigidBody(new BoxCollisionShape(0.5f, 0.5f, 0.5f));
        if (rotation == null) rotation = new Vector3(0, 0, 0);
        if (uuid == null) uuid = UUID.randomUUID();

        Threads.runOnMainThread(() -> {
            model = PuzzleGameAssetLoader.LOADER.loadSync("funni-blocks:rocket_ship/10475_Rocket_Ship_v1_l3.obj", Model.class);
//            modelInstance = new ItemEntityModel(new ItemThingModel((ItemThing) ItemThing.allItems.get("base:pickaxe_stone")).wrap());
            modelInstance = new BasicModelInstance(model);
        });

        if (position != null)
            body.setPhysicsLocation(new Vector3f(InGame.getLocalPlayer().getPosition().x, InGame.getLocalPlayer().getPosition().y, InGame.getLocalPlayer().getPosition().z));
        body.setFriction(1f);
        body.setMass(0);
        PhysicsWorld.addEntity(this);
        hasGravity = false;
    }

    @Override
    public void update(Zone zone, double deltaTime) {
//        body.applyForce(new Vector3f(0, 0.5f, 0), new Vector3f());
        rot = body.getPhysicsRotation(null);
        Vector3f vector3f = body.getPhysicsLocation(null);
        position = new Vector3(vector3f.x, vector3f.y, vector3f.z);

        getBoundingBox(globalBoundingBox);
        super.updateEntityChunk(zone);
    }

    @Override
    public void render(Camera worldCamera) {
        if (this.modelInstance == null) return;

        tmpRenderPos.set(this.position);
        this.lastRenderPosition.set(tmpRenderPos);
        if (worldCamera.frustum.boundsInFrustum(this.globalBoundingBox)) {
            tmpModelMatrix.idt();
            tmpModelMatrix.translate(tmpRenderPos);
            tmpModelMatrix.rotate(new com.badlogic.gdx.math.Quaternion(rot.getX(), rot.getY(), rot.getZ(), rot.getW()));

//            if (tinyTint.r == 0 && tinyTint.g == 0 && tinyTint.b == 0)
//                ((PhysicsModelInstance) modelInstance).tintSet(tinyTint);
//            else
            this.modelInstance.render(this, worldCamera, tmpModelMatrix);
        }
    }

    @Override
    @NonNull
    public PhysicsBody getBody() {
        return body;
    }

    @Override
    @NonNull
    public Vector3 getEularRotation() {
        return rotation;
    }

    @Override
    @NonNull
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public void setEularRotation(Vector3 rot) {
        this.rotation = rot;
    }

    @Override
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }
}
