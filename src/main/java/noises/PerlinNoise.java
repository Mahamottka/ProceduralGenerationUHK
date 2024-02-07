package noises;

import utils.FastNoiseLite;

public class PerlinNoise {

    float[][] noiseData;

    public PerlinNoise(int sizeX, int sizeY, float scale) {
        FastNoiseLite noise = new FastNoiseLite();
        noise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);

        noiseData = new float[sizeX][sizeY];

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                float scaledX = x * scale;
                float scaledY = y * scale;

                noiseData[x][y] = noise.GetNoise(scaledX, scaledY);

                // Debugging: Print out some noise values at corners and center
                if ((x == 0 || x == sizeX - 1 || x == sizeX / 2) &&
                        (y == 0 || y == sizeY - 1 || y == sizeY / 2)) {
                    System.out.println("Noise at (" + x + ", " + y + "): " + noiseData[x][y]);
                }
            }
        }
    }

    public float[][] getNoiseData() {
        return noiseData;
    }
}
