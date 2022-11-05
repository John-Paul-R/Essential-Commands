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
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.util.EssentialsConvertor;
import com.fibermc.essentialcommands.util.EssentialsXParser;
import org.spongepowered.asm.util.IConsumer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import static com.fibermc.essentialcommands.EssentialCommands.BACKING_CONFIG;
import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Primary registry class for EssentialCommands.
 * Contains logic for building the brigaider command trees, and registers
 * required permissions for each node.
 */
@SuppressWarnings("CheckStyle")
public final class EssentialCommandRegistry implements CommandRegistrationCallback {
    EssentialCommandRegistry() {}

    @Override
    public void register(
        CommandDispatcher<ServerCommandSource> dispatcher,
        CommandRegistryAccess commandRegistryAccess,
        CommandManager.RegistrationEnvironment registrationEnvironment
    ) {
        RootCommandNode<ServerCommandSource> rootNode = dispatcher.getRoot();

        LiteralCommandNode<ServerCommandSource> essentialCommandsRootNode;
        // Info Command & "/essentialcommands" root node init
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
                if (!excludedTopLevelCommands.contains(node.getLiteral())) {
                    rootNode.addChild(node);
                }
                essentialCommandsRootNode.addChild(node);
            }
            : essentialCommandsRootNode::addChild;

        if (CONFIG.ENABLE_TPA) {
            var tpaBuilder      = CommandManager.literal("tpa");
            var tpcancelBuilder = CommandManager.literal("tpcancel");
            var tpacceptBuilder = CommandManager.literal("tpaccept");
            var tpdenyBuilder   = CommandManager.literal("tpdeny");
            var tpahereBuilder  = CommandManager.literal("tpahere");

            registerNode.accept(tpaBuilder
                .requires(ECPerms.require(ECPerms.Registry.tpa, 0))
                .then(CommandUtil.targetPlayerArgument()
                    .executes(new TeleportAskCommand()))
                .build());

            registerNode.accept(tpcancelBuilder
                .requires(ECPerms.require(ECPerms.Registry.tpa, 0))
                .executes(new TeleportCancelCommand())
                .build());

            registerNode.accept(tpacceptBuilder
                .requires(ECPerms.require(ECPerms.Registry.tpaccept, 0))
                .executes(new TeleportAcceptCommand()::runDefault)
                .then(CommandUtil.targetPlayerArgument()
                    .suggests(TeleportResponseSuggestion.STRING_SUGGESTIONS_PROVIDER)
                    .executes(new TeleportAcceptCommand()))
                .build());

            registerNode.accept(tpdenyBuilder
                .requires(ECPerms.require(ECPerms.Registry.tpdeny, 0))
                .executes(new TeleportDenyCommand()::runDefault)
                .then(CommandUtil.targetPlayerArgument()
                    .suggests(TeleportResponseSuggestion.STRING_SUGGESTIONS_PROVIDER)
                    .executes(new TeleportDenyCommand()))
                .build());

            registerNode.accept(tpahereBuilder
                .requires(ECPerms.require(ECPerms.Registry.tpahere, 0))
                .then(CommandUtil.targetPlayerArgument()
                    .executes(new TeleportAskHereCommand()))
                .build());
        }

        if (CONFIG.ENABLE_HOME) {
           var homeBuilder            = CommandManager.literal("home");
           var homeSetBuilder         = CommandManager.literal("set");
           var homeTpBuilder          = CommandManager.literal("tp");
           var homeTpOtherBuilder     = CommandManager.literal("tp_other");
           var homeTpOfflineBuilder   = CommandManager.literal("tp_offline");
           var homeDeleteBuilder      = CommandManager.literal("delete");
           var homeListBuilder        = CommandManager.literal("list");
           var homeListOfflineBuilder = CommandManager.literal("list_offline");

            homeSetBuilder
                .requires(ECPerms.require(ECPerms.Registry.home_set, 0))
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
        }

        //Back
        if (CONFIG.ENABLE_BACK) {
            var backBuilder = CommandManager.literal("back");

            backBuilder
                .requires(ECPerms.require(ECPerms.Registry.back, 0))
                .executes(new BackCommand());

            LiteralCommandNode<ServerCommandSource> backNode = backBuilder.build();

            rootNode.addChild(backNode);
            essentialCommandsRootNode.addChild(backNode);
        }

