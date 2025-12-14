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

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import static com.fibermc.essentialcommands.EssentialCommands.BACKING_CONFIG;
import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * Primary registry class for EssentialCommands.
 * Contains logic for building the brigaider command trees, and registers
 * required permissions for each node.
 */
public final class EssentialCommandRegistry {
    private EssentialCommandRegistry() {}

    public static void register(
        CommandDispatcher<CommandSourceStack> dispatcher,
        CommandBuildContext commandRegistryAccess,
        Commands.CommandSelection registrationEnvironment
    ) {
        RootCommandNode<CommandSourceStack> rootNode = dispatcher.getRoot();

        LiteralCommandNode<CommandSourceStack> essentialCommandsRootNode;
        {
            LiteralCommandNode<CommandSourceStack> ecInfoNode = Commands.literal("info")
                .executes(new ModInfoCommand())
                .build();

            essentialCommandsRootNode = Commands.literal("essentialcommands")
                .executes(ecInfoNode.getCommand())
                .build();

            essentialCommandsRootNode.addChild(ecInfoNode);
        }

        var excludedTopLevelCommands = new HashSet<>(CONFIG.EXCLUDED_TOP_LEVEL_COMMANDS);
        IConsumer<LiteralCommandNode<CommandSourceStack>> registerNode = CONFIG.REGISTER_TOP_LEVEL_COMMANDS
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
            registerNode.accept(Commands.literal("tpa")
                .requires(ECPerms.require(ECPerms.Registry.tpa, 0))
                .then(CommandUtil.targetPlayerArgument()
                    .executes(new TeleportAskCommand()))
                .build());

            registerNode.accept(Commands.literal("tpcancel")
                .requires(ECPerms.require(ECPerms.Registry.tpa, 0))
                .executes(new TeleportCancelCommand())
                .build());

            registerNode.accept(Commands.literal("tpaccept")
                .requires(ECPerms.require(ECPerms.Registry.tpaccept, 0))
                .executes(new TeleportAcceptCommand()::runDefault)
                .then(CommandUtil.targetPlayerArgument()
                    .suggests(TeleportResponseSuggestion.STRING_SUGGESTIONS_PROVIDER)
                    .executes(new TeleportAcceptCommand()))
                .build());

            registerNode.accept(Commands.literal("tpdeny")
                .requires(ECPerms.require(ECPerms.Registry.tpdeny, 0))
                .executes(new TeleportDenyCommand()::runDefault)
                .then(CommandUtil.targetPlayerArgument()
                    .suggests(TeleportResponseSuggestion.STRING_SUGGESTIONS_PROVIDER)
                    .executes(new TeleportDenyCommand()))
                .build());

            registerNode.accept(Commands.literal("tpahere")
                .requires(ECPerms.require(ECPerms.Registry.tpahere, 0))
                .then(CommandUtil.targetPlayerArgument()
                    .executes(new TeleportAskHereCommand()))
                .build());
        }

