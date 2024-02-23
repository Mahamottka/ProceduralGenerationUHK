package chunks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkManager {
    private final int terrainSize;
    private final float terrainScale;
    private final Map<String, Chunk> chunks = new ConcurrentHashMap<>();

    public ChunkManager(int terrainSize, float terrainScale) {
        this.terrainSize = terrainSize;
        this.terrainScale = terrainScale;
    }

    public void generateChunk(int chunkX, int chunkY) {
        String key = Chunk.getKey(chunkX, chunkY);
        chunks.computeIfAbsent(key, k -> new Chunk(chunkX, chunkY, terrainSize, terrainScale));
    }

    public Chunk getChunk(int chunkX, int chunkY) {
        return chunks.get(Chunk.getKey(chunkX, chunkY));
    }
}
