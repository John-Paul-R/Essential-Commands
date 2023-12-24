package com.fibermc.essentialcommands.playerdata;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Optional;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.types.IStyleProvider;
import com.fibermc.essentialcommands.types.ProfileOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.world.PersistentState;

import dev.jpcode.eccore.config.ConfigUtil;

public class PlayerProfile extends PersistentState implements IServerPlayerEntityData, IStyleProvider {

    private ServerPlayerEntity player;
    private final File saveFile;
    private ProfileOptions profileOptions;

    public PlayerProfile(@NotNull ServerPlayerEntity player, File saveFile) {
        this.player = player;
        this.saveFile = saveFile;
        this.profileOptions = new ProfileOptions();
    }

    private static final class ProfileOptions {
        private Style formattingDefault;
        private Style formattingAccent;
        private Style formattingError;
        private boolean printTeleportCoordinates = true;
    }

    public static final Map<String, ProfileOption<?>> OPTIONS = Map.ofEntries(
        new SimpleEntry<>(
            StorageKey.PRINT_TELEPORT_COORDINATES,
            new ProfileOption<>(
                BoolArgumentType.bool(),
                false,
                (context, name, profile) -> profile.profileOptions.printTeleportCoordinates = BoolArgumentType.getBool(context, name),
                (profile) -> profile.profileOptions.printTeleportCoordinates)),
        new SimpleEntry<>(
            StorageKey.FORMATTING_DEAULT,
            new ProfileOption<>(
                StringArgumentType.greedyString(),
                null,
                (context, name, profile) -> profile.profileOptions.formattingDefault = ConfigUtil.parseStyle(StringArgumentType.getString(context, name)),
                (profile) -> ConfigUtil.serializeStyle(profile.profileOptions.formattingDefault))),
        new SimpleEntry<>(
            StorageKey.FORMATTING_ACCENT,
            new ProfileOption<>(
                StringArgumentType.greedyString(),
                null,
                (context, name, profile) -> profile.profileOptions.formattingAccent = ConfigUtil.parseStyle(StringArgumentType.getString(context, name)),
                (profile) -> ConfigUtil.serializeStyle(profile.profileOptions.formattingAccent)))
//        new SimpleEntry<>(
//            StorageKey.FORMATTING_ERROR,
//            new ProfileOption<>(
//                StringArgumentType.greedyString(),
//                null,
//                (context, name, profile) -> profile.profileOptions.formattingError = ConfigUtil.parseStyle(StringArgumentType.getString(context, name)),
//                (profile) -> ConfigUtil.serializeStyle(profile.profileOptions.formattingError)))
    );

    public boolean shouldPrintTeleportCoordinates() {
        return profileOptions.printTeleportCoordinates;
    }

    public @Nullable Style getFormattingDefault() {
        return profileOptions.formattingDefault;
    }

    public @Nullable Style getFormattingAccent() {
        return profileOptions.formattingAccent;
    }

    public @Nullable Style getFormattingError() {
        return profileOptions.formattingError;
    }

    private static final class StorageKey {
        static final String FORMATTING_DEAULT = "formattingDeault";
        static final String FORMATTING_ACCENT = "formattingAccent";
        static final String FORMATTING_ERROR = "formattingError";
        static final String PRINT_TELEPORT_COORDINATES = "printTeleportCoordinates";
    }

    public void fromNbt(NbtCompound tag) {
        NbtCompound dataTag = tag.getCompound("data");
        this.profileOptions = new ProfileOptions();
        this.profileOptions.formattingDefault = Optional.ofNullable(dataTag.get(StorageKey.FORMATTING_DEAULT))
            .map(NbtElement::asString)
            .map(ConfigUtil::parseStyle)
            .orElse(null);
        this.profileOptions.formattingAccent = Optional.ofNullable(dataTag.get(StorageKey.FORMATTING_ACCENT))
            .map(NbtElement::asString)
            .map(ConfigUtil::parseStyle)
            .orElse(null);
        this.profileOptions.formattingError = Optional.ofNullable(dataTag.get(StorageKey.FORMATTING_ERROR))
            .map(NbtElement::asString)
            .map(ConfigUtil::parseStyle)
            .orElse(null);
        this.profileOptions.printTeleportCoordinates = dataTag.getBoolean(StorageKey.PRINT_TELEPORT_COORDINATES);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        if (this.profileOptions.formattingDefault != null) {
            tag.putString(StorageKey.FORMATTING_DEAULT, ConfigUtil.serializeStyle(this.profileOptions.formattingDefault));
        }
        if (this.profileOptions.formattingAccent != null) {
            tag.putString(StorageKey.FORMATTING_ACCENT, ConfigUtil.serializeStyle(this.profileOptions.formattingAccent));
        }
        if (this.profileOptions.formattingError != null) {
            tag.putString(StorageKey.FORMATTING_ERROR, ConfigUtil.serializeStyle(this.profileOptions.formattingError));
        }
        tag.putBoolean(StorageKey.PRINT_TELEPORT_COORDINATES, this.profileOptions.printTeleportCoordinates);
        return tag;
    }

    public void save() {
        super.save(saveFile);
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return player;
    }

    @Override
    public void updatePlayerEntity(ServerPlayerEntity newPlayerEntity) {
        this.player = newPlayerEntity;
    }

    public static PlayerProfile access(@NotNull ServerPlayerEntity player) {
        return ((ServerPlayerEntityAccess) player).ec$getProfile();
    }

    public static PlayerProfile accessFromContextOrThrow(CommandContext<ServerCommandSource> context)
        throws CommandSyntaxException
    {
        return access(context.getSource().getPlayerOrThrow());
    }
}
