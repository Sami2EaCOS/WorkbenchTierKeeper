package fr.samiracle.wtkeeper;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.builtin.crafting.state.BenchState;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.protocol.GameMode;

import javax.annotation.Nonnull;

public class WorkbenchUpgradeBreakBlockSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    private final ComponentType<ChunkStore, WorkbenchUpgradeChunk> componentType;
    private final String metaKey;

    public WorkbenchUpgradeBreakBlockSystem(
            ComponentType<ChunkStore, WorkbenchUpgradeChunk> componentType,
            String metaKey
    ) {
        super(BreakBlockEvent.class);
        this.componentType = componentType;
        this.metaKey = metaKey;
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull BreakBlockEvent event
    ) {
        if (event.isCancelled()) {
            return;
        }

        BlockType blockType = event.getBlockType();
        if (blockType.getBench() == null) {
            return;
        }

        EntityStore entityStore = (EntityStore) commandBuffer.getExternalData();
        World world = entityStore.getWorld();
        Vector3i pos = event.getTargetBlock();

        BenchState benchState = WorkbenchTierUtils.getBenchState(world, pos);
        int tier = resolveTier(world, pos, benchState);
        if (tier <= 0) {
            return;
        }

        Ref<EntityStore> entityRef = chunk.getReferenceTo(index);
        if (isCreativePlayer(commandBuffer, entityRef)) {
            removeTierFromCache(world, pos);
            return;
        }

        ItemStack drop = WorkbenchTierUtils.createWorkbenchItem(
                blockType,
                benchState,
                tier,
                metaKey,
                false
        );
        if (drop == null || drop.isEmpty()) {
            removeTierFromCache(world, pos);
            return;
        }
        Holder<EntityStore> dropHolder = createDropHolder(commandBuffer, entityRef, drop, pos);
        if (dropHolder == null) {
            removeTierFromCache(world, pos);
            return;
        }
        commandBuffer.addEntity(dropHolder, AddReason.SPAWN);

        removeTierFromCache(world, pos);
        event.setCancelled(true);
        world.setBlock(pos.x, pos.y, pos.z, BlockType.EMPTY.getId());
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

    private int resolveTier(World world, Vector3i pos, BenchState benchState) {
        if (benchState != null) {
            int tier = benchState.getTierLevel();
            if (tier > 0) {
                return tier;
            }
        }
        WorkbenchUpgradeChunk data = WorkbenchTierUtils.getChunkData(world, pos, componentType);
        if (data == null) {
            return 0;
        }
        Integer tier = data.getTier(pos);
        return tier == null ? 0 : tier;
    }

    private void removeTierFromCache(World world, Vector3i pos) {
        WorkbenchUpgradeChunk data = WorkbenchTierUtils.getChunkData(world, pos, componentType);
        if (data != null) {
            data.removeTier(pos);
        }
    }

    private boolean isCreativePlayer(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> entityRef) {
        if (entityRef == null) {
            return false;
        }
        Player player = commandBuffer.getComponent(entityRef, Player.getComponentType());
        if (player == null) {
            return false;
        }
        return player.getGameMode() == GameMode.Creative;
    }

    private Holder<EntityStore> createDropHolder(
            CommandBuffer<EntityStore> commandBuffer,
            Ref<EntityStore> entityRef,
            ItemStack itemStack,
            Vector3i blockPos
    ) {
        Vector3d position = resolveDropPosition(commandBuffer, entityRef, blockPos);
        return ItemComponent.generateItemDrop(
                commandBuffer,
                itemStack,
                position,
                Vector3f.ZERO,
                0f,
                0f,
                0f
        );
    }

    private Vector3d resolveDropPosition(
            CommandBuffer<EntityStore> commandBuffer,
            Ref<EntityStore> entityRef,
            Vector3i blockPos
    ) {
        return new Vector3d(
                blockPos.x + 0.5d,
                blockPos.y + 0.5d,
                blockPos.z + 0.5d
        );
    }
}
