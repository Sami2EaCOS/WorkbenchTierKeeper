package fr.samiracle.wtkeeper;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerBlockState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import javax.annotation.Nonnull;

public class WorkbenchTierChunkContainerScanSystem extends HolderSystem<ChunkStore> {

    private final ComponentType<ChunkStore, WorkbenchUpgradeChunk> componentType;
    private final WorkbenchTierItemNormalizer normalizer;

    public WorkbenchTierChunkContainerScanSystem(
            ComponentType<ChunkStore, WorkbenchUpgradeChunk> componentType,
            String metaKey
    ) {
        this.componentType = componentType;
        this.normalizer = new WorkbenchTierItemNormalizer(metaKey);
    }

    @Override
    public Query<ChunkStore> getQuery() {
        return WorldChunk.getComponentType();
    }

    @Override
    public void onEntityAdd(
            @Nonnull Holder<ChunkStore> holder,
            @Nonnull AddReason reason,
            @Nonnull Store<ChunkStore> store
    ) {
        holder.ensureComponent(componentType);
        WorldChunk chunk = holder.getComponent(WorldChunk.getComponentType());
        if (chunk == null) {
            return;
        }
        int minY = ChunkUtil.MIN_Y;
        int maxY = minY + ChunkUtil.HEIGHT;
        for (int x = 0; x < ChunkUtil.SIZE; x++) {
            for (int z = 0; z < ChunkUtil.SIZE; z++) {
                for (int y = minY; y < maxY; y++) {
                    Object state = chunk.getState(x, y, z);
                    if (state instanceof ItemContainerBlockState) {
                        ItemContainer container = ((ItemContainerBlockState) state).getItemContainer();
                        normalizer.normalizeContainer(container);
                    }
                }
            }
        }
    }

    @Override
    public void onEntityRemoved(
            @Nonnull Holder<ChunkStore> holder,
            @Nonnull RemoveReason reason,
            @Nonnull Store<ChunkStore> store
    ) {
        // No-op.
    }
}
