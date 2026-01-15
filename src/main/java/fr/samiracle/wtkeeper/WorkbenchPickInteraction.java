package fr.samiracle.wtkeeper;

import com.hypixel.hytale.builtin.crafting.state.BenchState;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class WorkbenchPickInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<WorkbenchPickInteraction> CODEC =
            BuilderCodec.builder(
                            WorkbenchPickInteraction.class,
                            WorkbenchPickInteraction::new,
                            SimpleInstantInteraction.CODEC
                    )
                    .documentation("Pick a workbench and keep its tier metadata.")
                    .build();

    public WorkbenchPickInteraction() {
        super();
    }

    @Override
    protected void firstRun(
            @Nonnull InteractionType interactionType,
            @Nonnull InteractionContext context,
            @Nonnull CooldownHandler cooldownHandler
    ) {
        if (interactionType != InteractionType.Pick) {
            return;
        }
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        if (commandBuffer == null) {
            return;
        }
        Ref<EntityStore> entityRef = context.getEntity();
        Player player = commandBuffer.getComponent(entityRef, Player.getComponentType());
        if (player == null || player.getGameMode() != GameMode.Creative) {
            return;
        }
        BlockPosition targetBlock = context.getTargetBlock();
        if (targetBlock == null) {
            return;
        }
        EntityStore entityStore = commandBuffer.getExternalData();
        World world = entityStore.getWorld();
        Vector3i pos = new Vector3i(targetBlock.x, targetBlock.y, targetBlock.z);
        BlockType blockType = world.getBlockType(pos);
        if (blockType == null || blockType.getBench() == null) {
            return;
        }

        BenchState benchState = WorkbenchTierUtils.getBenchState(world, pos);
        int tier = 1;
        if (benchState != null) {
            int level = benchState.getTierLevel();
            if (level > 0) {
                tier = level;
            }
        }

        ItemStack stack = WorkbenchTierUtils.createWorkbenchItem(
                blockType,
                benchState,
                tier,
                WorkbenchTierKeeper.META_TIER_KEY,
                true
        );
        if (stack == null || stack.isEmpty()) {
            return;
        }
        Inventory inventory = player.getInventory();
        if (inventory == null) {
            return;
        }
        byte slot = inventory.getActiveHotbarSlot();
        if (slot == Inventory.INACTIVE_SLOT_INDEX) {
            return;
        }
        ItemContainer hotbar = inventory.getHotbar();
        if (hotbar == null) {
            return;
        }
        hotbar.setItemStackForSlot((short) slot, stack);
        inventory.markChanged();
    }

}
