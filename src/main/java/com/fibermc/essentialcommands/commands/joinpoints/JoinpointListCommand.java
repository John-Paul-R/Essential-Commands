package com.fibermc.essentialcommands.commands.joinpoints;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.database.JoinpointDatabase;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.types.JoinpointLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class JoinpointListCommand implements Command<ServerCommandSource> {

    public enum FilterType {
        ALL, OWNED, SHARED_WITH, GLOBAL
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity senderPlayer = context.getSource().getPlayerOrThrow();

        FilterType filter = FilterType.ALL;
        try {
            String filterArg = StringArgumentType.getString(context, "filter");
            // Convert hyphenated format to enum format
            String enumName = filterArg.toUpperCase().replace("-", "_");
            filter = FilterType.valueOf(enumName);
        } catch (IllegalArgumentException | NullPointerException ignored) {
            // Default to ALL if no filter specified or invalid filter
        }

        return exec(senderPlayer, filter);
    }

    public int runDefault(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity senderPlayer = context.getSource().getPlayerOrThrow();
        return exec(senderPlayer, FilterType.ALL);
    }

    private Consumer<Function<ECText, Text>> sendErrorToPlayer(ServerPlayerEntity senderPlayer) {
        var ecText = ECText.access(senderPlayer);

        return (messageFn) -> {
            var message = messageFn.apply(ecText);
            PlayerData.access(senderPlayer)
                .sendCommandError(message);
        };
    }

    private int exec(ServerPlayerEntity senderPlayer, FilterType filter) {
        Async.runCommand(() -> {
            PlayerData playerData = ((ServerPlayerEntityAccess) senderPlayer).ec$getPlayerData();
            JoinpointDatabase database = ManagerLocator.getInstance().getJoinpointDatabase();

            List<JoinpointLocation> joinpoints = database.getAccessibleJoinpointsWithNamesAsync(senderPlayer).join();
            List<JoinpointLocation> ownedJoinpoints = database.getOwnedJoinpointsAsync(senderPlayer.getUuid()).join();

            List<JoinpointEntry> filteredJoinpoints = filterJoinpointsAsync(joinpoints, ownedJoinpoints, senderPlayer.getUuid(), filter, database);

            if (filteredJoinpoints.isEmpty()) {
                String messageKey = switch (filter) {
                    case OWNED -> "cmd.joinpoint.list.empty.owned";
                    case SHARED_WITH -> "cmd.joinpoint.list.empty.shared_with";
                    case GLOBAL -> "cmd.joinpoint.list.empty.global";
                    case ALL -> "cmd.joinpoint.list.empty.all";
                };
                playerData.sendMessage(messageKey);
                return null;
            }

            // Send header message
            String headerKey = switch (filter) {
                case OWNED -> "cmd.joinpoint.list.header.owned";
                case SHARED_WITH -> "cmd.joinpoint.list.header.shared_with";
                case GLOBAL -> "cmd.joinpoint.list.header.global";
                case ALL -> "cmd.joinpoint.list.header.all";
            };
            playerData.sendMessage(headerKey, Text.literal(String.valueOf(filteredJoinpoints.size())));

            // Send each joinpoint with details
            var ecText = ECText.access(senderPlayer);
            for (JoinpointEntry entry : filteredJoinpoints) {
                MutableText message = Text.empty();

                // Joinpoint name (clickable for teleport)
                MutableText nameText = ecText.accent(entry.joinpoint.getName())
                    .styled(style -> style
                        .withClickEvent(new ClickEvent.SuggestCommand(
                            entry.isOwned
                                ? "/joinpoint tp " + entry.joinpoint.getName()
                                : "/joinpoint tp " + entry.ownerName + " " + entry.joinpoint.getName()
                        ))
                        .withHoverEvent(new HoverEvent.ShowText(
                            Text.literal("Click to suggest teleport command")
                        )));

                message.append("• ").append(nameText);

                // Add type indicators
                if (entry.joinpoint.isGlobal()) {
                    message.append(" ").append(Text.literal("[Global]").formatted(Formatting.GREEN));
                } else if (!entry.isOwned) {
                    message.append(" ").append(Text.literal("[Shared]").formatted(Formatting.YELLOW));
                    message.append(" ").append(Text.literal("by " + entry.ownerName).formatted(Formatting.GRAY));
                } else if (!entry.sharedWith.isEmpty()) {
                    message.append(" ").append(Text.literal("[Private+]").formatted(Formatting.BLUE));
                }

                senderPlayer.sendMessage(message);

                // For owned joinpoints, show who they're shared with
                if (entry.isOwned && !entry.sharedWith.isEmpty()) {
                    MutableText sharedText = Text.literal("    Shared with: ").formatted(Formatting.GRAY);
                    boolean first = true;
                    for (String playerName : entry.sharedWith) {
                        if (!first) sharedText.append(", ");
                        sharedText.append(Text.literal(playerName).formatted(Formatting.WHITE));
                        first = false;
                    }
                    senderPlayer.sendMessage(sharedText);
                }
            }

            return null;
        }, sendErrorToPlayer(senderPlayer));

        return SINGLE_SUCCESS;
    }

    private List<JoinpointEntry> filterJoinpointsAsync(List<JoinpointLocation> accessibleJoinpoints,
                                                       List<JoinpointLocation> ownedJoinpoints,
                                                       UUID playerUuid,
                                                       FilterType filter,
                                                       JoinpointDatabase database) {
        List<JoinpointEntry> result = new ArrayList<>();

        for (JoinpointLocation joinpoint : accessibleJoinpoints) {
            boolean isOwned = playerUuid.equals(joinpoint.getOwner());
            boolean isGlobal = joinpoint.isGlobal();
            boolean isSharedWith = !isOwned && !isGlobal;

            // Apply filter
            boolean shouldInclude = switch (filter) {
                case ALL -> true;
                case OWNED -> isOwned;
                case SHARED_WITH -> isSharedWith;
                case GLOBAL -> isGlobal;
            };

            if (!shouldInclude) continue;

            // Get owner name (already cached in JoinpointLocationWithOwnerName)
            String ownerName = "Unknown";
            if (isOwned) {
                ownerName = "You";
            } else if (joinpoint instanceof JoinpointDatabase.JoinpointLocationWithOwnerName withOwner) {
                ownerName = withOwner.getDisplayName() != null ? withOwner.getDisplayName() : withOwner.getOwnerName();
            }

            // Get shared player names for owned joinpoints using cached names
            Set<String> sharedWithNames = new HashSet<>();
            if (isOwned && !joinpoint.getSharedWith().isEmpty()) {
                getSharedWithNames(database, joinpoint, sharedWithNames);
            }

            result.add(new JoinpointEntry(joinpoint, isOwned, ownerName, sharedWithNames));
        }

        // Sort by name for consistent display
        result.sort((a, b) -> a.joinpoint.getName().compareToIgnoreCase(b.joinpoint.getName()));

        return result;
    }

    static void getSharedWithNames(JoinpointDatabase database, JoinpointLocation joinpoint, Set<String> sharedWithNames) {
        var cachedNames = database.getCachedNamesForUuidsAsync(joinpoint.getSharedWith()).join();
        for (UUID uuid : joinpoint.getSharedWith()) {
            String name = cachedNames.get(uuid);
            if (name != null) {
                sharedWithNames.add(name);
            } else {
                sharedWithNames.add(uuid.toString().substring(0, 8) + "...");
            }
        }
    }

    private static class JoinpointEntry {
        final JoinpointLocation joinpoint;
        final boolean isOwned;
        final String ownerName;
        final Set<String> sharedWith;

        JoinpointEntry(JoinpointLocation joinpoint, boolean isOwned, String ownerName, Set<String> sharedWith) {
            this.joinpoint = joinpoint;
            this.isOwned = isOwned;
            this.ownerName = ownerName;
            this.sharedWith = sharedWith;
        }
    }

    public static class Suggestion {
        public static final SuggestionProvider<ServerCommandSource> FILTER_TYPES =
            (context, builder) -> {
                builder.suggest("all");
                builder.suggest("owned");
                builder.suggest("shared-with");
                builder.suggest("global");
                return builder.buildFuture();
            };
    }
}
