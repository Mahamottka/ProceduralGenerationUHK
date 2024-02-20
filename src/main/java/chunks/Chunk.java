package chunks;

import lwjglutils.OGLBuffers;
import noises.PerlinNoise;

public class Chunk {
    private int chunkX;
    private int chunkZ;
    private int terrainSize;
    private float terrainScale;
    private OGLBuffers buffers;

    // Constructor
    public Chunk(int chunkX, int chunkZ, int terrainSize, float terrainScale) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.terrainSize = terrainSize;
        this.terrainScale = terrainScale;
    }

    // Method to create terrain
    public void createTerrain() {
        // Calculate the world coordinates of the chunk's origin
        float startX = chunkX * terrainSize;
        float startZ = chunkZ * terrainSize;

        // Increase terrain size to reduce visible seams
        int extendedSize = terrainSize + 1;
        PerlinNoise perlinNoise = new PerlinNoise(extendedSize, extendedSize, terrainScale);
        float[][] noiseData = perlinNoise.getNoiseData();

        float[] vertices = new float[extendedSize * extendedSize * 3];
        int[] indices = new int[extendedSize * extendedSize * 6];

        float heightMultiplier = 20.0f; // Adjust this value to increase/decrease terrain height

        for (int z = 0; z < extendedSize; z++) {
            for (int x = 0; x < extendedSize; x++) {
                // Calculate the world coordinates for each vertex
                float worldX = startX + x - 1;
                float worldZ = startZ + z - 1;
                // Apply the noise data with a height multiplier
                vertices[3 * (z * extendedSize + x)] = worldX;
                vertices[3 * (z * extendedSize + x) + 1] = worldZ;
                vertices[3 * (z * extendedSize + x) + 2] = noiseData[x][z] * heightMultiplier;
            }
        }

        int index = 0;
        for (int z = 0; z < extendedSize - 1; z++) {
            for (int x = 0; x < extendedSize - 1; x++) {
                // Calculate the indices for the current quad
                indices[index++] = z * extendedSize + x;
                indices[index++] = z * extendedSize + x + 1;
                indices[index++] = (z + 1) * extendedSize + x;
                indices[index++] = (z + 1) * extendedSize + x;
                indices[index++] = z * extendedSize + x + 1;
                indices[index++] = (z + 1) * extendedSize + x + 1;
            }
        }

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 3)
        };

        buffers = new OGLBuffers(vertices, attributes, indices);
    }

    // Accessor for OGLBuffers
    public OGLBuffers getBuffers() {
        return buffers;
    }

    // Accessors for chunk coordinates
    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    // Method to get a unique key for the chunk based on its coordinates
    public static String getKey(int chunkX, int chunkZ) {
        return chunkX + "_" + chunkZ;
    }
}
