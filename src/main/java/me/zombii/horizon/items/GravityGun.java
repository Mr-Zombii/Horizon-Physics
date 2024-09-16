package me.zombii.horizon.items;

import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import finalforeach.cosmicreach.util.Identifier;
import me.zombii.horizon.Constants;
import me.zombii.horizon.items.api.I3DItem;

public class GravityGun implements IModItem, I3DItem {

    DataTagManifest manifest = new DataTagManifest();
    Identifier modelLocation = Identifier.of(Constants.MOD_ID, "models/items/g3dj/GravityGun.g3dj");

    public GravityGun() {

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
}
