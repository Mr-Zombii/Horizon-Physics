package me.zombii.horizon.items;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.math.Vector3;
import com.github.puzzle.game.resources.PuzzleGameAssetLoader;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.util.Identifier;
import me.zombii.horizon.Constants;
import me.zombii.horizon.items.api.I3DItem;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

import java.util.concurrent.atomic.AtomicReference;

public class ToolGun implements IModItem, I3DItem {

    DataTagManifest manifest = new DataTagManifest();
    Identifier modelLocation = Identifier.of(Constants.MOD_ID, "models/items/g3dj/test.glb");

    public ToolGun() {
    }

    @Override
    public Identifier getIdentifier() {
        return Identifier.of(Constants.MOD_ID, "tool_gun");
    }

    @Override
    public String getName() {
        return "Tool Gun";
    }

    @Override
    public String toString() {
        return getID();
    }

    @Override
    public Identifier getModelLocation() {
        return modelLocation;
    }

    @Override
    public void loadModel(G3dModelLoader modelLoader, AtomicReference<ModelInstance> model) {
        final FileHandle modelHandle = PuzzleGameAssetLoader.locateAsset(getModelLocation());

        Threads.runOnMainThread(() -> {
            GLBLoader loader = new GLBLoader();
            SceneAsset model1 = loader.load(modelHandle, true);

            ModelInstance instance = new ModelInstance(model1.scene.model);

            model.set(instance);
        });
    }

    @Override
    public Vector3 getScalar() {
        return new Vector3(0.75f, 0.75f, 0.75f);
    }
}
