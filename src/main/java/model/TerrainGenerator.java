package model;

import static org.lwjgl.opengl.GL11.*;

public class TerrainGenerator {

    private float[][] noiseData; // 2D noise data for heightmap
    private int sizeX, sizeY; // Dimensions of the noise data

    public TerrainGenerator(float[][] noiseData) {
        this.noiseData = noiseData;
        this.sizeX = noiseData.length;
        this.sizeY = noiseData[0].length;
    }

    public void render() {
        glBegin(GL_TRIANGLES);
        for (int x = 0; x < sizeX - 1; x++) {
            for (int z = 0; z < sizeY - 1; z++) {
                float y0 = noiseData[x][z] * 10;
                float y1 = noiseData[x + 1][z] * 10;
                float y2 = noiseData[x][z + 1] * 10;
                float y3 = noiseData[x + 1][z + 1] * 10;

                // Triangle 1
                glVertex3f(x, y0, z);
                glVertex3f(x + 1, y1, z);
                glVertex3f(x, y2, z + 1);

                // Triangle 2
                glVertex3f(x + 1, y1, z);
                glVertex3f(x + 1, y3, z + 1);
                glVertex3f(x, y2, z + 1);
            }
        }
        glEnd();

        // Draw wireframe lines
        glColor3f(0.0f, 0.0f, 0.0f);
        glBegin(GL_LINES);
        for (int x = 0; x < sizeX - 1; x++) {
            for (int z = 0; z < sizeY - 1; z++) {
                float y0 = noiseData[x][z] * 10;
                float y1 = noiseData[x + 1][z] * 10;
                float y2 = noiseData[x][z + 1] * 10;
                float y3 = noiseData[x + 1][z + 1] * 10;

                // Draw lines connecting the vertices
                drawLine(x, y0, z, x + 1, y1, z);
                drawLine(x + 1, y1, z, x + 1, y3, z + 1);
                drawLine(x + 1, y3, z + 1, x, y2, z + 1);
                drawLine(x, y2, z + 1, x, y0, z);
            }
        }
        glEnd();
        glColor3f(1.0f, 1.0f, 1.0f);
    }

    private void drawLine(float x0, float y0, float z0, float x1, float y1, float z1) {
        glVertex3f(x0, y0, z0);
        glVertex3f(x1, y1, z1);
    }
}
