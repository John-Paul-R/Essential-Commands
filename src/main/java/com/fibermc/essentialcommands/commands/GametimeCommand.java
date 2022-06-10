package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.EssentialCommands;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jpcode.eccore.util.TextUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class GametimeCommand implements Command<ServerCommandSource> {

    private final String modVersion = EssentialCommands.MOD_METADATA.getVersion().getFriendlyString();
    private static final int ticksPerSecond = 20;
    private static final int secondsPerMinute = 60;
    private static final int minutesPerHour = 60;
    private static final int minutesPerMinecraftDay = 20;
    private static final double ticksPerDay = ticksPerSecond * secondsPerMinute * minutesPerMinecraftDay;
    private static final int hoursPerDay = 24;
    private static final int minutesPerDay = hoursPerDay * minutesPerHour;

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        context.getSource().sendFeedback(TextUtil.concat(
            getFormattedTime(context.getSource().getWorld().getTimeOfDay())
        ), false);

        return 0;
    }

    private static String formatGameTimeOfDay(long tickTime) {
        int tickTimeOfDay = (int)(tickTime % 24000L);
        double dayPercentComplete = tickTimeOfDay / ticksPerDay;
        int dayHoursComplete = (int)(dayPercentComplete * hoursPerDay);
        int dayMinutesComplete = (int)Math.round((dayPercentComplete - ((double)dayHoursComplete / hoursPerDay)) * minutesPerDay);
        return String.format(
            "%02d:%02d",
            dayHoursComplete,
            dayMinutesComplete
        );
    }

    private static Text getFormattedTime(long time) {
        return Text.translatable(
                "commands.time.query",
                Text.literal(formatGameTimeOfDay(time)).setStyle(CONFIG.FORMATTING_ACCENT.getValue()))
            .setStyle(CONFIG.FORMATTING_DEFAULT.getValue()
                .withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(String.valueOf(time % 24000L)))));
    }


}
