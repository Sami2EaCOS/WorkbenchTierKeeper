package fr.samiracle.wtkeeper;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WorkbenchUpgradeChunk implements Component<ChunkStore> {

    public static final BuilderCodec<WorkbenchUpgradeChunk> CODEC =
            BuilderCodec.builder(WorkbenchUpgradeChunk.class, WorkbenchUpgradeChunk::new)
                    .append(
                            new KeyedCodec<>("Data", Codec.STRING_ARRAY),
                            WorkbenchUpgradeChunk::deserialize,
                            WorkbenchUpgradeChunk::serialize
                    )
                    .add()
                    .build();

    private final Map<Vector3i, Integer> tierByBlock;

    public WorkbenchUpgradeChunk() {
        this.tierByBlock = new HashMap<>();
    }

    public Integer getTier(Vector3i blockPos) {
        return tierByBlock.get(blockPos);
    }

    public void setTier(Vector3i blockPos, int tier) {
        Objects.requireNonNull(blockPos, "blockPos");
        if (tier <= 0) {
            tierByBlock.remove(blockPos);
            return;
        }
        tierByBlock.put(blockPos, tier);
    }

    public Integer removeTier(Vector3i blockPos) {
        return tierByBlock.remove(blockPos);
    }

    public void deserialize(String[] data) {
        tierByBlock.clear();
        if (data == null || data.length == 0) {
            return;
        }
        for (String entry : data) {
            if (entry == null || entry.isEmpty()) {
                continue;
            }
            String[] parts = entry.split(",", 4);
            if (parts.length != 4) {
                continue;
            }
            int x;
            int y;
            int z;
            int tier;
            try {
                x = Integer.parseInt(parts[0]);
                y = Integer.parseInt(parts[1]);
                z = Integer.parseInt(parts[2]);
                tier = Integer.parseInt(parts[3]);
            } catch (NumberFormatException ex) {
                continue;
            }
            if (tier > 0) {
                tierByBlock.put(new Vector3i(x, y, z), tier);
            }
        }
    }

    public String[] serialize() {
        String[] entries = new String[tierByBlock.size()];
        int index = 0;
        for (Map.Entry<Vector3i, Integer> entry : tierByBlock.entrySet()) {
            Vector3i pos = entry.getKey();
            entries[index++] = pos.x + "," + pos.y + "," + pos.z + "," + entry.getValue();
        }
        return entries;
    }

    @Override
    public WorkbenchUpgradeChunk clone() {
        WorkbenchUpgradeChunk clone = new WorkbenchUpgradeChunk();
        for (Map.Entry<Vector3i, Integer> entry : tierByBlock.entrySet()) {
            clone.tierByBlock.put(new Vector3i(entry.getKey()), entry.getValue());
        }
        return clone;
    }
}
