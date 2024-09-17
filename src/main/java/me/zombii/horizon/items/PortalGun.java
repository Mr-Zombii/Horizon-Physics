package me.zombii.horizon.items;

import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.util.Identifier;
import me.zombii.horizon.Constants;
import me.zombii.horizon.items.api.I3DItem;

public class PortalGun implements IModItem, I3DItem {

    DataTagManifest manifest = new DataTagManifest();
    Identifier modelLocation = Identifier.of(Constants.MOD_ID, "models/items/g3dj/Portal Gun.g3dj");

    public PortalGun() {

    }

    @Override
    public Identifier getIdentifier() {
        return Identifier.of(Constants.MOD_ID, "portal_gun");
    }

    @Override
    public String getName() {
        return "Portal Gun";
    }

    @Override
    public Identifier getModelLocation() {
        return modelLocation;
    }
}
