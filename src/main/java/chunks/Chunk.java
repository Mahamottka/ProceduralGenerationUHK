package chunks;

import lwjglutils.OGLBuffers;
import noises.PerlinNoise;

public class Chunk {
    private int chunkX; // X-coordinate of the chunk, horizontal axis
    private int chunkZ; // Y-coordinate of the chunk, now used for vertical axis instead of Z
    private final int terrainSize;
    private final float terrainScale;
    private OGLBuffers buffers;

    public Chunk(int chunkX, int chunkZ, int terrainSize, float terrainScale) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ; // Z-axis for height
        this.terrainSize = terrainSize;
        this.terrainScale = terrainScale;
        createTerrain();
    }

    public void createTerrain() {
        float startX = chunkX * terrainSize * terrainScale;
        float startZ = chunkZ * terrainSize * terrainScale; // Start Z for height

        int extendedSize = terrainSize + 1;
        PerlinNoise perlinNoise = new PerlinNoise(extendedSize, extendedSize, terrainScale);
        float[][] noiseData = perlinNoise.getNoiseData();

        float[] vertices = new float[extendedSize * extendedSize * 3];
        int[] indices = new int[(extendedSize - 1) * (extendedSize - 1) * 6];

        float heightMultiplier = 100.0f;

        for (int y = 0; y < extendedSize; y++) {
            for (int x = 0; x < extendedSize; x++) {
                float worldX = startX + x * terrainScale; // X coordinate for front and back
                float worldY = startZ + y * terrainScale; // Y coordinate for left and right
                float worldZ = noiseData[x][y] * heightMultiplier; // Z coordinate for up and down (height)

                int vertexIndex = 3 * (y * extendedSize + x);
                vertices[vertexIndex] = worldX;
                vertices[vertexIndex + 1] = worldY;
                vertices[vertexIndex + 2] = worldZ;
            }
        }

        int index = 0;
        for (int y = 0; y < extendedSize - 1; y++) {
            for (int x = 0; x < extendedSize - 1; x++) {
                int baseIndex = y * extendedSize + x;
                indices[index++] = baseIndex;
                indices[index++] = baseIndex + 1;
                indices[index++] = baseIndex + extendedSize;
                indices[index++] = baseIndex + 1;
                indices[index++] = baseIndex + 1 + extendedSize;
                indices[index++] = baseIndex + extendedSize;
            }
        }

        OGLBuffers.Attrib[] attributes = { new OGLBuffers.Attrib("inPosition", 3) };
        buffers = new OGLBuffers(vertices, attributes, indices);
    }


    // Accessors
    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() { // Updated accessor for Y
        return chunkZ;
    }

    public OGLBuffers getBuffers() {
        return buffers;
    }

    public static String getKey(int chunkX, int chunkY) { // Adjusted to use X and Y
        return chunkX + "_" + chunkY;
    }
}
