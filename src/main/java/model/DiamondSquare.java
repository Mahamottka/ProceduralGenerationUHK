package model;

public class DiamondSquare {

    private final float[][] heightMap;
    private final int size;
    private final float roughness;

    public DiamondSquare(int iterations, float roughness) {
        this.size = (1 << iterations) + 1;
        this.heightMap = new float[size][size];
        this.roughness = roughness;

        heightMap[0][0] = randValue();
        heightMap[0][size - 1] = randValue();
        heightMap[size - 1][0] = randValue();
        heightMap[size - 1][size - 1] = randValue();
    }

    public void generate() {
        int step = size - 1;

        for (int halfStep = step / 2; halfStep > 0; halfStep /= 2, step /= 2) {
            for (int y = halfStep; y < size - 1; y += step) {
                for (int x = halfStep; x < size - 1; x += step) {
                    squareStep(x, y, halfStep, randValue(step));
                }
            }

            for (int y = 0; y < size; y += halfStep) {
                for (int x = (y + halfStep) % step; x < size; x += step) {
                    diamondStep(x, y, halfStep, randValue(step));
                }
            }
        }
    }

    private void squareStep(int x, int y, int halfStep, float offset) {
        float avg = (heightMap[x - halfStep][y - halfStep] + heightMap[x + halfStep][y - halfStep] +
                heightMap[x - halfStep][y + halfStep] + heightMap[x + halfStep][y + halfStep]) * 0.25f;

        heightMap[x][y] = avg + offset;
    }

    private void diamondStep(int x, int y, int halfStep, float offset) {
        float avg = 0;
        int n = 0;
        if (x - halfStep >= 0) { avg += heightMap[x - halfStep][y]; n++; }
        if (x + halfStep < size) { avg += heightMap[x + halfStep][y]; n++; }
        if (y - halfStep >= 0) { avg += heightMap[x][y - halfStep]; n++; }
        if (y + halfStep < size) { avg += heightMap[x][y + halfStep]; n++; }

        avg /= n;

        heightMap[x][y] = avg + offset;
    }

    private float randValue(int scale) {
        return (float)(Math.random() - 0.5) * scale * roughness;
    }

    private float randValue() {
        return (float)Math.random();
    }

    public float[][] getHeightMap() {
        return heightMap;
    }
}
