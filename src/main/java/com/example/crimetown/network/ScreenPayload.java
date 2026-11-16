package com.example.crimetown.network;

import com.example.crimetown.CrimeTown;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/** Server -> client: open (or refresh) a town screen with the given numbers. */
public record ScreenPayload(int screen, List<Integer> values) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ScreenPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CrimeTown.MOD_ID, "screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScreenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ScreenPayload::screen,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), ScreenPayload::values,
            ScreenPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
