package fr.samiracle.wtkeeper;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import javax.annotation.Nonnull;

public class WorkbenchUpgradeEnsureChunkSystem extends HolderSystem<ChunkStore> {

    private final ComponentType<ChunkStore, WorkbenchUpgradeChunk> componentType;

    public WorkbenchUpgradeEnsureChunkSystem(
            ComponentType<ChunkStore, WorkbenchUpgradeChunk> componentType
    ) {
        this.componentType = componentType;
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
