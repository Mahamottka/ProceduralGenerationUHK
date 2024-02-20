package chunks;

import lwjglutils.OGLBuffers;
import noises.PerlinNoise;

import java.util.HashMap;
import java.util.Map;

public class ChunkManager {
    private final int terrainSize;
    private final float terrainScale;
    private final Map<String, Chunk> chunks;

    public ChunkManager(int terrainSize, float terrainScale) {
        this.terrainSize = terrainSize;
        this.terrainScale = terrainScale;
        this.chunks = new HashMap<>();
    }

    // Method to add a chunk to the manager
    public void addChunk(Chunk chunk) {
        String key = Chunk.getKey(chunk.getChunkX(), chunk.getChunkZ());
        chunks.put(key, chunk);
    }


    // Method to get a chunk from the manager based on chunk coordinates
    public Chunk getChunk(int chunkX, int chunkZ) {
        String key = Chunk.getKey(chunkX, chunkZ);
        return chunks.get(key);
    }

    // Method to generate and add a new chunk to the manager
    public void generateChunk(int chunkX, int chunkZ) {
        String key = Chunk.getKey(chunkX, chunkZ);
        if (!chunks.containsKey(key)) {
            Chunk chunk = new Chunk(chunkX, chunkZ, terrainSize, terrainScale);
            chunk.createTerrain();
            addChunk(chunk);
        }
    }
}
