package com.example.crimetown;

import com.example.crimetown.concert.ConcertManager;
import com.example.crimetown.drug.DrugService;
import com.example.crimetown.economy.EconomyService;
import com.example.crimetown.economy.TownData;
import com.example.crimetown.network.ActionHandler;
import com.example.crimetown.npc.GuardService;
import com.example.crimetown.npc.Npcs;
import com.example.crimetown.npc.SpawnService;
import com.example.crimetown.progression.ProgressionService;
import com.example.crimetown.shop.Shop;
import com.example.crimetown.world.TownBuilder;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Set;

/** All event wiring on the server side. */
public final class TownEvents {
    private TownEvents() {}

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(TownData::load);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> TownData.unload());

        ServerTickEvents.END_SERVER_TICK.register(TownEvents::tick);

        // First player to join a world without a town gets one built next to the spawn.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            TownData d = TownData.get();
            ServerPlayer p = handler.getPlayer();
            if (d == null) return;
            if (!d.built && p.level() instanceof ServerLevel lv) {
                buildNear(lv, p);
            }
            int pending = ProgressionService.pendingFor(p);
            if (pending > 0) {
                p.sendSystemMessage(Component.literal("[Мэрия] Вас ждёт доход: " + pending + " незерита (заберите у Мэра)."));
            }
        });

        // Talking to NPCs
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (hand != InteractionHand.MAIN_HAND || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            TownData d = TownData.get();
            if (d == null || !d.built) return InteractionResult.PASS;
            Set<String> tags = entity.getTags();
            if (tags.contains(Npcs.CASHIER)) {
                ActionHandler.openBank(sp);
                return InteractionResult.SUCCESS;
            }
            if (tags.contains(Npcs.GUNSMITH)) {
                ActionHandler.openShop(sp, Shop.ARMORY);
                return InteractionResult.SUCCESS;
            }
            if (tags.contains(Npcs.DEALER)) {
                ActionHandler.openShop(sp, Shop.DEALER);
                return InteractionResult.SUCCESS;
            }
            if (tags.contains(Npcs.MAYOR)) {
                ActionHandler.openMayor(sp);
                return InteractionResult.SUCCESS;
            }
            if (tags.contains(Npcs.THUG) || tags.contains(Npcs.CITIZEN) || tags.contains(Npcs.SINGER)) {
                return InteractionResult.SUCCESS; // swallow the vanilla trade GUI
            }
            return InteractionResult.PASS;
        });

        // Slot machines
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (hand != InteractionHand.MAIN_HAND || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            TownData d = TownData.get();
            if (d == null || !d.built) return InteractionResult.PASS;
            var pos = hit.getBlockPos();
            if (d.isSlot(pos.getX(), pos.getY(), pos.getZ())) {
                ActionHandler.openSlot(sp);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        // Hitting a resident angers the bouncers
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (player instanceof ServerPlayer sp) {
                for (String t : entity.getTags()) {
                    if (t.startsWith("ct_")) {
                        GuardService.alert(sp);
                        break;
                    }
                }
            }
            return InteractionResult.PASS;
        });

        // Thugs drop what they stole
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            int loot = GuardService.lootOf(entity);
            if (loot > 0 && entity.level() instanceof ServerLevel lv) {
                lv.addFreshEntity(new ItemEntity(lv, entity.getX(), entity.getY(), entity.getZ(),
                        new ItemStack(Items.GOLD_INGOT, loot)));
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
                dispatcher.register(Commands.literal("crimetown")
                        .then(Commands.literal("build")
                                .requires(src -> src.getEntity() instanceof ServerPlayer sp && sp.isCreative())
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayer();
                                    if (p == null || !(p.level() instanceof ServerLevel lv)) return 0;
                                    buildNear(lv, p);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("info")
                                .executes(ctx -> {
                                    TownData d = TownData.get();
                                    String msg = d == null || !d.built
                                            ? "Город ещё не построен."
                                            : "Город: x=" + d.cx + " y=" + d.cy + " z=" + d.cz
                                            + " | Алмаз=" + EconomyService.fmt(d.diamondValue)
                                            + " Незерит=" + EconomyService.fmt(d.netheriteValue);
                                    ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
                                    return Command.SINGLE_SUCCESS;
                                }))));
    }

    private static void buildNear(ServerLevel lv, ServerPlayer p) {
        int ox = p.blockPosition().getX() + 20;
        int oz = p.blockPosition().getZ() - TownBuilder.SIZE / 2;
        TownBuilder.build(lv, ox, oz);
        TownData d = TownData.get();
        p.sendSystemMessage(Component.literal("[CrimeTown] Город построен: x=" + d.cx + " y=" + d.cy + " z=" + d.cz
                + ". Загляните к Кассиру, в Казино и в Мэрию."));
    }

    private static void tick(MinecraftServer server) {
        TownData d = TownData.get();
        if (d == null) return;
        EconomyService.dailyTick(server, d);
        DrugService.tick(server);
        if (!d.built) return;
        ConcertManager.tick(server);
        long t = server.getTickCount();
        if (t % 10 == 0) GuardService.thugTick(server);
        if (t % 40 == 0) ProgressionService.bodyguardTick(server);
        if (t % 400 == 0) SpawnService.tick(server);
    }
}
