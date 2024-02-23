package chunks;

import lwjglutils.OGLBuffers;
import noises.PerlinNoise;

public class Chunk {
    private int chunkX; // X-coordinate of the chunk
    private int chunkY; // Y-coordinate of the chunk, adjusted for clarity
    private final int terrainSize;
    private final float terrainScale;
    private OGLBuffers buffers;

    public Chunk(int chunkX, int chunkY, int terrainSize, float terrainScale) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.terrainSize = terrainSize;
        this.terrainScale = terrainScale;
        createTerrain();
    }

    public void createTerrain() {
        // Adjusted for clarity with X and Y coordinates
        float startX = chunkX * terrainSize * terrainScale;
        float startY = chunkY * terrainSize * terrainScale; // Use startY for consistency

        int extendedSize = terrainSize + 1;
        PerlinNoise perlinNoise = new PerlinNoise(extendedSize, extendedSize, terrainScale);
        float[][] noiseData = perlinNoise.getNoiseData();

        float[] vertices = new float[extendedSize * extendedSize * 3];
        int[] indices = new int[(extendedSize - 1) * (extendedSize - 1) * 6];

        float heightMultiplier = 20.0f;

        for (int y = 0; y < extendedSize; y++) {
            for (int x = 0; x < extendedSize; x++) {
                float worldX = startX + x * terrainScale;
                float worldZ = noiseData[x][y] * heightMultiplier; // Use as height
                float worldY = startY + y * terrainScale; // Adjusted for depth

                int vertexIndex = 3 * (y * extendedSize + x);
                vertices[vertexIndex] = worldX;
                vertices[vertexIndex + 1] = worldZ; // Height
                vertices[vertexIndex + 2] = worldY; // Depth
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

    public int getChunkY() {
        return chunkY;
    }

    public OGLBuffers getBuffers() {
        return buffers;
    }

    public static String getKey(int chunkX, int chunkY) {
        return chunkX + "_" + chunkY;
    }
}
