package com.example.dread;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
    public static final Item WATCHER_SPAWN_EGG = Registry.register(
            Registries.ITEM,
            new Identifier(DreadMod.MOD_ID, "watcher_spawn_egg"),
            new SpawnEggItem(ModEntities.WATCHER, 0x0A0A0C, 0xC81E1E, new FabricItemSettings())
    );

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
                .register(entries -> entries.add(WATCHER_SPAWN_EGG));
    }

    private ModItems() {}
}
