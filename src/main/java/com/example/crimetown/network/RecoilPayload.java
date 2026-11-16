package com.example.crimetown.network;

import com.example.crimetown.CrimeTown;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server -> client: kick the camera after a shot. */
public record RecoilPayload(float pitch, float yaw) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RecoilPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CrimeTown.MOD_ID, "recoil"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RecoilPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, RecoilPayload::pitch,
            ByteBufCodecs.FLOAT, RecoilPayload::yaw,
            RecoilPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
