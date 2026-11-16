package com.example.crimetown.client.screen;

import com.example.crimetown.network.ActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Base class for town screens. They are built purely from widgets (buttons used as labels),
 * so there is no custom rendering code to break between versions.
 */
public abstract class CtScreen extends Screen {
    protected List<Integer> v;

    protected CtScreen(String title, List<Integer> values) {
        super(Component.literal(title));
        this.v = values;
    }

    /** Server sent fresh numbers: rebuild the widgets. */
    public void update(List<Integer> values) {
        this.v = values;
        this.rebuildWidgets();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected void label(Component text, int y) {
        Button b = Button.builder(text, x -> {
        }).bounds(this.width / 2 - 130, y, 260, 20).build();
        b.active = false;
        this.addRenderableWidget(b);
    }

    protected Button button(Component text, int x, int y, int w, Runnable onPress) {
        Button b = Button.builder(text, bt -> onPress.run()).bounds(x, y, w, 20).build();
        this.addRenderableWidget(b);
        return b;
    }

    protected void send(int screen, int action, int arg) {
        ClientPlayNetworking.send(new ActionPayload(screen, action, arg));
    }

    protected int top(int rows) {
        return Math.max(10, this.height / 2 - rows * 12);
    }
}
