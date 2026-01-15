package fr.samiracle.wtkeeper;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;

import javax.annotation.Nonnull;

public class WorkbenchTierKeeper extends JavaPlugin {

    public static final String META_TIER_KEY = "wtkeeper:tier";

    private ComponentType<ChunkStore, WorkbenchUpgradeChunk> workbenchUpgradeChunkType;

    public WorkbenchTierKeeper(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();
        ComponentRegistryProxy<ChunkStore> chunkRegistry = getChunkStoreRegistry();
        ComponentRegistryProxy<EntityStore> entityRegistry = getEntityStoreRegistry();

        workbenchUpgradeChunkType = chunkRegistry.registerComponent(
                WorkbenchUpgradeChunk.class,
                "WorkbenchUpgradeChunk",
                WorkbenchUpgradeChunk.CODEC
        );
        chunkRegistry.registerSystem(new WorkbenchUpgradeEnsureChunkSystem(workbenchUpgradeChunkType));

        Interaction.CODEC.register(
                "WorkbenchPick",
                WorkbenchPickInteraction.class,
                WorkbenchPickInteraction.CODEC
        );
        entityRegistry.registerSystem(new WorkbenchUpgradePlaceBlockSystem(
                workbenchUpgradeChunkType,
                META_TIER_KEY
        ));
        entityRegistry.registerSystem(new WorkbenchUpgradeBreakBlockSystem(
                workbenchUpgradeChunkType,
                META_TIER_KEY
        ));
        chunkRegistry.registerSystem(
                new WorkbenchTierChunkContainerScanSystem(workbenchUpgradeChunkType, META_TIER_KEY)
        );

        WorkbenchTierPlayerInventoryListener inventoryListener =
                new WorkbenchTierPlayerInventoryListener(META_TIER_KEY);
        getEventRegistry().registerGlobal(
                PlayerReadyEvent.class,
                inventoryListener::onPlayerReady
        );
        getEventRegistry().registerGlobal(
                LivingEntityInventoryChangeEvent.class,
                inventoryListener::onInventoryChange
        );
    }

}