        if (CONFIG.ENABLE_HOME) {
            LiteralArgumentBuilder<CommandSourceStack> homeBuilder = Commands.literal("home");
            LiteralArgumentBuilder<CommandSourceStack> homeSetBuilder = Commands.literal("set");
            LiteralArgumentBuilder<CommandSourceStack> homeTpBuilder = Commands.literal("tp");
            LiteralArgumentBuilder<CommandSourceStack> homeTpOtherBuilder = Commands.literal("tp_other");
            LiteralArgumentBuilder<CommandSourceStack> homeTpOfflineBuilder = Commands.literal("tp_offline");
            LiteralArgumentBuilder<CommandSourceStack> homeDeleteBuilder = Commands.literal("delete");
            LiteralArgumentBuilder<CommandSourceStack> homeListBuilder = Commands.literal("list");
            LiteralArgumentBuilder<CommandSourceStack> homeListOfflineBuilder = Commands.literal("list_offline");
            LiteralArgumentBuilder<CommandSourceStack> homeOverwriteBuilder = Commands.literal("overwritehome");

            homeBuilder
                .requires(ECPerms.require(ECPerms.Registry.home_tp, 0))
                .executes(new HomeCommand()::runDefault)
                .then(argument("home_name", StringArgumentType.word())
                    .suggests(HomeCommand.Suggestion.LIST_SUGGESTION_PROVIDER)
                    .executes(new HomeCommand()));

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
                .then(argument("target_player", EntityArgument.player())
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

            LiteralCommandNode<CommandSourceStack> homeNode = homeBuilder
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
            LiteralArgumentBuilder<CommandSourceStack> backBuilder = Commands.literal("back");
            backBuilder
                .requires(ECPerms.require(ECPerms.Registry.back, 0))
                .executes(new BackCommand());

            LiteralCommandNode<CommandSourceStack> backNode = backBuilder.build();

            rootNode.addChild(backNode);
            essentialCommandsRootNode.addChild(backNode);
        }

        //Warp
        if (CONFIG.ENABLE_WARP) {
            LiteralArgumentBuilder<CommandSourceStack> warpBuilder = Commands.literal("warp");
            LiteralArgumentBuilder<CommandSourceStack> warpSetBuilder = Commands.literal("set");
            LiteralArgumentBuilder<CommandSourceStack> warpTpBuilder = Commands.literal("tp");
            LiteralArgumentBuilder<CommandSourceStack> warpTpOtherBuilder = Commands.literal("tp_other");
            LiteralArgumentBuilder<CommandSourceStack> warpDeleteBuilder = Commands.literal("delete");
            LiteralArgumentBuilder<CommandSourceStack> warpListBuilder = Commands.literal("list");

            warpBuilder
                .requires(ECPerms.require(ECPerms.Registry.warp_tp, 0))
                .then(argument("warp_name", StringArgumentType.word())
                    .suggests(WarpSuggestion.STRING_SUGGESTIONS_PROVIDER)
                    .executes(new WarpTpCommand()));

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
                .then(argument("target_player", EntityArgument.player())
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
                    (context) -> ManagerLocator.getInstance().getWorldDataManager().getAccessibleWarps(context.getSource().getPlayerOrException()).toList(),
                    NamedMinecraftLocation::getName
                ));

            LiteralCommandNode<CommandSourceStack> warpNode = warpBuilder
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
            LiteralArgumentBuilder<CommandSourceStack> spawnBuilder = Commands.literal("spawn");
            LiteralArgumentBuilder<CommandSourceStack> spawnSetBuilder = Commands.literal("set");
            LiteralArgumentBuilder<CommandSourceStack> spawnTpBuilder = Commands.literal("tp");

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

            LiteralCommandNode<CommandSourceStack> spawnNode = spawnBuilder.build();
            spawnNode.addChild(spawnSetBuilder.build());
            spawnNode.addChild(spawnTpBuilder.build());

            registerNode.accept(spawnNode);
        }

