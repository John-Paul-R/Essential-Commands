package com.fibermc.essentialcommands;

import java.io.File;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.world.PersistentState;

import dev.jpcode.eccore.config.ConfigUtil;

public class PlayerProfile extends PersistentState {

    private final ServerPlayerEntity player;
    private final File saveFile;
    private ProfileOptions profileOptions;

    public PlayerProfile(@NotNull ServerPlayerEntity player, File saveFile) {

        this.player = player;
        this.saveFile = saveFile;
    }

    private static class ProfileOptions {
        private Style formattingDefault;
        private Style formattingAccent;
        private Style formattingError;
    }

    // Persistance

    private static final class StorageKey {
        static final String FORMATTING_DEAULT = "formattingDeault";
        static final String FORMATTING_ACENT = "formattingAcent";
        static final String FORMATTING_ERROR = "formattingError";
    }

    public void fromNbt(NbtCompound tag) {
        NbtCompound dataTag = tag.getCompound("data");
        this.profileOptions = new ProfileOptions();
        this.profileOptions.formattingDefault = ConfigUtil.parseStyleOrDefault(dataTag.getString(StorageKey.FORMATTING_DEAULT), null);
        this.profileOptions.formattingAccent = ConfigUtil.parseStyleOrDefault(dataTag.getString(StorageKey.FORMATTING_ACENT), null);
        this.profileOptions.formattingError = ConfigUtil.parseStyleOrDefault(dataTag.getString(StorageKey.FORMATTING_ERROR), null);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putString(StorageKey.FORMATTING_DEAULT, ConfigUtil.serializeStyle(this.profileOptions.formattingDefault));
        tag.putString(StorageKey.FORMATTING_ACENT, ConfigUtil.serializeStyle(this.profileOptions.formattingAccent));
        tag.putString(StorageKey.FORMATTING_ERROR, ConfigUtil.serializeStyle(this.profileOptions.formattingError));
        return tag;
    }

    public void save() {
        super.save(saveFile);
    }
}
