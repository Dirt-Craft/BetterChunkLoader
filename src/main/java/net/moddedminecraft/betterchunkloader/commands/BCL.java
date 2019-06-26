package net.moddedminecraft.betterchunkloader.commands;

import net.dirtcraft.discord.spongediscordlib.SpongeDiscordLib;
import net.moddedminecraft.betterchunkloader.BetterChunkLoader;
import net.moddedminecraft.betterchunkloader.Permissions;
import net.moddedminecraft.betterchunkloader.Utilities;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.List;

public class BCL implements CommandExecutor {

    private final BetterChunkLoader plugin;

    public BCL(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource sender, CommandContext commandContext) throws CommandException {
        showHelp(sender);
        return CommandResult.success();
    }

    void showHelp(CommandSource sender) {
        List<Text> contents = new ArrayList<>();
        StringBuilder listString = new StringBuilder();

        if (sender.hasPermission(Permissions.COMMAND_LIST+".online") && sender.hasPermission(Permissions.COMMAND_LIST+".alwayson")) {
            listString.append("[online/offline] ");
        } else {
            if (sender.hasPermission(Permissions.COMMAND_LIST+".online")) {
                listString.append("[online] ");
            } else if (sender.hasPermission(Permissions.COMMAND_LIST+".alwayson")) {
                listString.append("[offline] ");
            }
        }

        if (sender.hasPermission(Permissions.COMMAND_LIST+".others")) {
            listString.append("[player]");
        }

        if (SpongeDiscordLib.getServerName().toLowerCase().contains("pixel")) {
            contents.add(buildCommand("&5/bcl &dhelp"));
        }
        if (sender.hasPermission(Permissions.COMMAND_BALANCE + ".base")) contents.add(buildCommand("&3/bcl &bbalance | bal"));
        if (sender.hasPermission(Permissions.COMMAND_CHUNKS)) contents.add(buildCommand("&3/bcl &bchunks [add/set/offline] [player] [online/offline]"));
        if (sender.hasPermission(Permissions.COMMAND_DELETE)) contents.add(buildCommand("&3/bcl &bdelete [online/alwayson] [player]"));
        if (sender.hasPermission(Permissions.COMMAND_INFO)) contents.add(buildCommand("&3/bcl &binfo"));
        if (sender.hasPermission(Permissions.COMMAND_LIST+".base")) contents.add(buildCommand("&3/bcl &blist " + listString));
        if (sender.hasPermission(Permissions.COMMAND_PURGE)) contents.add(buildCommand("&3/bcl &bpurge"));
        if (sender.hasPermission(Permissions.COMMAND_RELOAD)) contents.add(buildCommand("&3/bcl &breload [core/messages/data]"));

        if (contents.isEmpty()) {
            contents.add(Utilities.parseMessage("&cYou currently do not have any permissions for this plugin."));
        }

        plugin.getPaginationService().builder()
                //.title(Utilities.parseMessage(plugin.getConfig().getMessages().usageTitle))
                .title(Utilities.format("&cDirtCraft &6ChunkLoading"))
                .contents(contents)
                .padding(Utilities.parseMessage(plugin.getConfig().getMessages().usagePadding))
                .sendTo(sender);
    }

    private Text buildCommand(String command) {
        String noColorCodes = TextSerializers.FORMATTING_CODE.stripCodes(command);
        return Text.builder()
                .append(Utilities.format(command))
                .onHover(TextActions.showText(Utilities.format("&5&nClick Me&7 to execute &6&o" + noColorCodes)))
                .onClick(TextActions.suggestCommand(noColorCodes))
                .build();
    }
}
