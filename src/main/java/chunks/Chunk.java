package chunks;

import lwjglutils.OGLBuffers;
import noises.PerlinNoise;

public class Chunk {
    private final int chunkX;
    private final int chunkY;
    private final int terrainSize;
    private final float terrainScale;
    private OGLBuffers buffers;
    private PerlinNoise perlinNoise;

    public Chunk(int chunkX, int chunkY, int terrainSize, float terrainScale, PerlinNoise perlinNoise) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.terrainSize = terrainSize;
        this.terrainScale = terrainScale;
        this.perlinNoise = perlinNoise;
        createTerrain();
    }

    private void createTerrain() {
        int extendedSize = terrainSize + 1; // +1 to ensure we have an edge to stitch with adjacent chunks
        float[] vertices = new float[extendedSize * extendedSize * 3];
        int[] indices = new int[terrainSize * terrainSize * 6];

        float heightMultiplier = 100.0f;

        for (int y = 0; y < extendedSize; y++) {
            for (int x = 0; x < extendedSize; x++) {
                // Here, x and y represent the horizontal plane, with z being the height
                float globalX = (chunkX * terrainSize + x) * terrainScale;
                float globalY = (chunkY * terrainSize + y) * terrainScale;
                float height = perlinNoise.getNoise(globalX, globalY) * heightMultiplier;

                int vertexIndex = y * extendedSize + x;
                vertices[vertexIndex * 3] = globalX; // X - Front and Back
                vertices[vertexIndex * 3 + 1] = globalY; // Y - Left and Right
                vertices[vertexIndex * 3 + 2] = height; // Z - Up and Down (Height)
            }
        }

        int index = 0;
        for (int y = 0; y < terrainSize; y++) {
            for (int x = 0; x < terrainSize; x++) {
                int topLeft = y * extendedSize + x;
                int topRight = topLeft + 1;
                int bottomLeft = topLeft + extendedSize;
                int bottomRight = bottomLeft + 1;

                // First triangle
                indices[index++] = topLeft;
                indices[index++] = bottomLeft;
                indices[index++] = topRight;

                // Second triangle
                indices[index++] = topRight;
                indices[index++] = bottomLeft;
                indices[index++] = bottomRight;
            }
        }

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 3)
        };
        buffers = new OGLBuffers(vertices, attributes, indices);
    }


    public OGLBuffers getBuffers() {
        return buffers;
    }

    public static String getKey(int chunkX, int chunkY) {
        return chunkX + "_" + chunkY;
    }
}
