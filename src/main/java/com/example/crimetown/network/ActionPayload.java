package com.example.crimetown.network;

import com.example.crimetown.CrimeTown;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client -> server: a button was pressed on a town screen. The server re-validates everything. */
public record ActionPayload(int screen, int action, int arg) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ActionPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CrimeTown.MOD_ID, "action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ActionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ActionPayload::screen,
            ByteBufCodecs.VAR_INT, ActionPayload::action,
            ByteBufCodecs.VAR_INT, ActionPayload::arg,
            ActionPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
