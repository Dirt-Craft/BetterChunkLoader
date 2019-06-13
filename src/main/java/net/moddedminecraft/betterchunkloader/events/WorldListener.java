package net.moddedminecraft.betterchunkloader.events;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Permissions;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import net.moddedminecraft.betterchunkloader.data.PlayerData;
import net.moddedminecraft.betterchunkloader.menu.Menu;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class WorldListener {

    private final BetterChunkLoader plugin;

    public WorldListener(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    @Listener
    public void onWorldLoad(LoadWorldEvent event) {
        final List<ChunkLoader> chunks = new ArrayList<>(plugin.getDataStore().getChunkLoaders(event.getTargetWorld()));
        for (ChunkLoader chunk : chunks) {
            if (chunk.isLoadable()) {
                plugin.getChunkManager().loadChunkLoader(chunk);
            }
        }
    }

    /*@Listener
    public void onWorldUnLoad(UnloadWorldEvent event) {
        final List<ChunkLoader> chunks = new ArrayList<ChunkLoader>(plugin.getDataStore().getChunkLoaders(event.getTargetWorld()));
        for (ChunkLoader chunk : chunks) {
            if (chunk.isLoadable()) {
                plugin.getChunkManager().unloadChunkLoader(chunk);
            }
        }
    }*/

    @Listener
    public void onInteractBlockSecondary(InteractBlockEvent.Secondary.MainHand event, @Root Object cause) {
        if (!(cause instanceof Player)) return;
        Player player = (Player) cause;

        if (!player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) return;
        if (!player.getItemInHand(HandTypes.MAIN_HAND).get().getType().equals(ItemTypes.AIR)) return;

        BlockSnapshot block = event.getTargetBlock();
        if (block == null) return;

        if (!block.getState().getType().equals(ChunkLoader.ONLINE_TYPE) && !block.getState().getType().equals(ChunkLoader.ALWAYSON_TYPE)) return;

        if (!block.getLocation().isPresent()) return;
        if (!plugin.getDataStore().getChunkLoaderAt(block.getLocation().get()).isPresent()) return;

        ChunkLoader chunkLoader = plugin.getDataStore().getChunkLoaderAt(block.getLocation().get()).get();
        if (!plugin.getDataStore().getPlayerDataFor(chunkLoader.getOwner()).isPresent()) return;

        PlayerData playerData = plugin.getDataStore().getPlayerDataFor(chunkLoader.getOwner()).get();

        //Player player = event.getCause().last(Player.class).get();
        ArrayList<String> contents = new ArrayList<>();
        contents.add("&7Owner&8: &6" + playerData.getName());
        contents.add("&7Type&8: &6" + (chunkLoader.isAlwaysOn() ? "Offline" : "Online"));
        contents.add("&7Location&8: &6" + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()).replace("world", "World"));
        contents.add("&7Radius&8: &6" + chunkLoader.getRadius().toString());
        contents.add("&7Chunks&8: &6" + chunkLoader.getChunks().toString());
        contents.add("&7Loaded&8: &6" + (chunkLoader.isLoadable() ? "&aTrue" : "&cFalse"));


        PaginationList.Builder pagination = PaginationList.builder();
        pagination.title(Utilities.format("&b" + playerData.getName() + "&7's Dirt&8-&7Loader"));
        pagination.padding(Utilities.format("&4&m-"));
        pagination.contents(Utilities.format(String.join("\n", contents)));

        Optional<Player> owner = Sponge.getServer().getPlayer(chunkLoader.getOwner());
        if ((owner.isPresent() && player == owner.get()) || player.hasPermission(Permissions.EDIT)) {
            pagination.footer(
                    Text.builder()
                            .append(Utilities.format("&5&nClick Me&7 to edit &b" + playerData.getName() + "&7's &6Dirt Loader"))
                            .onHover(TextActions.showText(Utilities.format(String.join("\n", contents))))
                            .onClick(TextActions.executeCallback(editLoader(chunkLoader)))
                            .build());
        }
        pagination.build().sendTo(player);

    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event, @Root Object cause) {
        if (!(cause instanceof Player)) return;
        Player player = (Player) cause;

        BlockSnapshot block = event.getTransactions().get(0).getOriginal();
        if (block == null) return;

        if (!block.getState().getType().equals(ChunkLoader.ONLINE_TYPE) && !block.getState().getType().equals(ChunkLoader.ALWAYSON_TYPE))
            return;

        if (!plugin.getDataStore().getChunkLoaderAt(block.getLocation().get()).isPresent()) return;
        ChunkLoader chunkLoader = plugin.getDataStore().getChunkLoaderAt(block.getLocation().get()).get();

        if (!plugin.getDataStore().getPlayerDataFor(chunkLoader.getOwner()).isPresent()) return;
        PlayerData playerData = plugin.getDataStore().getPlayerDataFor(chunkLoader.getOwner()).get();

        HashMap<String, String> args = new HashMap<>();
        args.put("player", player.getName());
        args.put("playerUUID", player.getUniqueId().toString());
        args.put("ownerName", playerData.getName());
        args.put("owner", playerData.getUnqiueId().toString());
        args.put("type", chunkLoader.isAlwaysOn() ? "Offline" : "Online");
        args.put("location", Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()).replace("world", "World"));
        args.put("chunks", String.valueOf(chunkLoader.getChunks()));

        plugin.getChunkManager().unloadChunkLoader(chunkLoader);
        plugin.getDataStore().removeChunkLoader(chunkLoader.getUniqueId());

        player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().removeSuccess, args));
        Optional<Player> owner = Sponge.getServer().getPlayer(chunkLoader.getOwner());
        if (owner.isPresent() && player != owner.get()) {
            player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().ownerNotify, args));
        }
        //plugin.getLogger().info(player.getName() + " broke " + owner.get().getName() + "'s chunk loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()));
        plugin.getLogger().info(player.getName() + " broke " + owner.get().getName() + "'s " + (chunkLoader.isAlwaysOn() ? "offline" : "online") + " loader at " + Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()));

    }

    private Consumer<CommandSource> editLoader(ChunkLoader chunkLoader) {
        return consumer -> {
            Player player = (Player) consumer;
            new Menu(plugin).showMenu(player, chunkLoader);
        };
    }
}
