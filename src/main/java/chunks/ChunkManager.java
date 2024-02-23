package chunks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import noises.PerlinNoise;

public class ChunkManager {
    private final int terrainSize;
    private final float terrainScale;
    private final Map<String, Chunk> chunks = new ConcurrentHashMap<>();
    private final PerlinNoise perlinNoise;

    public ChunkManager(int terrainSize, float terrainScale) {
        this.terrainSize = terrainSize;
        this.terrainScale = terrainScale;
        this.perlinNoise = new PerlinNoise(); // Assume PerlinNoise is correctly set up for global use
    }

    public void generateChunk(int chunkX, int chunkY) {
        String key = getKey(chunkX, chunkY); // Use static method directly
        chunks.computeIfAbsent(key, k -> new Chunk(chunkX, chunkY, terrainSize, terrainScale, perlinNoise));
    }

    public Chunk getChunk(int chunkX, int chunkY) {
        return chunks.get(getKey(chunkX, chunkY));
    }

    public static String getKey(int chunkX, int chunkY) {
        // This method generates a unique key for each chunk based on its coordinates.
        // It's crucial for identifying and retrieving specific chunks from the collection.
        return chunkX + "_" + chunkY;
    }
}
