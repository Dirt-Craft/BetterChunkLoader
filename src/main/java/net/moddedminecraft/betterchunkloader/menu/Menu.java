package net.moddedminecraft.betterchunkloader.menu;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Permissions;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Menu {

    private final BetterChunkLoader plugin;

    private static final ItemType REMOVE_TYPE = Sponge.getRegistry().getType(ItemType.class, BetterChunkLoader.getInstance().getConfig().getCore().removeItemType).orElse(ItemTypes.REDSTONE_TORCH);
    private static final ItemType ACTIVE_TYPE = Sponge.getRegistry().getType(ItemType.class, BetterChunkLoader.getInstance().getConfig().getCore().activeItemType).orElse(ItemTypes.POTION);
    private static final ItemType INACTIVE_TYPE = Sponge.getRegistry().getType(ItemType.class, BetterChunkLoader.getInstance().getConfig().getCore().inactiveItemType).orElse(ItemTypes.GLASS_BOTTLE);

    public Menu(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    public void showMenu(Player player, ChunkLoader chunkLoader) {
        plugin.getDataStore().getPlayerDataFor(chunkLoader.getOwner()).ifPresent((playerData) -> {
            HashMap<String, String> args = new HashMap<>();
            args.put("player", playerData.getName());
            args.put("chunks", chunkLoader.getChunks().toString());

            Text mainTitle = Utilities.format(
                    "&4" + playerData.getName() + "&8 - &9" + chunkLoader.getChunks() + " &8Chunks");

            Text alwaysOnTitle = Utilities.format("&4DirtCraft&7: &8Offline Loader");
            Text onlineTitle = Utilities.format("&4DirtCraft&7: &8Online Loader");

            Text title = (chunkLoader.getRadius() != -1 ? mainTitle : chunkLoader.isAlwaysOn() ? alwaysOnTitle : onlineTitle);

            Inventory inventory = Inventory.builder()
                    .of(InventoryArchetypes.MENU_ROW)
                    .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(title)))
                    .property(MenuProperty.PROPERTY_NAME, MenuProperty.of(chunkLoader))
                    .listener(ClickInventoryEvent.class, createMenuListener(chunkLoader))
                    .build(plugin);

            if (chunkLoader.getRadius() != -1) {
                SlotPos slotPos = SlotPos.of(0, 0);
                HashMap<Key, Object> keys = new HashMap<>();
                keys.put(Keys.DISPLAY_NAME, Utilities.format("&c&oRemove &7&oDirt&8&o-&7&oLoader"));
                addMenuOption(inventory, slotPos, REMOVE_TYPE, keys, slotPos.getX(), slotPos.getY(), -1, -1);
            }

            int pos = 2;
            int maxRadius = 7;

            if (!player.hasPermission(Permissions.UNLLIMITED_CHUNKS)) maxRadius = plugin.getConfig().getCore().maxSize;

            if (maxRadius < 0) maxRadius = 0;
            if (maxRadius > 7) maxRadius = 7;

            for (int radius = 0; radius < maxRadius;) {
                Integer chunks = Double.valueOf(Math.pow((2 * radius) + 1, 2)).intValue();
                SlotPos slotPos = SlotPos.of(pos, 0);
                HashMap<Key, Object> keys = new HashMap<>();
                keys.put(Keys.DISPLAY_NAME, (chunkLoader.getRadius() == radius ? getReadableSize(radius + 1, true) : getReadableSize(radius + 1, false)));
                addMenuOption(inventory, slotPos, (chunkLoader.getRadius() == radius ? ACTIVE_TYPE : INACTIVE_TYPE), keys, slotPos.getX(), slotPos.getY(), radius, chunks);
                pos++;
                radius++;
            }
            player.openInventory(inventory);
        });
    }

    public void updateMenu(Player player, ChunkLoader chunkLoader, Inventory inventory) {

        plugin.getDataStore().getPlayerDataFor(chunkLoader.getOwner()).ifPresent((playerData) -> {
            if (chunkLoader.getRadius() != -1) {
                SlotPos slotPos = SlotPos.of(0, 0);
                HashMap<Key, Object> keys = new HashMap<>();
                keys.put(Keys.DISPLAY_NAME, Utilities.format("&c&oRemove &7&oDirt&8&o-&7&oLoader"));
                addMenuOption(inventory, slotPos, REMOVE_TYPE, keys, slotPos.getX(), slotPos.getY(), -1, -1);
            }


            int pos = 2;
            int maxRadius = 7;

            if (!player.hasPermission(Permissions.UNLLIMITED_CHUNKS)) maxRadius = plugin.getConfig().getCore().maxSize;

            if (maxRadius < 0) maxRadius = 0;
            if (maxRadius > 7) maxRadius = 7;

            for (int radius = 0; radius < maxRadius;) {
                Integer chunks = Double.valueOf(Math.pow((2 * radius) + 1, 2)).intValue();
                SlotPos slotPos = SlotPos.of(pos, 0);
                HashMap<Key, Object> keys = new HashMap<>();
                keys.put(Keys.DISPLAY_NAME, (chunkLoader.getRadius() == radius ? getReadableSize(radius + 1, true) : getReadableSize(radius + 1, false)));
                addMenuOption(inventory, slotPos, (chunkLoader.getRadius() == radius ? ACTIVE_TYPE : INACTIVE_TYPE), keys, slotPos.getX(), slotPos.getY(), radius, chunks);
                pos++;
                radius++;
            }

            player.openInventory(inventory);
        });
    }

    private Text getReadableSize(int i, boolean active) {
        String status = active ? "&9 - &d&oActive" : "";

        switch (i) {
            case 1:
                return Utilities.format("&7Size&8: &61&7x&61 &7Chunk" + status);
            case 2:
                return Utilities.format("&7Size&8: &63&7x&63 &7Chunks" + status);
            case 3:
                return Utilities.format("&7Size&8: &65&7x&65 &7Chunks" + status);
            case 4:
                return Utilities.format("&7Size&8: &67&7x&67 &7Chunk" + status);
            case 5:
                return Utilities.format("&7Size&8: &69&7x&69 &7Chunk" + status);
            case 6:
                return Utilities.format("&7Size&8: &611&7x&611 &7Chunk" + status);
            case 7:
                return Utilities.format("&7Size&8: &613&7x&613 &7Chunk" + status);
            default:
                return Utilities.format("&7Size&8: " + i);
        }
    }

    public Consumer<ClickInventoryEvent> createMenuListener(ChunkLoader chunkLoader) {
        return event -> {

        };
    }

    @Deprecated
    public void addMenuOption(Inventory inventory, SlotPos slotPos, ItemType icon, HashMap<Key, Object> keys, int x, int y, int radius, int chunks) {
        ItemStack itemStack = ItemStack.builder().itemType(icon).quantity(1).build();

        for (Map.Entry<Key, Object> entry : keys.entrySet()) {
            itemStack.offer(entry.getKey(), entry.getValue());
        }

        itemStack.toContainer()
                .set(DataQuery.of("UnsafeData","SLOTPOS1"), x)
                .set(DataQuery.of("UnsafeData","SLOTPOS2"), y);
        itemStack = ItemStack.builder().fromContainer(
                itemStack.toContainer()
                        .set(DataQuery.of("UnsafeData","SLOTPOS1"), x)
                        .set(DataQuery.of("UnsafeData","SLOTPOS2"), y))
                .build();

        if (radius >= 0) {
            itemStack = ItemStack.builder().fromContainer(
                    itemStack.toContainer()
                            .set(DataQuery.of("UnsafeData", "RADIUS"), radius))
                    .build();
        }
        if (chunks > 0) {
            itemStack = ItemStack.builder().fromContainer(
                    itemStack.toContainer()
                            .set(DataQuery.of("UnsafeData", "CHUNKS"), chunks))
                    .build();
        }

        inventory.query(slotPos).first().set(itemStack);
    }
}
