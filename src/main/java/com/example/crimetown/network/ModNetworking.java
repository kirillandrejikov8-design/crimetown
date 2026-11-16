package com.example.crimetown.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/** Registers payload types (common side). Fabric API 26.1 names: serverboundPlay / clientboundPlay. */
public final class ModNetworking {
    private ModNetworking() {}

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(ActionPayload.TYPE, ActionPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ScreenPayload.TYPE, ScreenPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RecoilPayload.TYPE, RecoilPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ActionPayload.TYPE, (payload, context) ->
                ActionHandler.handle(context.player(), payload.screen(), payload.action(), payload.arg()));
    }
}
