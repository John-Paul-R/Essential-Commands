package com.fibermc.essentialcommands.types;

import com.fibermc.essentialcommands.playerdata.PlayerProfile;
import com.fibermc.essentialcommands.text.TextFormatType;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import dev.jpcode.eccore.util.TextUtil;

public class NamedMinecraftLocation extends MinecraftLocation {
    private String name;

    protected NamedMinecraftLocation() {}

    public NamedMinecraftLocation(MinecraftLocation location, String name) {
        super(
            location.dim(),
            location.pos().x,
            location.pos().y,
            location.pos().z,
            location.headYaw(),
            location.pitch()
        );

        this.name = name;
    }

    public NamedMinecraftLocation(
        RegistryKey<World> dim,
        double x,
        double y,
        double z,
        float headYaw,
        float pitch,
        String name
    ) {
        super(dim, x, y, z, headYaw, pitch);
        this.name = name;
    }

    protected void loadNbt(NbtCompound tag, String name) {
        super.loadNbt(tag);
        this.name = name;
    }

    public static NamedMinecraftLocation fromNbt(NbtCompound tag, String name) {
        var loc = new NamedMinecraftLocation();
        loc.loadNbt(tag, name);
        return loc;
    }

    public String getName() {
        return name;
    }

    @Override
    public Text toText(PlayerProfile playerProfile) {
        return TextUtil.join(
            new Text[]{
                TextUtil.literal(name),
                toLiteralTextSimple(),
            },
            TextUtil.literal(" ")
        ).setStyle(playerProfile.getStyle(TextFormatType.Accent));
    }
}
