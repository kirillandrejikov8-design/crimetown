package com.example.crimetown.weapon;

import com.example.crimetown.network.RecoilPayload;
import com.example.crimetown.npc.GuardService;
import com.example.crimetown.registry.ModComponents;
import com.example.crimetown.registry.ModSounds;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Hitscan firearm. Everything (ammo, hit detection, damage) is decided on the server;
 * the client only receives a recoil impulse packet.
 *
 * Right click = shoot, Shift + right click = reload.
 */
public class GunItem extends Item {
    private final GunType type;

    public GunItem(Properties props, GunType type) {
        super(props);
        this.type = type;
    }

    public GunType gunType() {
        return type;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel sl) || !(player instanceof ServerPlayer sp)) {
            return InteractionResult.CONSUME;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResult.FAIL;

        if (player.isShiftKeyDown()) {
            reload(sl, sp, stack);
        } else {
            fire(sl, sp, stack);
        }
        return InteractionResult.CONSUME;
    }

    private void reload(ServerLevel sl, ServerPlayer sp, ItemStack stack) {
        int cur = stack.getOrDefault(ModComponents.AMMO, 0);
        int need = type.magazine - cur;
        if (need <= 0) {
            sp.displayClientMessage(Component.literal("Магазин полон"), true);
            return;
        }
        int taken = takeAmmo(sp, need);
        if (taken <= 0) {
            sp.displayClientMessage(Component.literal("Нет патронов в инвентаре"), true);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), ModSounds.GUN_EMPTY, SoundSource.PLAYERS, 1f, 1f);
            return;
        }
        stack.set(ModComponents.AMMO, cur + taken);
        sp.getCooldowns().addCooldown(stack, type.reloadTicks);
        sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), ModSounds.GUN_RELOAD, SoundSource.PLAYERS, 1f, 1f);
        sp.displayClientMessage(Component.literal(type.label + ": " + (cur + taken) + "/" + type.magazine), true);
    }

    private int takeAmmo(ServerPlayer sp, int max) {
        int taken = 0;
        Inventory inv = sp.getInventory();
        for (int i = 0; i < inv.getContainerSize() && taken < max; i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(type.ammoItem())) {
                int n = Math.min(max - taken, s.getCount());
                s.shrink(n);
                taken += n;
            }
        }
        return taken;
    }

    private void fire(ServerLevel sl, ServerPlayer sp, ItemStack stack) {
        int ammo = stack.getOrDefault(ModComponents.AMMO, 0);
        if (ammo <= 0) {
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), ModSounds.GUN_EMPTY, SoundSource.PLAYERS, 1f, 1f);
            sp.displayClientMessage(Component.literal("Пусто! Shift + ПКМ - перезарядка"), true);
            sp.getCooldowns().addCooldown(stack, 10);
            return;
        }
        stack.set(ModComponents.AMMO, ammo - 1);
        sp.getCooldowns().addCooldown(stack, type.cooldown);

        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        Vec3 start = sp.getEyePosition();
        Vec3 look = sp.getLookAngle();
        for (int i = 0; i < type.pellets; i++) {
            Vec3 dir = look.add(
                    (rnd.nextDouble() - 0.5) * 2 * type.spread,
                    (rnd.nextDouble() - 0.5) * 2 * type.spread,
                    (rnd.nextDouble() - 0.5) * 2 * type.spread).normalize();
            shootRay(sl, sp, start, dir);
        }

        sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), type.sound(), SoundSource.PLAYERS, 2.0f, 1f);
        sl.sendParticles(ParticleTypes.SMOKE, start.x + look.x, start.y + look.y - 0.1, start.z + look.z, 3, 0.03, 0.03, 0.03, 0.01);
        ServerPlayNetworking.send(sp, new RecoilPayload(-type.recoil, (rnd.nextFloat() - 0.5f) * type.recoil));
        sp.displayClientMessage(Component.literal(type.label + ": " + (ammo - 1) + "/" + type.magazine), true);

        GuardService.alert(sp); // drawing/firing a weapon in town angers the bouncers
    }

    private void shootRay(ServerLevel sl, ServerPlayer sp, Vec3 start, Vec3 dir) {
        Vec3 end = start.add(dir.scale(type.range));
        BlockHitResult bh = sl.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, sp));
        double maxDist = bh.getType() == HitResult.Type.MISS ? type.range : bh.getLocation().distanceTo(start);
        Vec3 stop = start.add(dir.scale(maxDist));

        AABB box = sp.getBoundingBox().expandTowards(dir.scale(maxDist)).inflate(1.0);
        Entity best = null;
        double bestDist = maxDist;
        Vec3 bestPos = null;
        for (Entity e : sl.getEntities(sp, box, ent -> ent instanceof LivingEntity && ent.isAlive() && !ent.isSpectator())) {
            Optional<Vec3> hit = e.getBoundingBox().inflate(0.1).clip(start, stop);
            if (hit.isPresent()) {
                double d = start.distanceTo(hit.get());
                if (d < bestDist) {
                    bestDist = d;
                    best = e;
                    bestPos = hit.get();
                }
            }
        }

        if (best != null) {
            best.hurtServer(sl, sl.damageSources().playerAttack(sp), type.damage);
            sl.sendParticles(ParticleTypes.CRIT, bestPos.x, bestPos.y, bestPos.z, 4, 0.1, 0.1, 0.1, 0.1);
        } else if (bh.getType() != HitResult.Type.MISS) {
            Vec3 p = bh.getLocation();
            sl.sendParticles(ParticleTypes.POOF, p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.01);
        }
    }
}
