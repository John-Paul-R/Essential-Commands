package com.fibermc.essentialcommands;

import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.function.Predicate;

import com.fibermc.essentialcommands.commands.*;
import com.fibermc.essentialcommands.commands.bench.*;
import com.fibermc.essentialcommands.commands.suggestions.ListSuggestion;
import com.fibermc.essentialcommands.commands.suggestions.NicknamePlayersSuggestion;
import com.fibermc.essentialcommands.commands.suggestions.TeleportResponseSuggestion;
import com.fibermc.essentialcommands.commands.suggestions.WarpSuggestion;
import com.fibermc.essentialcommands.commands.utility.*;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.types.NamedMinecraftLocation;
import com.fibermc.essentialcommands.util.EssentialsConvertor;
import com.fibermc.essentialcommands.util.EssentialsXParser;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.util.IConsumer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.fibermc.essentialcommands.EssentialCommands.BACKING_CONFIG;
import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Primary registry class for EssentialCommands.
 * Contains logic for building the brigaider command trees, and registers
 * required permissions for each node.
 */
public final class EssentialCommandRegistry {
    private EssentialCommandRegistry() {}

    public static void register(
        CommandDispatcher<ServerCommandSource> dispatcher,
        CommandRegistryAccess commandRegistryAccess,
        CommandManager.RegistrationEnvironment registrationEnvironment
    ) {
        RootCommandNode<ServerCommandSource> rootNode = dispatcher.getRoot();

        LiteralCommandNode<ServerCommandSource> essentialCommandsRootNode;
        {
            LiteralCommandNode<ServerCommandSource> ecInfoNode = CommandManager.literal("info")
                .executes(new ModInfoCommand())
                .build();

            essentialCommandsRootNode = CommandManager.literal("essentialcommands")
                .executes(ecInfoNode.getCommand())
                .build();

            essentialCommandsRootNode.addChild(ecInfoNode);
        }

        var excludedTopLevelCommands = new HashSet<>(CONFIG.EXCLUDED_TOP_LEVEL_COMMANDS);
        IConsumer<LiteralCommandNode<ServerCommandSource>> registerNode = CONFIG.REGISTER_TOP_LEVEL_COMMANDS
            ? (node) -> {
                if (excludedTopLevelCommands.contains(node.getLiteral())) {
                    excludedTopLevelCommands.remove(node.getLiteral());
                } else {
                    rootNode.addChild(node);
                }
                essentialCommandsRootNode.addChild(node);
            }
            : essentialCommandsRootNode::addChild;

        if (CONFIG.ENABLE_TPA) {
            registerNode.accept(CommandManager.literal("tpa")
                .requires(ECPerms.require(ECPerms.Registry.tpa, 0))
                .then(CommandUtil.targetPlayerArgument()
                    .executes(new TeleportAskCommand()))
                .build());

            registerNode.accept(CommandManager.literal("tpcancel")
                .requires(ECPerms.require(ECPerms.Registry.tpa, 0))
                .executes(new TeleportCancelCommand())
                .build());

            registerNode.accept(CommandManager.literal("tpaccept")
                .requires(ECPerms.require(ECPerms.Registry.tpaccept, 0))
                .executes(new TeleportAcceptCommand()::runDefault)
                .then(CommandUtil.targetPlayerArgument()
                    .suggests(TeleportResponseSuggestion.STRING_SUGGESTIONS_PROVIDER)
                    .executes(new TeleportAcceptCommand()))
                .build());

            registerNode.accept(CommandManager.literal("tpdeny")
                .requires(ECPerms.require(ECPerms.Registry.tpdeny, 0))
                .executes(new TeleportDenyCommand()::runDefault)
                .then(CommandUtil.targetPlayerArgument()
                    .suggests(TeleportResponseSuggestion.STRING_SUGGESTIONS_PROVIDER)
                    .executes(new TeleportDenyCommand()))
                .build());

            registerNode.accept(CommandManager.literal("tpahere")
                .requires(ECPerms.require(ECPerms.Registry.tpahere, 0))
                .then(CommandUtil.targetPlayerArgument()
                    .executes(new TeleportAskHereCommand()))
                .build());
        }

        if (CONFIG.ENABLE_HOME) {
            LiteralArgumentBuilder<ServerCommandSource> homeBuilder = CommandManager.literal("home");
            LiteralArgumentBuilder<ServerCommandSource> homeSetBuilder = CommandManager.literal("set");
            LiteralArgumentBuilder<ServerCommandSource> homeTpBuilder = CommandManager.literal("tp");
            LiteralArgumentBuilder<ServerCommandSource> homeTpOtherBuilder = CommandManager.literal("tp_other");
            LiteralArgumentBuilder<ServerCommandSource> homeTpOfflineBuilder = CommandManager.literal("tp_offline");
            LiteralArgumentBuilder<ServerCommandSource> homeDeleteBuilder = CommandManager.literal("delete");
            LiteralArgumentBuilder<ServerCommandSource> homeListBuilder = CommandManager.literal("list");
            LiteralArgumentBuilder<ServerCommandSource> homeListOfflineBuilder = CommandManager.literal("list_offline");
            LiteralArgumentBuilder<ServerCommandSource> homeOverwriteBuilder = CommandManager.literal("overwritehome");

            homeSetBuilder
                .requires(ECPerms.require(ECPerms.Registry.home_set, 0))
                .executes(new HomeSetCommand()::runDefault)
                .then(argument("home_name", StringArgumentType.word())
                    .executes(new HomeSetCommand()));

            homeTpBuilder
                .requires(ECPerms.require(ECPerms.Registry.home_tp, 0))
                .executes(new HomeCommand()::runDefault)
                .then(argument("home_name", StringArgumentType.word())
                    .suggests(HomeCommand.Suggestion.LIST_SUGGESTION_PROVIDER)
                    .executes(new HomeCommand()));

            homeTpOtherBuilder
                .requires(ECPerms.require(ECPerms.Registry.home_tp_others, 2))
                .then(argument("target_player", EntityArgumentType.player())
                    .then(argument("home_name", StringArgumentType.word())
                        .suggests(HomeTeleportOtherCommand.Suggestion.LIST_SUGGESTION_PROVIDER)
                        .executes(new HomeTeleportOtherCommand())));

            homeTpOfflineBuilder
                .requires(ECPerms.require(ECPerms.Registry.home_tp_others, 2))
                .then(argument("target_player", StringArgumentType.word())
                    .then(argument("home_name", StringArgumentType.word())
                        .executes(new HomeTeleportOtherCommand()::runOfflinePlayer)));

            homeDeleteBuilder
                .requires(ECPerms.require(ECPerms.Registry.home_delete, 0))
                .then(argument("home_name", StringArgumentType.word())
                    .suggests(HomeCommand.Suggestion.LIST_SUGGESTION_PROVIDER)
                    .executes(new HomeDeleteCommand()));

            homeListBuilder
                .requires(ECPerms.require(ECPerms.Registry.home_tp, 0))
                .executes(ListCommandFactory.create(
                    ECText.getInstance().getString("cmd.home.list.start"),
                    "home tp",
                    HomeCommand.Suggestion::getSuggestionEntries));

            homeListOfflineBuilder
                .requires(ECPerms.require(ECPerms.Registry.home_tp_others, 2))
                .then(argument("target_player", StringArgumentType.word())
                    .executes(HomeTeleportOtherCommand::runListOffline));

            homeOverwriteBuilder
                .requires(ECPerms.require(ECPerms.Registry.home_set, 0))
                .then(argument("home_name", StringArgumentType.word())
                .executes(new HomeOverwriteCommand()));

            LiteralCommandNode<ServerCommandSource> homeNode = homeBuilder
                .requires(ECPerms.requireAny(ECPerms.Registry.Group.home_group, 0))
                .build();
            homeNode.addChild(homeTpBuilder.build());
            homeNode.addChild(homeTpOtherBuilder.build());
            homeNode.addChild(homeTpOfflineBuilder.build());
            homeNode.addChild(homeSetBuilder.build());
            homeNode.addChild(homeDeleteBuilder.build());
            homeNode.addChild(homeListBuilder.build());
            homeNode.addChild(homeListOfflineBuilder.build());

            registerNode.accept(homeNode);

            essentialCommandsRootNode.addChild(homeOverwriteBuilder.build());
        }

        //Back
        if (CONFIG.ENABLE_BACK) {
            LiteralArgumentBuilder<ServerCommandSource> backBuilder = CommandManager.literal("back");
            backBuilder
                .requires(ECPerms.require(ECPerms.Registry.back, 0))
                .executes(new BackCommand());

            LiteralCommandNode<ServerCommandSource> backNode = backBuilder.build();

            rootNode.addChild(backNode);
            essentialCommandsRootNode.addChild(backNode);
        }

        //Warp
        if (CONFIG.ENABLE_WARP) {
            LiteralArgumentBuilder<ServerCommandSource> warpBuilder = CommandManager.literal("warp");
            LiteralArgumentBuilder<ServerCommandSource> warpSetBuilder = CommandManager.literal("set");
            LiteralArgumentBuilder<ServerCommandSource> warpTpBuilder = CommandManager.literal("tp");
            LiteralArgumentBuilder<ServerCommandSource> warpTpOtherBuilder = CommandManager.literal("tp_other");
            LiteralArgumentBuilder<ServerCommandSource> warpDeleteBuilder = CommandManager.literal("delete");
            LiteralArgumentBuilder<ServerCommandSource> warpListBuilder = CommandManager.literal("list");

            warpSetBuilder
                .requires(ECPerms.require(ECPerms.Registry.warp_set, 4))
                .then(argument("warp_name", StringArgumentType.word())
                    .executes(new WarpSetCommand())
                    .then(argument("requires_permission", BoolArgumentType.bool())
                        .executes(new WarpSetCommand())));

            warpTpBuilder
                .requires(ECPerms.require(ECPerms.Registry.warp_tp, 0))
                .then(argument("warp_name", StringArgumentType.word())
                    .suggests(WarpSuggestion.STRING_SUGGESTIONS_PROVIDER)
                    .executes(new WarpTpCommand()));

            warpTpOtherBuilder
                .requires(ECPerms.require(ECPerms.Registry.home_tp_others, 2))
                .then(argument("target_player", EntityArgumentType.player())
                    .then(argument("warp_name", StringArgumentType.word())
                        .suggests(WarpSuggestion.STRING_SUGGESTIONS_PROVIDER)
                        .executes(new WarpTpCommand()::runOther)));

            warpDeleteBuilder
                .requires(ECPerms.require(ECPerms.Registry.warp_delete, 4))
                .then(argument("warp_name", StringArgumentType.word())
                    .suggests(WarpSuggestion.STRING_SUGGESTIONS_PROVIDER)
                    .executes(new WarpDeleteCommand()));

            warpListBuilder
                .requires(ECPerms.require(ECPerms.Registry.warp_tp, 0))
                .executes(ListCommandFactory.create(
                    ECText.getInstance().getString("cmd.warp.list.start"),
                    "warp tp",
                    (context) -> ManagerLocator.getInstance().getWorldDataManager().getAccessibleWarps(context.getSource().getPlayerOrThrow()).toList(),
                    NamedMinecraftLocation::getName
                ));

            LiteralCommandNode<ServerCommandSource> warpNode = warpBuilder
                .requires(ECPerms.requireAny(ECPerms.Registry.Group.warp_group, 0))
                .build();
            warpNode.addChild(warpTpBuilder.build());
            warpNode.addChild(warpTpOtherBuilder.build());
            warpNode.addChild(warpSetBuilder.build());
            warpNode.addChild(warpDeleteBuilder.build());
            warpNode.addChild(warpListBuilder.build());

            registerNode.accept(warpNode);
        }

        //Spawn
        if (CONFIG.ENABLE_SPAWN) {
            LiteralArgumentBuilder<ServerCommandSource> spawnBuilder = CommandManager.literal("spawn");
            LiteralArgumentBuilder<ServerCommandSource> spawnSetBuilder = CommandManager.literal("set");
            LiteralArgumentBuilder<ServerCommandSource> spawnTpBuilder = CommandManager.literal("tp");

            spawnSetBuilder
                .requires(ECPerms.require(ECPerms.Registry.spawn_set, 4))
                .executes(new SpawnSetCommand());

            SpawnCommand cmd = new SpawnCommand();
            spawnBuilder
                .requires(ECPerms.require(ECPerms.Registry.spawn_tp, 0))
                .executes(cmd);
            spawnTpBuilder
                .requires(ECPerms.require(ECPerms.Registry.spawn_tp, 0))
                .executes(cmd);

            LiteralCommandNode<ServerCommandSource> spawnNode = spawnBuilder.build();
            spawnNode.addChild(spawnSetBuilder.build());
            spawnNode.addChild(spawnTpBuilder.build());

            registerNode.accept(spawnNode);
        }

        if (CONFIG.ENABLE_NICK) {
            LiteralArgumentBuilder<ServerCommandSource> nickBuilder = CommandManager.literal("nickname");
            LiteralArgumentBuilder<ServerCommandSource> nickSetBuilder = CommandManager.literal("set");
            LiteralArgumentBuilder<ServerCommandSource> nickClearBuilder = CommandManager.literal("clear");
            LiteralArgumentBuilder<ServerCommandSource> nickRevealBuilder = CommandManager.literal("reveal");

            Predicate<ServerCommandSource> permissionSelf = ECPerms.require(ECPerms.Registry.nickname_self, 2);
            Predicate<ServerCommandSource> permissionOther = ECPerms.require(ECPerms.Registry.nickname_others, 2);
            nickSetBuilder.requires(permissionSelf)
                .then(argument("nickname", TextArgumentType.text())
                    .executes(new NicknameSetCommand())
                ).then(CommandUtil.targetPlayerArgument()
                    .requires(permissionOther)
                    .then(argument("nickname", TextArgumentType.text())
                        .executes(new NicknameSetCommand())
                    ).then(argument("nickname_placeholder_api", StringArgumentType.greedyString())
                        .executes(NicknameSetCommand::runStringToText)
                    )
                )
                .then(argument("nickname_placeholder_api", StringArgumentType.greedyString())
                    .executes(NicknameSetCommand::runStringToText)
                );

            nickClearBuilder
                .requires(ECPerms.require(ECPerms.Registry.nickname_self, 2))
                .executes(new NicknameClearCommand())
                .then(CommandUtil.targetPlayerArgument()
                    .requires(ECPerms.require(ECPerms.Registry.nickname_others, 2))
                    .executes(new NicknameClearCommand()));

            nickRevealBuilder
                .requires(ECPerms.require(ECPerms.Registry.nickname_reveal, 2))
                .then(argument("player_nickname", StringArgumentType.word())
                    .suggests(NicknamePlayersSuggestion.STRING_SUGGESTIONS_PROVIDER)
                    .executes(new RealNameCommand())
                );

            LiteralCommandNode<ServerCommandSource> nickNode = nickBuilder
                .requires(ECPerms.requireAny(ECPerms.Registry.Group.nickname_group, 2))
                .build();
            nickNode.addChild(nickSetBuilder.build());
            nickNode.addChild(nickClearBuilder.build());
            nickNode.addChild(nickRevealBuilder.build());

            registerNode.accept(nickNode);
        }

        if (CONFIG.ENABLE_RTP) {
            registerNode.accept(CommandManager.literal("randomteleport")
                .requires(ECPerms.require(ECPerms.Registry.randomteleport, 2))
                .executes(new RandomTeleportCommand())
                .build());

            registerNode.accept(CommandManager.literal("rtp")
                .requires(ECPerms.require(ECPerms.Registry.randomteleport, 2))
                .executes(new RandomTeleportCommand())
                .build()
            );
        }

        if (CONFIG.ENABLE_FLY) {
            LiteralArgumentBuilder<ServerCommandSource> flyBuilder = CommandManager.literal("fly");
            LiteralArgumentBuilder<ServerCommandSource> flySpeedBuilder = CommandManager.literal("speed");

            Predicate<ServerCommandSource> permissionSelf = ECPerms.require(ECPerms.Registry.fly_self, 2);
            Predicate<ServerCommandSource> permissionOther = ECPerms.require(ECPerms.Registry.fly_others, 2);

            flyBuilder
                .requires(permissionSelf)
                .executes(new FlyCommand())
                .then(argument("flight_enabled", BoolArgumentType.bool())
                    .executes(new FlyCommand()))
                .then(CommandUtil.targetPlayerArgument()
                    .requires(permissionOther)
                    .then(argument("flight_enabled", BoolArgumentType.bool())
                        .executes(new FlyCommand())));

            flySpeedBuilder
                .requires(permissionSelf)
                .then(CommandManager.literal("reset")
                    .executes(new FlySpeedCommand()::reset))
                .then(argument("fly_speed", IntegerArgumentType.integer(0))
                    .executes(new FlySpeedCommand()))
                .then(CommandUtil.targetPlayerArgument()
                    .requires(permissionOther)
                    .then(CommandManager.literal("reset")
                        .executes(new FlySpeedCommand()::reset))
                    .then(argument("fly_speed", IntegerArgumentType.integer(0))
                        .executes(new FlySpeedCommand())));

            LiteralCommandNode<ServerCommandSource> flyNode = flyBuilder.build();
            flyNode.addChild(flySpeedBuilder.build());

            registerNode.accept(flyNode);
        }

        if (CONFIG.ENABLE_INVULN) {
            registerNode.accept(
                CommandManager.literal("invuln")
                    .requires(ECPerms.require(ECPerms.Registry.invuln_self, 2))
                    .executes(new InvulnCommand())
                    .then(CommandUtil.targetPlayerArgument()
                        .requires(ECPerms.require(ECPerms.Registry.invuln_others, 2))
                        .then(argument("invuln_enabled", BoolArgumentType.bool())
                            .executes(new InvulnCommand())))
                    .build());
        }

        if (CONFIG.ENABLE_WORKBENCH) {
            registerNode.accept(CommandManager.literal("workbench")
                .requires(ECPerms.require(ECPerms.Registry.workbench, 0))
                .executes(new WorkbenchCommand())
                .build());

            registerNode.accept(CommandManager.literal("stonecutter")
                .requires(ECPerms.require(ECPerms.Registry.stonecutter, 0))
                .executes(new StonecutterCommand())
                .build());

            registerNode.accept(CommandManager.literal("grindstone")
                .requires(ECPerms.require(ECPerms.Registry.grindstone, 0))
                .executes(new GrindstoneCommand())
                .build());
        }

        if (CONFIG.ENABLE_ANVIL) {
            registerNode.accept(CommandManager.literal("anvil")
                .requires(ECPerms.require(ECPerms.Registry.anvil, 0))
                .executes(new AnvilCommand())
                .build());
        }

        if (CONFIG.ENABLE_ENDERCHEST) {
            registerNode.accept(CommandManager.literal("enderchest")
                    .requires(ECPerms.require(ECPerms.Registry.enderchest, 0))
                    .executes(new EnderchestCommand())
                .build());
        }

        if (CONFIG.ENABLE_WASTEBIN) {
            registerNode.accept(CommandManager.literal("wastebin")
                .requires(ECPerms.require(ECPerms.Registry.wastebin, 0))
                .executes(new WastebinCommand())
                .build());
        }

        if (CONFIG.ENABLE_TOP) {
            registerNode.accept(CommandManager.literal("top")
                .requires(ECPerms.require(ECPerms.Registry.top, 2))
                .executes(new TopCommand())
                .build());
        }

        if (CONFIG.ENABLE_GAMETIME) {
            registerNode.accept(CommandManager.literal("gametime")
                .requires(ECPerms.require(ECPerms.Registry.gametime, 0))
                .executes(new GametimeCommand())
                .build());
        }

        if (CONFIG.ENABLE_AFK) {
            registerNode.accept(CommandManager.literal("afk")
                .requires(ECPerms.require(ECPerms.Registry.afk, 0))
                .executes(new AfkCommand())
                .build());
        }

        if (CONFIG.ENABLE_BED) {
            registerNode.accept(CommandManager.literal("bed")
                .requires(ECPerms.require(ECPerms.Registry.bed, 0))
                .executes(new BedCommand())
                .build());
        }

        registerNode.accept(CommandManager.literal("lastPos")
            .requires(ECPerms.require("essentialcommands.admin.lastpos", 2))
                .then(argument("target_player", StringArgumentType.word())
                .executes((context) -> {
                    var targetPlayerName = StringArgumentType.getString(context, "target_player");
                    ManagerLocator.getInstance()
                        .getOfflinePlayerRepo()
                        .getOfflinePlayerByNameAsync(targetPlayerName)
                        .whenComplete((playerEntity, err) -> {
                            if (playerEntity == null) {
                                context.getSource().sendError(Text.of("No player with the specified name found."));
                                return;
                            }
                            context.getSource().sendFeedback(() ->
                                Text.of(playerEntity.getPos().toString()),
                                EssentialCommands.CONFIG.BROADCAST_TO_OPS);
                        });
                    return 1;
                }))
            .build());

        if (CONFIG.ENABLE_DAY) {
            registerNode.accept(CommandManager.literal("day")
                .requires(ECPerms.require(ECPerms.Registry.time_set_day, 2))
                .executes(new DayCommand())
                .build());
        }

        if (CONFIG.ENABLE_RULES) {
            registerNode.accept(CommandManager.literal("rules")
                .requires(ECPerms.require(ECPerms.Registry.rules, 0))
                .executes(RulesCommand::run)
                .then(literal("reload")
                    .requires(ECPerms.require(ECPerms.Registry.rules_reload, 4))
                    .executes(RulesCommand::reloadCommand))
                .build());
        }

        if (CONFIG.ENABLE_FEED) {
            registerNode.accept(CommandManager.literal("feed")
                .requires(ECPerms.require(ECPerms.Registry.feed_self, 2))
                .executes(new FeedCommand())
                .then(CommandUtil.targetPlayerArgument()
                    .requires(ECPerms.require(ECPerms.Registry.feed_others, 2))
                    .executes(new FeedCommand()))
                    .build());
        }

        if (CONFIG.ENABLE_HEAL) {
            registerNode.accept(CommandManager.literal("heal")
                .requires(ECPerms.require(ECPerms.Registry.heal_self, 2))
                .executes(new HealCommand())
                .then(CommandUtil.targetPlayerArgument()
                    .requires(ECPerms.require(ECPerms.Registry.heal_others, 2))
                    .executes(new HealCommand()))
                    .build());
        }

        if (CONFIG.ENABLE_EXTINGUISH) {
            LiteralCommandNode<ServerCommandSource> node = CommandManager.literal("extinguish")
                .requires(ECPerms.require(ECPerms.Registry.extinguish_self, 2))
                .executes(new ExtinguishCommand())
                .then(CommandUtil.targetPlayerArgument()
                    .requires(ECPerms.require(ECPerms.Registry.extinguish_others, 2))
                    .executes(new ExtinguishCommand()))
                    .build();

            registerNode.accept(node);
            registerNode.accept(CommandManager.literal("ext").redirect(node).build());
        }

        if (CONFIG.ENABLE_SUICIDE) {
            registerNode.accept(CommandManager.literal("suicide")
                .requires(ECPerms.require(ECPerms.Registry.suicide, 0))
                .executes(new SuicideCommand())
                .build());
        }

        if (CONFIG.ENABLE_NIGHT) {
            registerNode.accept(CommandManager.literal("night")
                .requires(ECPerms.require(ECPerms.Registry.time_set_night, 2))
                .executes(new NightCommand())
                .build());
        }

        if (CONFIG.ENABLE_REPAIR) {
            registerNode.accept(CommandManager.literal("repair")
                .requires(ECPerms.require(ECPerms.Registry.repair_self, 2))
                .executes(new RepairCommand())
                .then(CommandUtil.targetPlayerArgument()
                    .requires(ECPerms.require(ECPerms.Registry.repair_others, 2))
                    .executes(new RepairCommand()))
                    .build());
        }

        if (CONFIG.ENABLE_NEAR) {
            registerNode.accept(CommandManager.literal("near")
                .requires(ECPerms.require(ECPerms.Registry.near_self, 2))
                .executes(new NearCommand())
                .then(argument("range", IntegerArgumentType.integer())
                    .executes(NearCommand::withRange)
                    .then(CommandUtil.targetPlayerArgument()
                        .requires(ECPerms.require(ECPerms.Registry.near_others, 2))
                        .executes(NearCommand::withRange)))
                        .build());
        }

        if (CONFIG.ENABLE_MOTD) {
            registerNode.accept(CommandManager.literal("motd")
                .requires(ECPerms.require(ECPerms.Registry.motd, 0))
                .executes(MotdCommand::run)
                .build());
        }

        var profileNode = ProfileCommand.buildNode();
        essentialCommandsRootNode.addChild(profileNode);

        LiteralCommandNode<ServerCommandSource> configNode = CommandManager.literal("config")
            .requires(ECPerms.requireAny(ECPerms.Registry.Group.config_group, 4))
            .then(CommandManager.literal("reload")
                .executes((context) -> {
                    BACKING_CONFIG.loadOrCreateProperties();
                    var player = context.getSource().getPlayer();
                    var ecText = player != null ? ECText.access(player) : ECText.getInstance();
                    context.getSource().sendFeedback(() ->
                        ecText.getText("cmd.config.reload"),
                        true
                    );
                    return 1;
                }).requires(
                    ECPerms.require(ECPerms.Registry.config_reload, 4)
                ).build())
            .then(CommandManager.literal("display")
                .requires(ECPerms.require(ECPerms.Registry.config_reload, 4))
                .executes((context) -> {
                    BACKING_CONFIG.loadOrCreateProperties();
                    context.getSource().sendFeedback(
                        BACKING_CONFIG::stateAsText,
                        false
                    );
                    return 1;
                })
                .then(CommandManager.argument("config_property", StringArgumentType.word())
                    .suggests(ListSuggestion.of(BACKING_CONFIG::getPublicFieldNames))
                    .executes(context -> {
                        try {
                            Text t = BACKING_CONFIG.getFieldValueAsText(
                                StringArgumentType.getString(context, "config_property"));
                            context.getSource().sendFeedback(() -> t, false);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }

                        return 1;
                    })
                )
            ).build();

        essentialCommandsRootNode.addChild(configNode);

        if (true) {
            essentialCommandsRootNode.addChild(CommandManager.literal("deleteAllPlayerData")
                .requires(source -> source.hasPermissionLevel(4))
                .executes(new ClearPlayerDataCommand())
                .build()
            );
        }

        if (CONFIG.ENABLE_ESSENTIALSX_CONVERT) {
            essentialCommandsRootNode.addChild(CommandManager.literal("convertEssentialsXPlayerHomes")
                .requires(source -> source.hasPermissionLevel(4))
                .executes((source) -> {
                    Path mcDir = source.getSource().getServer().getRunDirectory().toPath();
                    try {
                        EssentialsXParser.convertPlayerDataDir(
                            mcDir.resolve("plugins/Essentials/userdata").toFile(),
                            mcDir.resolve("world/modplayerdata").toFile(),
                            source.getSource().getServer()
                        );
                        source.getSource().sendFeedback(() -> Text.literal("Successfully converted data dirs."), CONFIG.BROADCAST_TO_OPS);
                    } catch (NotDirectoryException | FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }).build()
            );
            essentialCommandsRootNode.addChild(CommandManager.literal("convertEssentialsXWarps")
                .requires(source -> source.hasPermissionLevel(4))
                .executes((source) -> {
                    Path mcDir = source.getSource().getServer().getRunDirectory().toPath();
                    EssentialsConvertor.warpConvert(
                        source.getSource().getServer(),
                        mcDir.resolve("plugins/Essentials/warps").toFile()
                    );
                    source.getSource().sendFeedback(() -> Text.literal("Successfully converted warps."), CONFIG.BROADCAST_TO_OPS);
                    return 0;
                }).build()
            );

        }

        rootNode.addChild(essentialCommandsRootNode);

        if (!excludedTopLevelCommands.isEmpty() && CONFIG.REGISTER_TOP_LEVEL_COMMANDS) {
            EssentialCommands.log(Level.ERROR, "The following commands were set to be excluded but don't exist: " + excludedTopLevelCommands);
        }
    }

}
