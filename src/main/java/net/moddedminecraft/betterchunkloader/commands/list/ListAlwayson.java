package net.moddedminecraft.betterchunkloader.commands.list;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Permissions;
import net.moddedminecraft.betterchunkloader.Utilities;
import net.moddedminecraft.betterchunkloader.data.ChunkLoader;
import net.moddedminecraft.betterchunkloader.data.PlayerData;
import net.moddedminecraft.betterchunkloader.menu.Menu;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.function.Consumer;

public class ListAlwayson implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public ListAlwayson(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {
        Optional<User> playerName = commandContext.<User>getOne("player");

        List<ChunkLoader> chunkLoaders = new ArrayList<>();

        if (playerName.isPresent()) {
            if (sender.hasPermission(Permissions.COMMAND_LIST + ".others")) {
                Optional<UUID> playerUUID = Utilities.getPlayerUUID(playerName.get().getName());
                if (playerUUID.isPresent()) {
                    chunkLoaders = plugin.getDataStore().getChunkLoadersByType(playerUUID.get(), true);
                } else {
                    sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().noPlayerExists));
                    return CommandResult.empty();
                }
            } else {
                sender.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().prefix + plugin.getConfig().getMessages().chunksListNoPermission));
            }
        } else {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                chunkLoaders = plugin.getDataStore().getChunkLoadersByType(player.getUniqueId(), true);
            } else {
                chunkLoaders = plugin.getDataStore().getChunkLoadersByType(true);
            }
        }

        List<Text> readableCLs = new ArrayList<>();
        for(ChunkLoader chunkLoader : chunkLoaders) {
            readableCLs.add(getReadableChunkLoader(chunkLoader, sender));
        }
        if (readableCLs.isEmpty()) {
            readableCLs.add(Utilities.parseMessage(plugin.getConfig().getMessages().chunksListNoChunkLoadersFound));
        }

        plugin.getPaginationService().builder()
                .contents(readableCLs)
                .title(Utilities.format("&bOffline &7Dirt&8-&7Loaders"))
                .padding(Utilities.parseMessage(plugin.getConfig().getMessages().chunksListPadding))
                .sendTo(sender);

        return CommandResult.success();
    }

    public Text getReadableChunkLoader(ChunkLoader chunkLoader, CommandSource sender) {
        Optional<PlayerData> playerData = plugin.getDataStore().getPlayerDataFor(chunkLoader.getOwner());

        String type = chunkLoader.isAlwaysOn() ? "Offline" : "Online";
        String loaded = chunkLoader.isLoadable() ? "&aTrue" : "&cFalse";
        String playerName = "null";


        if (playerData.isPresent()) {
            playerName = playerData.get().getName();
        }

        /*HashMap<String, String> args = new HashMap<>();
        args.put("ownerabr", StringUtils.abbreviate(playerName, 11));
        args.put("owner", playerName);
        args.put("type", type);
        args.put("location", Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation()));
        args.put("radius", chunkLoader.getRadius().toString());
        args.put("chunks", chunkLoader.getChunks().toString());
        args.put("loaded", loaded);
        args.put("server", SpongeDiscordLib.getServerName());*/

        ArrayList<String> hover = new ArrayList<>();
        hover.add("&7Owner&8: &6" + playerName);
        hover.add("&7Type&8: &6" + type);
        hover.add("&7Location&8: &6" +
                Utilities.getReadableLocation(chunkLoader.getWorld(), chunkLoader.getLocation())
                        .replace("world", "World"));
        hover.add("&7Radius&8: &6" + chunkLoader.getRadius().toString());
        hover.add("&7Chunks&8: &6" + chunkLoader.getChunks().toString());
        hover.add("&7Loaded&8: " + loaded);

        Text.Builder send = Text.builder();

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (chunkLoader.canEdit(player)) {
                send.append(Text.builder().append(Utilities.format("&7[&bEdit&7]"))
                        .onClick(TextActions.executeCallback(editLoader(chunkLoader)))
                        .onHover(TextActions.showText(Utilities.format("&5&nClick Me&7 to edit this &6Dirt Loader"))).build());
                send.append(Utilities.parseMessage("&r &8- "));
            }
        }

        if (sender.hasPermission(Permissions.TELEPORT)) {
            send.append(Utilities.format("&6" + playerName))
                    .onHover(TextActions.showText(Utilities.format(String.join("\n", hover))))
                    .onClick(TextActions.executeCallback(teleportTo(chunkLoader.getWorld(), chunkLoader.getLocation())));
        } else {
            send.append(Utilities.format("&6" + playerName))
                    .onHover(TextActions.showText(Utilities.format(String.join("\n", hover))));
        }

        return send.build();
    }

    private Consumer<CommandSource> editLoader(ChunkLoader chunkLoader) {
        return consumer -> {
            Player player = (Player) consumer;
            new Menu(plugin).showMenu(player, chunkLoader);
        };
    }

    private Consumer<CommandSource> teleportTo(UUID worldUUID, Vector3i vector3i) {
        return consumer -> {
            Player player = (Player) consumer;
            World world = null;
            if (Sponge.getServer().getWorld(worldUUID).isPresent()) {
                world = Sponge.getServer().getWorld(worldUUID).get();
            }
            if (world != null) {
                Location loc = new Location(world, vector3i);
                Vector3d vect = new Vector3d(0, 0, 0);
                player.setLocationAndRotation(loc, vect);

                HashMap<String, String> args = new HashMap<>();
                args.put("location", Utilities.getReadableLocation(worldUUID, vector3i));

                player.sendMessage(Utilities.parseMessage(plugin.getConfig().getMessages().chunksListTeleport, args));
            }
        };
    }
}
