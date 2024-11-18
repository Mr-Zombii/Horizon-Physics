package me.zombii.horizon.items;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.util.Identifier;
import me.zombii.horizon.Constants;
import me.zombii.horizon.entity.api.IPhysicEntity;
import me.zombii.horizon.items.api.I3DItem;
import me.zombii.horizon.util.PhysicsUtil;

public class GravityGun implements IModItem, I3DItem {

    DataTagManifest manifest = new DataTagManifest();
    Identifier modelLocation = Identifier.of(Constants.MOD_ID, "models/items/g3dj/GravityGun.g3dj");

    public GravityGun() {

    }

    @Override
    public String toString() {
        return getID();
    }

    public static IPhysicEntity heldEntity;

    static Vector3 intersectionPoint = new Vector3();
    static Ray ray = new Ray();

    @Override
    public void use(ItemSlot slot, Player player) {
        if (heldEntity != null) {
            heldEntity.setPickedUp(!heldEntity.isPickedUp());
            heldEntity = null;
            return;
        }

        Vector3 rayStart = player.getPosition().cpy().add(0, 1, 0);
        ray.set(rayStart, player.getEntity().viewDirection);
        Vector3 rayEnd = rayStart.cpy().add(player.getEntity().viewDirection.cpy().scl(5));

        intersectionPoint.setZero();
        PhysicsUtil.raycast(intersectionPoint, ray, rayEnd, this::interact);
    }

    public void interact(float dist, Entity e, PhysicsRayTestResult result) {
        if (((IPhysicEntity) e).canBePickedUp()) {
            IPhysicEntity entity = (IPhysicEntity) e;
            entity.setPickedUp(!entity.isPickedUp());
            if (entity.isPickedUp()) heldEntity = entity;
        }
    }

    @Override
    public Identifier getIdentifier() {
        return Identifier.of(Constants.MOD_ID, "gravity_gun");
    }

    @Override
    public String getName() {
        return "Gravity Gun";
    }

    @Override
    public Identifier getModelLocation() {
        return modelLocation;
    }

//    @Override
//    public void loadModel(G3dModelLoader modelLoader, AtomicReference<ModelInstance> model) {
//        final FileHandle modelHandle = PuzzleGameAssetLoader.locateAsset(getModelLocation());
//
//        Threads.runOnMainThread(() -> {
//            GLBLoader loader = new GLBLoader();
//            SceneAsset model1 = loader.load(modelHandle, true);
//
//            ModelInstance instance = new ModelInstance(model1.scene.model);
//
//            model.set(instance);
//        });
//    }
}
