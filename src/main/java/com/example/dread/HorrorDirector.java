package com.example.dread;

import com.example.dread.entity.WatcherEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * «Режиссёр» ужаса: ночью иногда пугает игроков звуками за спиной
 * и подселяет Наблюдателя. Когда Наблюдатель близко — бьётся сердце.
 */
public final class HorrorDirector {
    private static final int INTERVAL = 40; // раз в 2 секунды
    private static final Map<UUID, Integer> COOLDOWNS = new HashMap<>();

    private static final SoundEvent[] SCARE_SOUNDS = {
            SoundEvents.ENTITY_ENDERMAN_STARE,
            SoundEvents.ENTITY_GHAST_AMBIENT,
            SoundEvents.ENTITY_PHANTOM_AMBIENT,
            SoundEvents.ENTITY_WARDEN_NEARBY_CLOSE,
            SoundEvents.BLOCK_WOODEN_DOOR_OPEN,
            SoundEvents.BLOCK_IRON_DOOR_CLOSE,
            SoundEvents.ENTITY_ZOMBIE_AMBIENT,
            SoundEvents.ENTITY_SKELETON_AMBIENT
    };

    public static void init() {
        ServerTickEvents.END_WORLD_TICK.register(HorrorDirector::onWorldTick);
    }

    private static void onWorldTick(ServerWorld world) {
        if (world.getRegistryKey() != World.OVERWORLD) return;
        if (world.getTime() % INTERVAL != 0) return;

        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.isSpectator()) continue;

            heartbeatIfWatcherNear(world, player);

            long t = world.getTimeOfDay() % 24000L;
            boolean night = t >= 13000L && t <= 23000L;
            if (!night) continue;

            int cd = COOLDOWNS.getOrDefault(player.getUuid(), 0) - INTERVAL;
            if (cd > 0) {
                COOLDOWNS.put(player.getUuid(), cd);
                continue;
            }

            Random r = world.random;
            if (r.nextInt(8) != 0) continue; // шанс события за каждый «тик» режиссёра

            if (r.nextInt(4) == 0) {
                trySpawnWatcher(world, player);
            } else {
                playScareBehind(world, player);
            }
            COOLDOWNS.put(player.getUuid(), 600 + r.nextInt(1200)); // 30–90 сек паузы
        }
    }

    private static void heartbeatIfWatcherNear(ServerWorld world, ServerPlayerEntity player) {
        Box box = player.getBoundingBox().expand(20.0);
        List<WatcherEntity> near = world.getEntitiesByClass(WatcherEntity.class, box, e -> e.isAlive());
        if (!near.isEmpty()) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.HOSTILE, 1.0f, 1.0f);
        }
    }

    /** Звук в 6–10 блоках строго позади игрока. */
    private static void playScareBehind(ServerWorld world, ServerPlayerEntity player) {
        Random r = world.random;
        double yawRad = Math.toRadians(player.getYaw() + 180.0 + (r.nextDouble() - 0.5) * 60.0);
        double dist = 6 + r.nextInt(5);
        double x = player.getX() - Math.sin(yawRad) * dist;
        double z = player.getZ() + Math.cos(yawRad) * dist;
        SoundEvent sound = SCARE_SOUNDS[r.nextInt(SCARE_SOUNDS.length)];
        world.playSound(null, x, player.getY(), z, sound, SoundCategory.AMBIENT,
                1.2f, 0.6f + r.nextFloat() * 0.3f);
    }

    private static void trySpawnWatcher(ServerWorld world, ServerPlayerEntity player) {
        Box area = player.getBoundingBox().expand(64.0);
        if (!world.getEntitiesByClass(WatcherEntity.class, area, e -> true).isEmpty()) return;

        Random r = world.random;
        for (int i = 0; i < 12; i++) {
            double angle = r.nextDouble() * Math.PI * 2;
            double dist = 26 + r.nextInt(14);
            int x = MathHelper.floor(player.getX() + Math.cos(angle) * dist);
            int z = MathHelper.floor(player.getZ() + Math.sin(angle) * dist);

            if (!world.getChunkManager().isChunkLoaded(x >> 4, z >> 4)) continue;

            int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos below = pos.down();
            BlockState ground = world.getBlockState(below);

            if (!ground.isSolidBlock(world, below)) continue;
            if (world.getLightLevel(pos) > 7) continue;

            WatcherEntity watcher = ModEntities.WATCHER.create(world);
            if (watcher == null) return;
            watcher.refreshPositionAndAngles(x + 0.5, y, z + 0.5, r.nextFloat() * 360f, 0f);
            watcher.setTarget(player);
            world.spawnEntity(watcher);
            return;
        }
    }

    private HorrorDirector() {}
}
