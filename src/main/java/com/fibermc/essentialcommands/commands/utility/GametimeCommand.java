package com.fibermc.essentialcommands.commands.utility;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.playerdata.PlayerProfile;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.essentialcommands.types.IStyleProvider;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

public class GametimeCommand implements Command<ServerCommandSource> {

    private final String modVersion = EssentialCommands.MOD_METADATA.getVersion().getFriendlyString();
    private static final int TICKS_PER_SECOND = 20;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int MINUTES_PER_HOUR = 60;
    private static final int MINUTES_PER_MINECRAFT_DAY = 20;
    private static final double TICKS_PER_DAY = TICKS_PER_SECOND * SECONDS_PER_MINUTE * MINUTES_PER_MINECRAFT_DAY;
    private static final int HOURS_PER_DAY = 24;
    private static final int MINUTES_PER_DAY = HOURS_PER_DAY * MINUTES_PER_HOUR;

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Text t = getFormattedTime(
                context.getSource().getWorld().getTimeOfDay(),
                PlayerProfile.accessFromContextOrThrow(context));
        context.getSource().sendFeedback(() -> t, false);

        return 0;
    }

    private static String formatGameTimeOfDay(long tickTime) {
        int tickTimeOfDay = (int)(tickTime % 24000L);
        double dayPercentComplete = tickTimeOfDay / TICKS_PER_DAY;
        int dayHoursComplete = (int)(dayPercentComplete * HOURS_PER_DAY);
        int dayMinutesComplete = (int)Math.round((dayPercentComplete - ((double)dayHoursComplete / HOURS_PER_DAY)) * MINUTES_PER_DAY);
        return String.format(
            "%02d:%02d",
            dayHoursComplete,
            dayMinutesComplete
        );
    }

    private static Text getFormattedTime(long time, IStyleProvider styleProvider) {
        return Text.translatable(
                "commands.time.query",
                Text.literal(formatGameTimeOfDay(time)).setStyle(styleProvider.getStyle(TextFormatType.Accent)))
            .setStyle(styleProvider.getStyle(TextFormatType.Default)
                .withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(String.valueOf(time % 24000L)))));
    }
}
