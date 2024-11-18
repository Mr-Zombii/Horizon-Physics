package me.zombii.horizon.util;

import com.github.puzzle.core.Constants;
import com.github.puzzle.core.loader.meta.EnvType;
import com.github.puzzle.game.items.IModItem;
import finalforeach.cosmicreach.items.Item;
import me.zombii.horizon.items.api.I3DItem;

import static finalforeach.cosmicreach.items.Item.allItems;

public interface IItemRegistrar {

    static <T extends I3DItem & IModItem & Item> T registerItem(T item) {
        if (Constants.SIDE == EnvType.CLIENT) {
            me.zombii.horizon.Constants.ITEM_REGISTRAR_INSTANCE.registerItemINST(item);
        } else allItems.put(item.getID(), item);

        return item;
    }

    <T extends I3DItem & IModItem & Item> void registerItemINST(T item);

}
