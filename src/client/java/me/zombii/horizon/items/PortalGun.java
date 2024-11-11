package me.zombii.horizon.items;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.github.puzzle.game.resources.PuzzleGameAssetLoader;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.util.Identifier;
import me.zombii.horizon.Constants;
import me.zombii.horizon.items.api.I3DItem;

import java.util.concurrent.atomic.AtomicReference;

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
    public String toString() {
        return getID();
    }

    @Override
    public String getName() {
        return "Portal Gun";
    }

    @Override
    public Identifier getModelLocation() {
        return modelLocation;
    }

    @Override
    public DataTagManifest getTagManifest() {
        return manifest;
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