        //Warp
        if (CONFIG.ENABLE_WARP) {
            var warpBuilder        = CommandManager.literal("warp");
            var warpSetBuilder     = CommandManager.literal("set");
            var warpTpBuilder      = CommandManager.literal("tp");
            var warpTpOtherBuilder = CommandManager.literal("tp_other");
            var warpDeleteBuilder  = CommandManager.literal("delete");
            var warpListBuilder    = CommandManager.literal("list");

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
                    (context) -> ManagerLocator.getInstance().getWorldDataManager().getWarpEntries()
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
            var spawnBuilder    = CommandManager.literal("spawn");
            var spawnSetBuilder = CommandManager.literal("set");
            var spawnTpBuilder  = CommandManager.literal("tp");

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
            var nickBuilder       = CommandManager.literal("nickname");
            var nickSetBuilder    = CommandManager.literal("set");
            var nickClearBuilder  = CommandManager.literal("clear");
            var nickRevealBuilder = CommandManager.literal("reveal");

            Predicate<ServerCommandSource> permissionSelf = ECPerms.require(ECPerms.Registry.nickname_self, 0);
            Predicate<ServerCommandSource> permissionOther = ECPerms.require(ECPerms.Registry.nickname_others, 3);
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
                .requires(ECPerms.require(ECPerms.Registry.nickname_self, 0))
                .executes(new NicknameClearCommand())
                .then(CommandUtil.targetPlayerArgument()
                    .requires(ECPerms.require(ECPerms.Registry.nickname_others, 3))
                    .executes(new NicknameClearCommand()));

            nickRevealBuilder
                .requires(ECPerms.require(ECPerms.Registry.nickname_reveal, 0))
                .then(argument("player_nickname", StringArgumentType.word())
                    .suggests(NicknamePlayersSuggestion.STRING_SUGGESTIONS_PROVIDER)
                    .executes(new RealNameCommand())
                );

            LiteralCommandNode<ServerCommandSource> nickNode = nickBuilder
                .requires(ECPerms.requireAny(ECPerms.Registry.Group.nickname_group, 0))
                .build();
            nickNode.addChild(nickSetBuilder.build());
            nickNode.addChild(nickClearBuilder.build());
            nickNode.addChild(nickRevealBuilder.build());

            registerNode.accept(nickNode);
        }

        if (CONFIG.ENABLE_RTP) {
            var randomteleportBuilder = CommandManager.literal("randomteleport");
            var rtpBuilder            = CommandManager.literal("rtp");

            // TODO @jp Surely there is a better way to do an alias?
            registerNode.accept(randomteleportBuilder
                .requires(ECPerms.require(ECPerms.Registry.randomteleport, 2))
                .executes(new RandomTeleportCommand())
                .build());

            registerNode.accept(rtpBuilder
                .requires(ECPerms.require(ECPerms.Registry.randomteleport, 2))
                .executes(new RandomTeleportCommand())
                .build()
            );
        }

        if (CONFIG.ENABLE_FLY) {
            var flyBuilder = CommandManager.literal("fly");

            registerNode.accept(flyBuilder
                .requires(ECPerms.require(ECPerms.Registry.fly_self, 2))
                .executes(new FlyCommand())
                .then(CommandUtil.targetPlayerArgument()
                    .requires(ECPerms.require(ECPerms.Registry.fly_others, 2))
                    .then(argument("flight_enabled", BoolArgumentType.bool())
                        .executes(new FlyCommand())))
                .build());
        }

        if (CONFIG.ENABLE_INVULN) {
            var invulnBuilder = CommandManager.literal("invuln");

            registerNode.accept(
                invulnBuilder
                    .requires(ECPerms.require(ECPerms.Registry.invuln_self, 2))
                    .executes(new InvulnCommand())
                    .then(CommandUtil.targetPlayerArgument()
                        .requires(ECPerms.require(ECPerms.Registry.invuln_others, 2))
                        .then(argument("invuln_enabled", BoolArgumentType.bool())
                            .executes(new InvulnCommand())))
                    .build());
        }

        if (CONFIG.ENABLE_WORKBENCH) {
            var workbenchBuilder   = CommandManager.literal("workbench");
            var stonecutterBuilder = CommandManager.literal("stonecutter");
            var grindstoneBuilder  = CommandManager.literal("grindstone");

            // TODO @jp: 1.0.0 - let these be individually toggled in the config
            registerNode.accept(workbenchBuilder
                .requires(ECPerms.require(ECPerms.Registry.workbench, 0))
                .executes(new WorkbenchCommand())
                .build());

            registerNode.accept(stonecutterBuilder
                .requires(ECPerms.require(ECPerms.Registry.stonecutter, 0))
                .executes(new StonecutterCommand())
                .build());

            registerNode.accept(grindstoneBuilder
                .requires(ECPerms.require(ECPerms.Registry.grindstone, 0))
                .executes(new GrindstoneCommand())
                .build());
        }

        if (CONFIG.ENABLE_ANVIL) {
            var anvilBuilder = CommandManager.literal("anvil");

            registerNode.accept(anvilBuilder
                .requires(ECPerms.require(ECPerms.Registry.anvil, 0))
                .executes(new AnvilCommand())
                .build());
        }

