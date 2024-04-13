package noises;

import utils.FastNoiseLite;

public class PerlinNoise {
    private FastNoiseLite noise;

    public PerlinNoise(int seed) {
        noise = new FastNoiseLite();
        noise.SetSeed(seed);
        noise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
    }

    public float getNoise(float x, float y) {
        return noise.GetNoise(x, y);
    }
}
