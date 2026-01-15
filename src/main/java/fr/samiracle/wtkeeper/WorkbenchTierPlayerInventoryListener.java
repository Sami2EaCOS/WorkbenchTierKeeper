package fr.samiracle.wtkeeper;

import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;

public class WorkbenchTierPlayerInventoryListener {

    private final WorkbenchTierItemNormalizer normalizer;

    public WorkbenchTierPlayerInventoryListener(String metaKey) {
        this.normalizer = new WorkbenchTierItemNormalizer(metaKey);
    }

    public void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        Inventory inventory = player.getInventory();
        if (inventory == null) {
            return;
        }
        boolean changed = normalizeInventory(inventory);
        if (changed) {
            inventory.markChanged();
        }
    }

    public void onInventoryChange(LivingEntityInventoryChangeEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }
        ItemContainer container = event.getItemContainer();
        if (container == null) {
            return;
        }
        boolean changed = normalizer.normalizeContainer(container);
        if (changed) {
            Inventory inventory = player.getInventory();
            if (inventory != null) {
                inventory.markChanged();
            }
        }
    }

    private boolean normalizeInventory(Inventory inventory) {
        boolean changed = false;
        ItemContainer[] containers = new ItemContainer[] {
                inventory.getHotbar(),
                inventory.getStorage(),
                inventory.getArmor(),
                inventory.getUtility(),
                inventory.getTools(),
                inventory.getBackpack()
        };
        for (ItemContainer container : containers) {
            changed |= normalizer.normalizeContainer(container);
        }
        return changed;
    }
}
