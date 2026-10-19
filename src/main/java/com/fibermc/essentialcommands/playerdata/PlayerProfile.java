package com.fibermc.essentialcommands.playerdata;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.commands.CommandUtil;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.types.AllowTeleportRequests;
import com.fibermc.essentialcommands.types.IStyleProvider;
import com.fibermc.essentialcommands.types.ProfileOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import dev.jpcode.eccore.config.ConfigUtil;

public class PlayerProfile extends SavedData implements IServerPlayerEntityData, IStyleProvider {

    private ServerPlayer player;
    private final File saveFile;
    private ProfileOptions profileOptions;

    public PlayerProfile(@NotNull ServerPlayer player, File saveFile) {
        this.player = player;
        this.saveFile = saveFile;
        this.profileOptions = new ProfileOptions();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final class ProfileOptions {
        private Optional<Style> formattingDefault = Optional.empty();
        private Optional<Style> formattingAccent = Optional.empty();
        private Optional<Style> formattingError = Optional.empty();
        private Optional<Boolean> printTeleportCoordinates = Optional.empty();
        private Optional<AllowTeleportRequests> allowTeleportRequests = Optional.empty();
    }

    public static final Map<String, ProfileOption<?>> OPTIONS = Map.ofEntries(
        new SimpleEntry<>(
            StorageKey.PRINT_TELEPORT_COORDINATES,
            new ProfileOption<>(
                BoolArgumentType.bool(),
                false,
                (context, name, profile) -> profile.profileOptions.printTeleportCoordinates = Optional.of(BoolArgumentType.getBool(context, name)),
                (profile) -> profile.profileOptions.printTeleportCoordinates)),
        new SimpleEntry<>(
            StorageKey.ALLOW_TELEPORT_REQUESTS,
            new ProfileOption<>(
                StringArgumentType.word(),
                AllowTeleportRequests.ON.getId(),
                (context, name, profile) -> {
                    var id = StringArgumentType.getString(context, name);
                    var value = AllowTeleportRequests.fromId(id);
                    if (value.isEmpty()) {
                        throw CommandUtil.createSimpleException(
                            ECText.access(profile.getPlayer()).getText("cmd.profile.error.invalid_value", Component.literal(id)));
                    }
                    profile.profileOptions.allowTeleportRequests = value;
                },
                (profile) -> profile.profileOptions.allowTeleportRequests.map(AllowTeleportRequests::getId),
                (context, builder) -> SharedSuggestionProvider.suggest(
                    Arrays.stream(AllowTeleportRequests.values()).map(AllowTeleportRequests::getId),
                    builder))),
        new SimpleEntry<>(
            StorageKey.FORMATTING_DEAULT,
            new ProfileOption<>(
                StringArgumentType.greedyString(),
                null,
                (context, name, profile) -> profile.profileOptions.formattingDefault = Optional.ofNullable(ConfigUtil.parseStyle(StringArgumentType.getString(context, name))),
                (profile) -> profile.profileOptions.formattingDefault.map(ConfigUtil::serializeStyle))),
        new SimpleEntry<>(
            StorageKey.FORMATTING_ACCENT,
            new ProfileOption<>(
                StringArgumentType.greedyString(),
                null,
                (context, name, profile) -> profile.profileOptions.formattingAccent = Optional.ofNullable(ConfigUtil.parseStyle(StringArgumentType.getString(context, name))),
                (profile) -> profile.profileOptions.formattingAccent.map(ConfigUtil::serializeStyle)))
//        new SimpleEntry<>(
//            StorageKey.FORMATTING_ERROR,
//            new ProfileOption<>(
//                StringArgumentType.greedyString(),
//                null,
//                (context, name, profile) -> profile.profileOptions.formattingError = ConfigUtil.parseStyle(StringArgumentType.getString(context, name)),
//                (profile) -> ConfigUtil.serializeStyle(profile.profileOptions.formattingError)))
    );

    public Optional<Boolean> shouldPrintTeleportCoordinates() {
        return profileOptions.printTeleportCoordinates;
    }

    public AllowTeleportRequests getAllowTeleportRequests() {
        return profileOptions.allowTeleportRequests.orElse(AllowTeleportRequests.ON);
    }

    public void setAllowTeleportRequests(AllowTeleportRequests allowTeleportRequests) {
        profileOptions.allowTeleportRequests = Optional.of(allowTeleportRequests);
    }

    public @Nullable Style getFormattingDefault() {
        return profileOptions.formattingDefault.orElse(null);
    }

    public @Nullable Style getFormattingAccent() {
        return profileOptions.formattingAccent.orElse(null);
    }

    public @Nullable Style getFormattingError() {
        return profileOptions.formattingError.orElse(null);
    }

    private static final class StorageKey {
        static final String FORMATTING_DEAULT = "formattingDeault";
        static final String FORMATTING_ACCENT = "formattingAccent";
        static final String FORMATTING_ERROR = "formattingError";
        static final String PRINT_TELEPORT_COORDINATES = "printTeleportCoordinates";
        static final String ALLOW_TELEPORT_REQUESTS = "allowTeleportRequests";
    }

    public void fromNbt(CompoundTag tag) {
        // `data` was the main obj key in old mc PersistentState schema
        CompoundTag dataTag = tag.getCompound("data").orElse(tag);
        this.profileOptions = new ProfileOptions();

        this.profileOptions.formattingDefault = dataTag.getString(StorageKey.FORMATTING_DEAULT)
            .flatMap(s -> Optional.ofNullable(ConfigUtil.parseStyle(s)));

        this.profileOptions.formattingAccent = dataTag.getString(StorageKey.FORMATTING_ACCENT)
            .flatMap(s -> Optional.ofNullable(ConfigUtil.parseStyle(s)));

        this.profileOptions.formattingError = dataTag.getString(StorageKey.FORMATTING_ERROR)
            .flatMap(s -> Optional.ofNullable(ConfigUtil.parseStyle(s)));

        this.profileOptions.printTeleportCoordinates = dataTag.getBoolean(StorageKey.PRINT_TELEPORT_COORDINATES);

        this.profileOptions.allowTeleportRequests = dataTag.getString(StorageKey.ALLOW_TELEPORT_REQUESTS)
            .flatMap(AllowTeleportRequests::fromId);
    }

    public CompoundTag writeNbt(CompoundTag tag, HolderLookup.Provider wrapperLookup) {
        this.profileOptions.formattingDefault
            .ifPresent(style -> tag.putString(StorageKey.FORMATTING_DEAULT, ConfigUtil.serializeStyle(style)));

        this.profileOptions.formattingAccent
            .ifPresent(style -> tag.putString(StorageKey.FORMATTING_ACCENT, ConfigUtil.serializeStyle(style)));

        this.profileOptions.formattingError
            .ifPresent(style -> tag.putString(StorageKey.FORMATTING_ERROR, ConfigUtil.serializeStyle(style)));

        this.profileOptions.printTeleportCoordinates.ifPresent(
            printTeleportCoordinates -> tag.putBoolean(StorageKey.PRINT_TELEPORT_COORDINATES, printTeleportCoordinates));

        this.profileOptions.allowTeleportRequests.ifPresent(
            allowTeleportRequests -> tag.putString(StorageKey.ALLOW_TELEPORT_REQUESTS, allowTeleportRequests.getId()));

        return tag;
    }

    public void save(HolderLookup.Provider wrapperLookup) {
        CompoundTag data = this.writeNbt(new CompoundTag(), wrapperLookup);

        try {
            NbtIo.writeCompressed(data, this.saveFile.toPath());
        } catch (IOException e) {
            EssentialCommands.LOGGER.error("Could not save data {}", this, e);
        }
    }

    @Override
    public ServerPlayer getPlayer() {
        return player;
    }

    @Override
    public void updatePlayerEntity(ServerPlayer newPlayerEntity) {
        this.player = newPlayerEntity;
    }

    public static PlayerProfile access(@NotNull ServerPlayer player) {
        return ((ServerPlayerEntityAccess) player).ec$getProfile();
    }

    public static PlayerProfile accessFromContextOrThrow(CommandContext<CommandSourceStack> context)
        throws CommandSyntaxException
    {
        return access(context.getSource().getPlayerOrException());
    }
}
