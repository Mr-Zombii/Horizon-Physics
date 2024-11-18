package me.zombii.horizon.util;

import com.badlogic.gdx.utils.ObjectMap;
import com.github.puzzle.core.loader.util.Reflection;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.mixins.accessors.ItemRenderAccessor;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.rendering.items.ItemModel;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import me.zombii.horizon.items.api.I3DItem;
import me.zombii.horizon.items.model.Item3DModel;

import java.lang.ref.WeakReference;
import java.util.function.Function;

import static finalforeach.cosmicreach.items.Item.allItems;

public class ItemRegistrar implements IItemRegistrar{


    @Override
    public <T extends I3DItem & IModItem & Item> void registerItemINST(T item) {
        allItems.put(item.getID(), item);
        ItemRenderAccessor.getRefMap().put(item, new WeakReference(item));
        ObjectMap<Class<? extends Item>, Function<?, ItemModel>> modelCreators = Reflection.getFieldContents(ItemRenderer.class, "modelCreators");
        if (!modelCreators.containsKey((Class<? extends Item>) item.getClass())) {
            ItemRenderer.registerItemModelCreator((Class<? extends Item>) item.getClass(), (modItem) -> {
                return new Item3DModel(item).wrap();
            });
        }
    }
}
