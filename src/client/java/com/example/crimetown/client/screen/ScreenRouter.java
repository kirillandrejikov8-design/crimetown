package com.example.crimetown.client.screen;

import com.example.crimetown.network.ScreenIds;
import com.example.crimetown.network.ScreenPayload;
import net.minecraft.client.Minecraft;

/** Opens the right screen, or refreshes it if it is already open. */
public final class ScreenRouter {
    private ScreenRouter() {}

    public static void handle(ScreenPayload p) {
        Minecraft mc = Minecraft.getInstance();
        CtScreen next = switch (p.screen()) {
            case ScreenIds.BANK -> mc.screen instanceof BankScreen ? null : new BankScreen(p.values());
            case ScreenIds.SLOT -> mc.screen instanceof SlotScreen ? null : new SlotScreen(p.values());
            case ScreenIds.MAYOR -> mc.screen instanceof MayorScreen ? null : new MayorScreen(p.values());
            case ScreenIds.SHOP -> mc.screen instanceof ShopScreen ? null : new ShopScreen(p.values());
            default -> null;
        };
        if (next != null) {
            mc.setScreen(next);
        } else if (mc.screen instanceof CtScreen open) {
            open.update(p.values());
        }
    }
}