        if (CONFIG.ENABLE_NICK) {
            LiteralArgumentBuilder<CommandSourceStack> nickBuilder = Commands.literal("nickname");
            LiteralArgumentBuilder<CommandSourceStack> nickSetBuilder = Commands.literal("set");
            LiteralArgumentBuilder<CommandSourceStack> nickClearBuilder = Commands.literal("clear");
            LiteralArgumentBuilder<CommandSourceStack> nickRevealBuilder = Commands.literal("reveal");

            Predicate<CommandSourceStack> permissionSelf = ECPerms.require(ECPerms.Registry.nickname_self, 2);
            Predicate<CommandSourceStack> permissionOther = ECPerms.require(ECPerms.Registry.nickname_others, 2);
            nickSetBuilder.requires(permissionSelf)
                .then(argument("nickname", ComponentArgument.textComponent(commandRegistryAccess))
                    .executes(new NicknameSetCommand())
                ).then(CommandUtil.targetPlayerArgument()
                    .requires(permissionOther)
                    .then(argument("nickname", ComponentArgument.textComponent(commandRegistryAccess))
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

            LiteralCommandNode<CommandSourceStack> nickNode = nickBuilder
                .requires(ECPerms.requireAny(ECPerms.Registry.Group.nickname_group, 2))
                .build();
            nickNode.addChild(nickSetBuilder.build());
            nickNode.addChild(nickClearBuilder.build());
            nickNode.addChild(nickRevealBuilder.build());

            registerNode.accept(nickNode);
        }

        if (CONFIG.ENABLE_RTP) {
            registerNode.accept(Commands.literal("randomteleport")
                .requires(ECPerms.require(ECPerms.Registry.randomteleport, 2))
                .executes(new RandomTeleportCommand())
                .build());

            registerNode.accept(Commands.literal("rtp")
                .requires(ECPerms.require(ECPerms.Registry.randomteleport, 2))
                .executes(new RandomTeleportCommand())
                .build()
            );
        }

        if (CONFIG.ENABLE_FLY) {
            LiteralArgumentBuilder<CommandSourceStack> flyBuilder = Commands.literal("fly");
            LiteralArgumentBuilder<CommandSourceStack> flySpeedBuilder = Commands.literal("speed");

            Predicate<CommandSourceStack> permissionSelf = ECPerms.require(ECPerms.Registry.fly_self, 2);
            Predicate<CommandSourceStack> permissionOther = ECPerms.require(ECPerms.Registry.fly_others, 2);

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
                .then(Commands.literal("reset")
                    .executes(new FlySpeedCommand()::reset))
                .then(argument("fly_speed", IntegerArgumentType.integer(0))
                    .executes(new FlySpeedCommand()))
                .then(CommandUtil.targetPlayerArgument()
                    .requires(permissionOther)
                    .then(Commands.literal("reset")
                        .executes(new FlySpeedCommand()::reset))
                    .then(argument("fly_speed", IntegerArgumentType.integer(0))
                        .executes(new FlySpeedCommand())));

            LiteralCommandNode<CommandSourceStack> flyNode = flyBuilder.build();
            flyNode.addChild(flySpeedBuilder.build());

            registerNode.accept(flyNode);
        }

        if (CONFIG.ENABLE_INVULN) {
            registerNode.accept(
                Commands.literal("invuln")
                    .requires(ECPerms.require(ECPerms.Registry.invuln_self, 2))
                    .executes(new InvulnCommand())
                    .then(CommandUtil.targetPlayerArgument()
                        .requires(ECPerms.require(ECPerms.Registry.invuln_others, 2))
                        .then(argument("invuln_enabled", BoolArgumentType.bool())
                            .executes(new InvulnCommand())))
                    .build());
        }

        if (CONFIG.ENABLE_WORKBENCH) {
            registerNode.accept(Commands.literal("workbench")
                .requires(ECPerms.require(ECPerms.Registry.workbench, 0))
                .executes(new WorkbenchCommand())
                .build());

            registerNode.accept(Commands.literal("stonecutter")
                .requires(ECPerms.require(ECPerms.Registry.stonecutter, 0))
                .executes(new StonecutterCommand())
                .build());

            registerNode.accept(Commands.literal("grindstone")
                .requires(ECPerms.require(ECPerms.Registry.grindstone, 0))
                .executes(new GrindstoneCommand())
                .build());
        }

        if (CONFIG.ENABLE_ANVIL) {
            registerNode.accept(Commands.literal("anvil")
                .requires(ECPerms.require(ECPerms.Registry.anvil, 0))
                .executes(new AnvilCommand())
                .build());
        }

        if (CONFIG.ENABLE_ENDERCHEST) {
            registerNode.accept(Commands.literal("enderchest")
                    .requires(ECPerms.require(ECPerms.Registry.enderchest, 0))
                    .executes(new EnderchestCommand())
                .build());
        }

        if (CONFIG.ENABLE_WASTEBIN) {
            registerNode.accept(Commands.literal("wastebin")
                .requires(ECPerms.require(ECPerms.Registry.wastebin, 0))
                .executes(new WastebinCommand())
                .build());
        }

        if (CONFIG.ENABLE_TOP) {
            registerNode.accept(Commands.literal("top")
                .requires(ECPerms.require(ECPerms.Registry.top, 2))
                .executes(new TopCommand())
                .build());
        }

        if (CONFIG.ENABLE_GAMETIME) {
            registerNode.accept(Commands.literal("gametime")
                .requires(ECPerms.require(ECPerms.Registry.gametime, 0))
                .executes(new GametimeCommand())
                .build());
        }

        if (CONFIG.ENABLE_AFK) {
            registerNode.accept(Commands.literal("afk")
                .requires(ECPerms.require(ECPerms.Registry.afk, 0))
                .executes(new AfkCommand())
                .build());
        }

        if (CONFIG.ENABLE_BED) {
            registerNode.accept(Commands.literal("bed")
                .requires(ECPerms.require(ECPerms.Registry.bed, 0))
                .executes(new BedCommand())
                .build());
        }

        if (CONFIG.ENABLE_SLEEP) {
            registerNode.accept(Commands.literal("sleep")
                .requires(ECPerms.require(ECPerms.Registry.sleep, 0))
                .executes(new SleepCommand())
                .build());
        }

        registerNode.accept(Commands.literal("lastPos")
            .requires(ECPerms.require("essentialcommands.admin.lastpos", 2))
                .then(argument("target_player", StringArgumentType.word())
                .executes((context) -> {
                    var targetPlayerName = StringArgumentType.getString(context, "target_player");
                    ManagerLocator.getInstance()
                        .getOfflinePlayerRepo()
                        .getOfflinePlayerByNameAsync(targetPlayerName)
                        .whenComplete((playerEntity, err) -> {
                            if (playerEntity == null) {
                                context.getSource().sendFailure(Component.nullToEmpty("No player with the specified name found."));
                                return;
                            }
                            context.getSource().sendSuccess(() ->
                                Component.nullToEmpty(playerEntity.position().toString()),
                                EssentialCommands.CONFIG.BROADCAST_TO_OPS);
                        });
                    return 1;
                }))
            .build());

        if (CONFIG.ENABLE_DAY) {
            registerNode.accept(Commands.literal("day")
                .requires(ECPerms.require(ECPerms.Registry.time_set_day, 2))
                .executes(new DayCommand())
                .build());
        }

        if (CONFIG.ENABLE_RULES) {
            registerNode.accept(Commands.literal("rules")
                .requires(ECPerms.require(ECPerms.Registry.rules, 0))
                .executes(RulesCommand::run)
                .then(literal("reload")
                    .requires(ECPerms.require(ECPerms.Registry.rules_reload, 4))
                    .executes(RulesCommand::reloadCommand))
                .build());
        }

        if (CONFIG.ENABLE_FEED) {
            registerNode.accept(Commands.literal("feed")
                .requires(ECPerms.require(ECPerms.Registry.feed_self, 2))
                .executes(new FeedCommand())
                .then(CommandUtil.targetPlayerArgument()
                    .requires(ECPerms.require(ECPerms.Registry.feed_others, 2))
                    .executes(new FeedCommand()))
                    .build());
        }

        if (CONFIG.ENABLE_HEAL) {
            registerNode.accept(Commands.literal("heal")
                .requires(ECPerms.require(ECPerms.Registry.heal_self, 2))
                .executes(new HealCommand())
                .then(CommandUtil.targetPlayerArgument()
                    .requires(ECPerms.require(ECPerms.Registry.heal_others, 2))
                    .executes(new HealCommand()))
                    .build());
        }

        if (CONFIG.ENABLE_EXTINGUISH) {
            LiteralCommandNode<CommandSourceStack> node = Commands.literal("extinguish")
                .requires(ECPerms.require(ECPerms.Registry.extinguish_self, 2))
                .executes(new ExtinguishCommand())
                .then(CommandUtil.targetPlayerArgument()
                    .requires(ECPerms.require(ECPerms.Registry.extinguish_others, 2))
                    .executes(new ExtinguishCommand()))
                    .build();

            registerNode.accept(node);
            registerNode.accept(Commands.literal("ext").redirect(node).build());
        }

        if (CONFIG.ENABLE_SUICIDE) {
            registerNode.accept(Commands.literal("suicide")
                .requires(ECPerms.require(ECPerms.Registry.suicide, 0))
                .executes(new SuicideCommand())
                .build());
        }

        if (CONFIG.ENABLE_NIGHT) {
            registerNode.accept(Commands.literal("night")
                .requires(ECPerms.require(ECPerms.Registry.time_set_night, 2))
                .executes(new NightCommand())
                .build());
        }

        if (CONFIG.ENABLE_REPAIR) {
            registerNode.accept(Commands.literal("repair")
                .requires(ECPerms.require(ECPerms.Registry.repair_self, 2))
                .executes(new RepairCommand())
                .then(CommandUtil.targetPlayerArgument()
                    .requires(ECPerms.require(ECPerms.Registry.repair_others, 2))
                    .executes(new RepairCommand()))
                    .build());
        }

        if (CONFIG.ENABLE_NEAR) {
            registerNode.accept(Commands.literal("near")
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
            registerNode.accept(Commands.literal("motd")
                .requires(ECPerms.require(ECPerms.Registry.motd, 0))
                .executes(MotdCommand::run)
                .build());
        }

        var profileNode = ProfileCommand.buildNode();
        essentialCommandsRootNode.addChild(profileNode);

        LiteralCommandNode<CommandSourceStack> configNode = Commands.literal("config")
            .requires(ECPerms.requireAny(ECPerms.Registry.Group.config_group, 4))
            .then(Commands.literal("reload")
                .executes((context) -> {
                    BACKING_CONFIG.loadOrCreateProperties();
                    var player = context.getSource().getPlayer();
                    var ecText = player != null ? ECText.access(player) : ECText.getInstance();
                    context.getSource().sendSuccess(() ->
                        ecText.getText("cmd.config.reload"),
                        true
                    );
                    return 1;
                }).requires(
                    ECPerms.require(ECPerms.Registry.config_reload, 4)
                ).build())
            .then(Commands.literal("display")
                .requires(ECPerms.require(ECPerms.Registry.config_reload, 4))
                .executes((context) -> {
                    BACKING_CONFIG.loadOrCreateProperties();
                    context.getSource().sendSuccess(
                        BACKING_CONFIG::stateAsText,
                        false
                    );
                    return 1;
                })
                .then(Commands.argument("config_property", StringArgumentType.word())
                    .suggests(ListSuggestion.of(BACKING_CONFIG::getPublicFieldNames))
                    .executes(context -> {
                        try {
                            Component t = BACKING_CONFIG.getFieldValueAsText(
                                StringArgumentType.getString(context, "config_property"));
                            context.getSource().sendSuccess(() -> t, false);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }

                        return 1;
                    })
                )
            ).build();

        essentialCommandsRootNode.addChild(configNode);

        if (true) {
            essentialCommandsRootNode.addChild(Commands.literal("deleteAllPlayerData")
                .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.OWNERS)))
                .executes(new ClearPlayerDataCommand())
                .build()
            );
        }

