package fr.samiracle.wtkeeper;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;

public class WorkbenchTierItemNormalizer {

    private final String metaKey;

    public WorkbenchTierItemNormalizer(String metaKey) {
        this.metaKey = metaKey;
    }

    public boolean normalizeContainer(ItemContainer container) {
        if (container == null) {
            return false;
        }
        boolean[] changed = new boolean[] {false};
        container.forEach((slot, stack) -> {
            ItemStack updated = normalizeStack(stack);
            if (updated != null && !updated.equals(stack)) {
                container.setItemStackForSlot(slot, updated);
                changed[0] = true;
            }
        });
        return changed[0];
    }

    public ItemStack normalizeStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        Item item = stack.getItem();
        BlockType blockType = WorkbenchTierUtils.resolveBlockType(stack);
        if (blockType == null || blockType.getBench() == null) {
            return null;
        }
        Integer tierMeta = stack.getFromMetadataOrNull(metaKey, Codec.INTEGER);
        int tierFromMeta = tierMeta != null ? tierMeta : 0;
        int tierFromState = resolveTierFromState(blockType, item, stack);
        int tier = tierFromMeta > 0 ? tierFromMeta : Math.max(tierFromState, 1);

        ItemStack updated = stack;
        if (tierFromMeta != tier) {
            updated = updated.withMetadata(metaKey, Codec.INTEGER, tier);
        }

        if (tier > 1) {
            String stateName = "Tier" + tier;
            if (updated.getItem().getItemIdForState(stateName) != null) {
                try {
                    updated = updated.withState(stateName);
                } catch (IllegalArgumentException ignored) {
                    // State not available for this item.
                }
            }
        }
        return updated;
    }

    private int resolveTierFromState(BlockType blockType, Item item, ItemStack stack) {
        Item baseItem = blockType.getItem() != null ? blockType.getItem() : item;
        if (baseItem == null) {
            return 0;
        }
        String stateName = baseItem.getStateForItem(stack.getItemId());
        if (stateName == null || !stateName.startsWith("Tier")) {
            return 0;
        }
        try {
            return Integer.parseInt(stateName.substring(4));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
