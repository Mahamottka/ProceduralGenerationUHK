package chunks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import noises.PerlinNoise;

public class ChunkManager {
    private final int terrainSize;
    private float terrainScale;  // Not final anymore, to allow changes
    private Map<String, Chunk> chunks = new ConcurrentHashMap<>();
    private PerlinNoise perlinNoise;
    private int seed;

    public ChunkManager(int terrainSize, float terrainScale, int seed) {
        this.terrainSize = terrainSize;
        this.terrainScale = terrainScale;
        initializePerlinNoise(seed);
    }

    private void initializePerlinNoise(int newSeed) {
        this.seed = newSeed;
        this.perlinNoise = new PerlinNoise(seed);
        this.chunks.clear(); // Clear existing chunks
    }

    public void regenerateTerrain() {
        initializePerlinNoise(this.seed);
        for (String key : chunks.keySet()) {
            String[] parts = key.split("_");
            int chunkX = Integer.parseInt(parts[0]);
            int chunkY = Integer.parseInt(parts[1]);
            chunks.put(key, new Chunk(chunkX, chunkY, terrainSize, terrainScale, perlinNoise)); // Recreate chunks with new scale
        }
    }

    public void setSeed(int newSeed) {
        if (this.seed != newSeed) {
            initializePerlinNoise(newSeed);
        }
    }

    public void setTerrainScale(float newScale) {
        if (this.terrainScale != newScale) {
            this.terrainScale = newScale;
            regenerateTerrain(); // Regenerate all chunks to apply the new scale
        }
    }

    public float getTerrainScale() {
        return this.terrainScale;
    }

    public void generateChunk(int chunkX, int chunkY) {
        String key = getKey(chunkX, chunkY);
        chunks.computeIfAbsent(key, k -> new Chunk(chunkX, chunkY, terrainSize, terrainScale, perlinNoise));
    }

    public Chunk getChunk(int chunkX, int chunkY) {
        return chunks.get(getKey(chunkX, chunkY));
    }

    public static String getKey(int chunkX, int chunkY) {
        return chunkX + "_" + chunkY;
    }
}
