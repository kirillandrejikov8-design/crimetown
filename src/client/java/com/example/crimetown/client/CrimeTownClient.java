package com.example.crimetown.client;

import com.example.crimetown.client.screen.ScreenRouter;
import com.example.crimetown.network.RecoilPayload;
import com.example.crimetown.network.ScreenPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

/** Client-only entrypoint: screens and packet receivers. Never loaded on a dedicated server. */
public class CrimeTownClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ScreenPayload.TYPE, (payload, context) ->
                Minecraft.getInstance().execute(() -> ScreenRouter.handle(payload)));

        ClientPlayNetworking.registerGlobalReceiver(RecoilPayload.TYPE, (payload, context) ->
                Minecraft.getInstance().execute(() -> {
                    var p = Minecraft.getInstance().player;
                    if (p != null) {
                        p.setXRot(Math.max(-90f, Math.min(90f, p.getXRot() + payload.pitch())));
                        p.setYRot(p.getYRot() + payload.yaw());
                    }
                }));
    }
}
