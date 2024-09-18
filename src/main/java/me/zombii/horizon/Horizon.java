package me.zombii.horizon;

import com.badlogic.gdx.utils.ObjectMap;
import com.github.puzzle.core.PuzzleRegistries;
import com.github.puzzle.core.localization.ILanguageFile;
import com.github.puzzle.core.localization.LanguageManager;
import com.github.puzzle.core.localization.files.LanguageFileVersion1;
import com.github.puzzle.core.resources.PuzzleGameAssetLoader;
import com.github.puzzle.game.engine.items.ExperimentalItemModel;
import com.github.puzzle.game.events.OnPreLoadAssetsEvent;
import com.github.puzzle.game.events.OnRegisterBlockEvent;
import com.github.puzzle.game.events.OnRegisterZoneGenerators;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.mixins.accessors.ItemRenderAccessor;
import com.github.puzzle.game.util.Reflection;
import com.github.puzzle.loader.entrypoint.interfaces.ModInitializer;
import finalforeach.cosmicreach.entities.EntityCreator;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.rendering.items.ItemModel;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import finalforeach.cosmicreach.util.Identifier;
import me.zombii.horizon.blocks.Chair;
import me.zombii.horizon.commands.Commands;
import me.zombii.horizon.entity.BasicPhysicsEntity;
import me.zombii.horizon.entity.BasicShipEntity;
import me.zombii.horizon.entity.Cube;
import me.zombii.horizon.entity.WorldCube;
import me.zombii.horizon.items.GravityGun;
import me.zombii.horizon.items.MoonScepter;
import me.zombii.horizon.items.PortalGun;
import me.zombii.horizon.items.ToolGun;
import me.zombii.horizon.items.api.I3DItem;
import me.zombii.horizon.items.model.Item3DModel;
import me.zombii.horizon.threading.MeshingThread;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.worldgen.SuperFlat;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.function.Function;

import static finalforeach.cosmicreach.items.Item.allItems;

public class Horizon implements ModInitializer {

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

        EntityCreator.registerEntityCreator(Constants.MOD_ID + ":cube", Cube::new);

        IModItem.registerItem(new MoonScepter());
        registerItem(new GravityGun());
        registerItem(new PortalGun());
        registerItem(new ToolGun());
    }

    static <T extends I3DItem & IModItem & Item> T registerItem(T item) {
        allItems.put(item.getID(), item);
        ItemRenderAccessor.getRefMap().put(item, new WeakReference(item));
        ObjectMap<Class<? extends Item>, Function<?, ItemModel>> modelCreators = Reflection.getFieldContents(ItemRenderer.class, "modelCreators");
        if (!modelCreators.containsKey((Class<? extends Item>) item.getClass())) {
            ItemRenderer.registerItemModelCreator((Class<? extends Item>) item.getClass(), (modItem) -> {
                return new Item3DModel(item).wrap();
            });
        }

        return item;
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
            lang = LanguageFileVersion1.loadLanguageFile(Objects.requireNonNull(PuzzleGameAssetLoader.locateAsset(Identifier.of(Constants.MOD_ID, "languages/en-US.json"))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LanguageManager.registerLanguageFile(lang);
    }

}
