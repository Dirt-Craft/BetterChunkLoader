package net.moddedminecraft.betterchunkloader.commands;

import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.*;

public class Info implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public Info(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) {
        List<ChunkLoader> chunkLoaders = plugin.getDataStore().getChunkLoaderData();

        if (chunkLoaders.isEmpty()) {
            sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksInfoFailure));
            return CommandResult.success();
        }

        int alwaysOnLoaders = 0, onlineOnlyLoaders = 0, alwaysOnChunks = 0, onlineOnlyChunks = 0, maxChunksCount = 0, playerCount = 0;

        HashMap<UUID, Integer> loadedChunksForPlayer = new HashMap<>();

        for (ChunkLoader chunkLoader : chunkLoaders) {
            if (chunkLoader.isAlwaysOn()) {
                alwaysOnLoaders++;
                alwaysOnChunks += chunkLoader.getChunks();
            } else {
                onlineOnlyLoaders++;
                onlineOnlyChunks += chunkLoader.getChunks();
            }

            Integer count = loadedChunksForPlayer.get(chunkLoader.getOwner());
            if (count == null) {
                count = 0;
            }
            count += chunkLoader.getChunks();
            loadedChunksForPlayer.put(chunkLoader.getOwner(), count);
        }

        playerCount = loadedChunksForPlayer.size();

        for (Map.Entry<UUID, Integer> entry : loadedChunksForPlayer.entrySet()) {
            if (maxChunksCount < entry.getValue()) {
                maxChunksCount = entry.getValue();
            }
        }

        HashMap<String, String> args = new HashMap<>();
        args.put("onlineLoaders", String.valueOf(onlineOnlyLoaders));
        args.put("onlineChunks", String.valueOf(onlineOnlyChunks));
        args.put("alwaysOnLoaders", String.valueOf(alwaysOnLoaders));
        args.put("alwaysOnChunks", String.valueOf(alwaysOnChunks));
        args.put("playerCount", String.valueOf(playerCount));

        plugin.getPaginationService().builder()
                .contents(Utilities.parseMessageList(
                        Arrays.asList("&6{onlineLoaders} &aOnline &7Loaders &7loading &6{onlineChunks} chunks&r",
                                "&6{alwaysOnLoaders} &cOffline &7Loaders &7loading &6{alwaysOnChunks} chunks&r",
                                "&6{playerCount} &7players loading chunks&r"), args))
                .title(TextSerializers.FORMATTING_CODE.deserialize("&cDirt&8-&6Loader &7Statistics"))
                .padding(Utilities.parseMessage(plugin.getConfig().getMessages().infoPadding))
                .sendTo(sender);

        return CommandResult.success();
    }
}
