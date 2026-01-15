package fr.samiracle.wtkeeper;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.builtin.crafting.state.BenchState;

import javax.annotation.Nonnull;

public class WorkbenchUpgradePlaceBlockSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

    private final ComponentType<ChunkStore, WorkbenchUpgradeChunk> componentType;
    private final String metaKey;

    public WorkbenchUpgradePlaceBlockSystem(
            ComponentType<ChunkStore, WorkbenchUpgradeChunk> componentType,
            String metaKey
    ) {
        super(PlaceBlockEvent.class);
        this.componentType = componentType;
        this.metaKey = metaKey;
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull PlaceBlockEvent event
    ) {
        if (event.isCancelled()) {
            return;
        }

        ItemStack itemInHand = event.getItemInHand();
        BlockType blockType = WorkbenchTierUtils.resolveBlockType(itemInHand);
        if (blockType == null || blockType.getBench() == null) {
            return;
        }

        EntityStore entityStore = (EntityStore) commandBuffer.getExternalData();
        World world = entityStore.getWorld();
        Vector3i pos = event.getTargetBlock();

        WorkbenchUpgradeChunk data = WorkbenchTierUtils.getChunkData(world, pos, componentType);
        if (data == null) {
            return;
        }

        Integer tier = itemInHand.getFromMetadataOrNull(metaKey, Codec.INTEGER);
        if (tier == null || tier <= 0) {
            data.removeTier(new Vector3i(pos));
            return;
        }
        data.setTier(new Vector3i(pos), tier);
        applyTierToBenchState(commandBuffer, world, pos, tier);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

    private void applyTierToBenchState(
            CommandBuffer<EntityStore> commandBuffer,
            World world,
            Vector3i pos,
            int tier
    ) {
        commandBuffer.run(store -> {
            BenchState benchState = WorkbenchTierUtils.getBenchState(world, pos);
            if (benchState != null) {
                benchState.setTierLevel(tier);
            }
        });
    }
}
