package me.zombii.horizon.items;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.math.Vector3;
import com.github.puzzle.core.resources.PuzzleGameAssetLoader;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.util.Identifier;
import me.zombii.horizon.Constants;
import me.zombii.horizon.entity.api.IPhysicEntity;
import me.zombii.horizon.items.api.I3DItem;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.util.InGameAccess;
import me.zombii.horizon.util.PhysicsUtil;

import java.util.concurrent.atomic.AtomicReference;

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

    @Override
    public void use(ItemSlot slot, Player player) {
        if (heldEntity != null) {
            heldEntity.setPickedUp(!heldEntity.isPickedUp());
            heldEntity = null;
            return;
        }
        Vector3 rayStart = player.getPosition().cpy().add(0, 1, 0);
        Vector3 rayEnd = rayStart.cpy().add(InGameAccess.getAccess().getPlayerFacing().cpy().scl(5));

        PhysicsUtil.raycast(rayStart, rayEnd, this::interact);
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

    @Override
    public void loadModel(G3dModelLoader modelLoader, AtomicReference<ModelInstance> model) {
        final FileHandle modelHandle = PuzzleGameAssetLoader.locateAsset(getModelLocation());

        Threads.runOnMainThread(() -> {
            Model model1 = modelLoader.loadModel(modelHandle);

            model.set(new ModelInstance(model1));
        });
    }
}
