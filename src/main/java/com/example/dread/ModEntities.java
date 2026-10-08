package com.example.dread;

import com.example.dread.entity.WatcherEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;

public final class ModEntities {
    public static final EntityType<WatcherEntity> WATCHER = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(DreadMod.MOD_ID, "watcher"),
            FabricEntityTypeBuilder.<WatcherEntity>createMob()
                    .spawnGroup(SpawnGroup.MONSTER)
                    .entityFactory(WatcherEntity::new)
                    .spawnRestriction(SpawnRestriction.Location.ON_GROUND,
                            Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                            HostileEntity::canSpawnInDark)
                    .dimensions(EntityDimensions.fixed(0.6f, 2.2f))
                    .trackRangeBlocks(64)
                    .build()
    );

    public static void register() {
        FabricDefaultAttributeRegistry.register(WATCHER, WatcherEntity.createAttributes());

        // Естественный спавн: очень редкий, по одному, в Верхнем мире
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(),
                SpawnGroup.MONSTER, WATCHER, 2, 1, 1);
    }

    private ModEntities() {}
}
