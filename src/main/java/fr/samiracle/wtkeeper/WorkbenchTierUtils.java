package fr.samiracle.wtkeeper;

import com.hypixel.hytale.builtin.crafting.state.BenchState;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerBlockState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import java.lang.reflect.Field;

final class WorkbenchTierUtils {

    private WorkbenchTierUtils() {
    }

    static ItemStack createWorkbenchItem(
            BlockType blockType,
            BenchState benchState,
            int tier,
            String metaKey,
            boolean allowTierFallback
    ) {
        if (blockType.getItem() == null || blockType.getItem().getId() == null) {
            return ItemStack.EMPTY;
        }
        String resolvedItemId = blockType.getItem().getId();
        String stateName = benchState != null ? benchState.getTierStateName() : null;
        if (stateName != null) {
            String stateItemId = blockType.getItem().getItemIdForState(stateName);
            if (stateItemId != null) {
                resolvedItemId = stateItemId;
            }
        } else if (allowTierFallback && tier > 1) {
            String stateItemId = blockType.getItem().getItemIdForState("Tier" + tier);
            if (stateItemId != null) {
                resolvedItemId = stateItemId;
            }
        }
        return new ItemStack(resolvedItemId, 1)
                .withMetadata(metaKey, Codec.INTEGER, tier);
    }

    static BenchState getBenchState(World world, Vector3i pos) {
        long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
        WorldChunk worldChunk = world.getChunkIfLoaded(chunkIndex);
        if (worldChunk == null) {
            return null;
        }
        Object state = worldChunk.getState(pos.x, pos.y, pos.z);
        if (state instanceof BenchState) {
            return (BenchState) state;
        }
        return null;
    }

    static ItemContainer getItemContainer(World world, Vector3i pos) {
        long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
        WorldChunk worldChunk = world.getChunkIfLoaded(chunkIndex);
        if (worldChunk == null) {
            return null;
        }
        Object state = worldChunk.getState(pos.x, pos.y, pos.z);
        if (state instanceof ItemContainerBlockState) {
            return ((ItemContainerBlockState) state).getItemContainer();
        }
        return null;
    }

    static void clearUpgradeItems(BenchState benchState) {
        if (benchState == null) {
            return;
        }
        try {
            Field upgradeItems = BenchState.class.getDeclaredField("upgradeItems");
            upgradeItems.setAccessible(true);
            upgradeItems.set(benchState, new ItemStack[0]);
        } catch (ReflectiveOperationException ignored) {
            // Best-effort: avoid upgrade item drops when the field layout changes.
        }
    }

    static BlockType resolveBlockType(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return null;
        }
        String blockKey = itemStack.getBlockKey();
        if (blockKey == null) {
            return null;
        }
        return BlockType.getAssetMap().getAsset(blockKey);
    }

    static WorkbenchUpgradeChunk getChunkData(
            World world,
            Vector3i pos,
            ComponentType<ChunkStore, WorkbenchUpgradeChunk> componentType
    ) {
        ChunkStore chunkStore = world.getChunkStore();
        long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
        Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
        if (chunkRef == null) {
            return null;
        }
        return (WorkbenchUpgradeChunk) chunkStore.getStore().getComponent(chunkRef, componentType);
    }
}
