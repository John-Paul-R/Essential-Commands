package com.fibermc.essentialcommands;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Optional;

import com.fibermc.essentialcommands.types.ProfileOption;
import org.jetbrains.annotations.NotNull;

import com.mojang.brigadier.arguments.BoolArgumentType;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.world.PersistentState;

import dev.jpcode.eccore.config.ConfigUtil;

public class PlayerProfile extends PersistentState implements IServerPlayerEntityData {

    private ServerPlayerEntity player;
    private final File saveFile;
    private ProfileOptions profileOptions;

    public PlayerProfile(@NotNull ServerPlayerEntity player, File saveFile) {
        this.player = player;
        this.saveFile = saveFile;
        this.profileOptions = new ProfileOptions();
    }

    private static class ProfileOptions {
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
                (profile) -> profile.profileOptions.printTeleportCoordinates))
    );

    public boolean shouldPrintTeleportCoordinates() {
        return profileOptions.printTeleportCoordinates;
    }

    private static final class StorageKey {
        static final String FORMATTING_DEAULT = "formattingDeault";
        static final String FORMATTING_ACENT = "formattingAcent";
        static final String FORMATTING_ERROR = "formattingError";
        static final String PRINT_TELEPORT_COORDINATES = "printTeleportCoordinates";
    }

    public void fromNbt(NbtCompound tag) {
        NbtCompound dataTag = tag.getCompound("data");
        this.profileOptions = new ProfileOptions();
        this.profileOptions.formattingDefault = Optional.of(dataTag.getString(StorageKey.FORMATTING_DEAULT))
            .map(ConfigUtil::parseStyle)
            .orElse(null);
        this.profileOptions.formattingAccent = Optional.of(dataTag.getString(StorageKey.FORMATTING_ACENT))
            .map(ConfigUtil::parseStyle)
            .orElse(null);
        this.profileOptions.formattingError = Optional.of(dataTag.getString(StorageKey.FORMATTING_ERROR))
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
            tag.putString(StorageKey.FORMATTING_ACENT, ConfigUtil.serializeStyle(this.profileOptions.formattingAccent));
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

}
