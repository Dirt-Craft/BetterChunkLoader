package net.moddedminecraft.betterchunkloader.commands;

import net.moddedminecraft.betterchunkloader.Utilities;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.util.ArrayList;

public class Help implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) {

        ArrayList<Text> contents = new ArrayList<Text>() {{
            add(Utilities.format("\n&dChunk Loading allows for a chunk to be loaded without a player"));
            add(Utilities.format("&dThis can allow you to breed Pokémon while you're away from home\n&5&nInstructions&r &8(&6Hover&8)\n"));
        }};

        Text text = Text.builder()
                .append(contents)
                .onHover(TextActions.showText(Utilities.format(
                        "&7Step 1&8: &6Craft an Iron Block (Online) or Diamond Block (Offline)\n" +
                        "&7Step 2&8: &6Right click the Iron/Diamond Block with a blaze rod\n" +
                        "&7Step 3&8: &6Select the amount of chunks you would like to load\n" +
                        "&7Step 4&8: &bProfit???"
                )))
                .onClick(TextActions.runCommand("/bcl"))
                .build();

        PaginationList.builder()
                .title(Utilities.format("&cDirtCraft &6ChunkLoading &7Help"))
                .padding(Utilities.format("&4&m-"))
                .contents(text)
                .build()
                .sendTo(source);


        return CommandResult.success();
    }
}
