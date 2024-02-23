package noises;

import utils.FastNoiseLite;

public class PerlinNoise {
    private FastNoiseLite noise;

    public PerlinNoise() {
        noise = new FastNoiseLite();
        noise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
        // Consider setting other properties or exposing them through the constructor
    }

    public float getNoise(float x, float y) {
        return noise.GetNoise(x, y);
    }
}