        if (CONFIG.ENABLE_ESSENTIALSX_CONVERT) {
            essentialCommandsRootNode.addChild(Commands.literal("convertEssentialsXPlayerHomes")
                .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.OWNERS)))
                .executes((source) -> {
                    Path mcDir = source.getSource().getServer().getServerDirectory();
                    try {
                        EssentialsXParser.convertPlayerDataDir(
                            mcDir.resolve("plugins/Essentials/userdata").toFile(),
                            mcDir.resolve("world/modplayerdata").toFile(),
                            source.getSource().getServer()
                        );
                        source.getSource().sendSuccess(() -> Component.literal("Successfully converted data dirs."), CONFIG.BROADCAST_TO_OPS);
                    } catch (NotDirectoryException | FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }).build()
            );
            essentialCommandsRootNode.addChild(Commands.literal("convertEssentialsXWarps")
                .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.OWNERS)))
                .executes((source) -> {
                    Path mcDir = source.getSource().getServer().getServerDirectory();
                    EssentialsConvertor.warpConvert(
                        source.getSource().getServer(),
                        mcDir.resolve("plugins/Essentials/warps").toFile()
                    );
                    source.getSource().sendSuccess(() -> Component.literal("Successfully converted warps."), CONFIG.BROADCAST_TO_OPS);
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
