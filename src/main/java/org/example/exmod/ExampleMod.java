package org.example.exmod;

import com.badlogic.gdx.physics.bullet.Bullet;
import com.github.puzzle.core.PuzzleRegistries;
import com.github.puzzle.core.localization.ILanguageFile;
import com.github.puzzle.core.localization.LanguageManager;
import com.github.puzzle.core.localization.files.LanguageFileVersion1;
import com.github.puzzle.core.resources.ResourceLocation;
import com.github.puzzle.game.events.OnPreLoadAssetsEvent;
import com.github.puzzle.game.events.OnRegisterZoneGenerators;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.loader.entrypoint.interfaces.ModInitializer;
import finalforeach.cosmicreach.entities.EntityCreator;
import org.example.exmod.commands.Commands;
import org.example.exmod.entity.BasicPhysicsEntity;
import org.example.exmod.entity.WorldCube;
import org.example.exmod.items.MoonScepter;
import org.example.exmod.mesh.LoneThread;
import org.example.exmod.util.NativeLibraryLoader;
import org.example.exmod.worldgen.SuperFlat;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

public class ExampleMod implements ModInitializer {

    public static LoneThread thread = new LoneThread();

    @Override
    public void onInit() {
        PuzzleRegistries.EVENT_BUS.register(this);
        Commands.register();

        EntityCreator.registerEntityCreator(Constants.MOD_ID + ":entity", () -> new WorldCube());
        EntityCreator.registerEntityCreator("base:test", BasicPhysicsEntity::new);

        IModItem.registerItem(new MoonScepter());

        boolean success = NativeLibraryLoader.loadLibbulletjme("Debug", "Sp");
        if (!success) {
            throw new RuntimeException("Failed to load native library. Please contact nab138, he may need to add support for your platform.");
        }
    }

    @Subscribe
    public void onEvent(OnRegisterZoneGenerators event) {
        event.registerGenerator(SuperFlat::new);
    }

    @Subscribe
    public void onEvent(OnPreLoadAssetsEvent event) {
        ILanguageFile lang = null;
        try {
            lang = LanguageFileVersion1.loadLanguageFromString(new ResourceLocation(Constants.MOD_ID, "languages/en-US.json").locate().readString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LanguageManager.registerLanguageFile(lang);
    }

}
