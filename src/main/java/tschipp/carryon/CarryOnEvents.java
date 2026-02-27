package tschipp.carryon;

import com.google.common.eventbus.Subscribe;
import net.minecraft.Item;
import net.xiaoyu233.fml.reload.event.ItemRegistryEvent;
import net.xiaoyu233.fml.reload.utils.IdUtil;
import tschipp.carryon.items.ItemEntity;
import tschipp.carryon.items.ItemTile;

public class CarryOnEvents {

    public static Item TILE_ITEM;
    public static Item ENTITY_ITEM;

    private static final String NAMESPACE = "carryon";

    @Subscribe
    public void onItemRegister(ItemRegistryEvent event) {
        TILE_ITEM = new ItemTile(IdUtil.getNextItemID());
        event.register(NAMESPACE, NAMESPACE + ":carryon_tile", "carryon.tile_item", TILE_ITEM);

        ENTITY_ITEM = new ItemEntity(IdUtil.getNextItemID());
        event.register(NAMESPACE, NAMESPACE + ":carryon_entity", "carryon.entity_item", ENTITY_ITEM);
    }

}