package org.example.exmod;

import com.badlogic.gdx.utils.ObjectMap;
import com.github.puzzle.core.PuzzleRegistries;
import com.github.puzzle.core.localization.ILanguageFile;
import com.github.puzzle.core.localization.LanguageManager;
import com.github.puzzle.core.localization.files.LanguageFileVersion1;
import com.github.puzzle.core.resources.ResourceLocation;
import com.github.puzzle.game.events.OnPreLoadAssetsEvent;
import com.github.puzzle.game.events.OnRegisterBlockEvent;
import com.github.puzzle.game.events.OnRegisterZoneGenerators;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.loader.entrypoint.interfaces.ModInitializer;
import finalforeach.cosmicreach.entities.EntityCreator;
import org.example.exmod.blocks.Chair;
import org.example.exmod.commands.Commands;
import org.example.exmod.entity.BasicPhysicsEntity;
import org.example.exmod.entity.BasicShipEntity;
import org.example.exmod.entity.WorldCube;
import org.example.exmod.items.MoonScepter;
import org.example.exmod.threading.MeshingThread;
import org.example.exmod.threading.PhysicsThread;
import org.example.exmod.worldgen.SuperFlat;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

public class ExampleMod implements ModInitializer {

    @Override
    public void onInit() {
        MeshingThread.init();
        PhysicsThread.init();

        PuzzleRegistries.EVENT_BUS.register(this);
        Commands.register();

        EntityCreator.registerEntityCreator("funni-blocks:entity", () -> new WorldCube());
        EntityCreator.registerEntityCreator("base:test", BasicPhysicsEntity::new);
        EntityCreator.registerEntityCreator("base:test2", BasicShipEntity::new);

        EntityCreator.registerEntityCreator(Constants.MOD_ID + ":rotating_entity", () -> new WorldCube());
        EntityCreator.registerEntityCreator(Constants.MOD_ID + ":physics_entity", BasicPhysicsEntity::new);
        EntityCreator.registerEntityCreator(Constants.MOD_ID + ":ship", BasicShipEntity::new);

        IModItem.registerItem(new MoonScepter());
    }

    @Subscribe
    public void onEvent(OnRegisterBlockEvent event) {
        event.registerBlock(Chair::new);
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