        if (CONFIG.ENABLE_ENDERCHEST) {
            var enderchestBuilder = CommandManager.literal("enderchest");

            registerNode.accept(enderchestBuilder
                    .requires(ECPerms.require(ECPerms.Registry.enderchest, 0))
                    .executes(new EnderchestCommand())
                .build());
        }

        if (CONFIG.ENABLE_WASTEBIN) {
            var wastebinBuilder = CommandManager.literal("wastebin");

            registerNode.accept(wastebinBuilder
                .requires(ECPerms.require(ECPerms.Registry.wastebin, 0))
                .executes(new WastebinCommand())
                .build());
        }

        if (CONFIG.ENABLE_TOP) {
            var topBuilder = CommandManager.literal("top");

            registerNode.accept(topBuilder
                .requires(ECPerms.require(ECPerms.Registry.top, 2))
                .executes(new TopCommand())
                .build());
        }

        if (CONFIG.ENABLE_GAMETIME) {
            var gametimeBuilder = CommandManager.literal("gametime");

            registerNode.accept(gametimeBuilder
                .requires(ECPerms.require(ECPerms.Registry.gametime, 0))
                .executes(new GametimeCommand())
                .build());
        }

        if (CONFIG.ENABLE_AFK) {
            var afkBuilder = CommandManager.literal("afk");

            registerNode.accept(afkBuilder
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
                            context.getSource().sendFeedback(
                                Text.of(playerEntity.getPos().toString()),
                                EssentialCommands.CONFIG.BROADCAST_TO_OPS);
                        });
                    return 1;
                }))
            .build());

        if (CONFIG.ENABLE_DAY) {
            var dayBuilder = CommandManager.literal("day");

            registerNode.accept(dayBuilder
                .requires(ECPerms.require(ECPerms.Registry.time_set_day, 2))
                .executes((context) -> {
                    var source = context.getSource();
                    var playerData = PlayerData.accessFromContextOrThrow(context);
                    var world = source.getServer().getOverworld();
                    if (world.isDay()) {
                        playerData.sendCommandFeedback("cmd.day.error.already_daytime");
                        return -1;
                    }
                    var time = world.getTimeOfDay();
                    var timeToDay = 24000L - time % 24000L;

                    world.setTimeOfDay(time + timeToDay);
                    playerData.sendCommandFeedback("cmd.day.feedback");
                    return 1;
                })
                .build());
        }

        if (CONFIG.ENABLE_RULES) {
            var rulesBuilder = CommandManager.literal("rules");

            registerNode.accept(rulesBuilder
                .requires(ECPerms.require(ECPerms.Registry.rules, 0))
                .executes(RulesCommand::run)
                .then(literal("reload")
                    .requires(ECPerms.require(ECPerms.Registry.rules_reload, 4))
                    .executes(RulesCommand::reloadCommand))
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
                    context.getSource().sendFeedback(
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
                        BACKING_CONFIG.stateAsText(),
                        false
                    );
                    return 1;
                })
                .then(CommandManager.argument("config_property", StringArgumentType.word())
                    .suggests(ListSuggestion.of(BACKING_CONFIG::getPublicFieldNames))
                    .executes(context -> {
                        try {
                            context.getSource().sendFeedback(BACKING_CONFIG.getFieldValueAsText(
                                StringArgumentType.getString(context, "config_property")
                            ), false);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }

                        return 1;
                    })
                )
            ).build();

        essentialCommandsRootNode.addChild(configNode);

        if (CONFIG.ENABLE_ESSENTIALSX_CONVERT) {
            var convertEssentialsXPlayerHomesBuilder = CommandManager.literal("convertEssentialsXPlayerHomes");

            essentialCommandsRootNode.addChild(convertEssentialsXPlayerHomesBuilder
                .requires(source -> source.hasPermissionLevel(4))
                .executes((source) -> {
                    Path mcDir = source.getSource().getServer().getRunDirectory().toPath();
                    try {
                        EssentialsXParser.convertPlayerDataDir(
                            mcDir.resolve("plugins/Essentials/userdata").toFile(),
                            mcDir.resolve("world/modplayerdata").toFile(),
                            source.getSource().getServer()
                        );
                        source.getSource().sendFeedback(Text.literal("Successfully converted data dirs."), CONFIG.BROADCAST_TO_OPS);
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
                    source.getSource().sendFeedback(Text.literal("Successfully converted warps."), CONFIG.BROADCAST_TO_OPS);
                    return 0;
                }).build()
            );

        }

        rootNode.addChild(essentialCommandsRootNode);
    }

}
