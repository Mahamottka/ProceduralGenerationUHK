package model;

public class DiamondSquare {

    private float[][] heightMap;
    private int size;
    private float roughness;
    private float firstValue, secondValue, thirdValue, fourthValue;
    private boolean useRandomness;  // Flag to toggle randomness

    public DiamondSquare(int iterations, float roughness, float firstValue, float secondValue, float thirdValue, float fourthValue, boolean useRandomness) {
        this.roughness = roughness;
        this.firstValue = firstValue;
        this.secondValue = secondValue;
        this.thirdValue = thirdValue;
        this.fourthValue = fourthValue;
        this.useRandomness = useRandomness;
        setSize((1 << iterations) + 1);
    }

    private void setSize(int newSize) {
        this.size = newSize;
        this.heightMap = new float[size][size];
        initializeCorners();
        generate();
    }

    private void initializeCorners() {
        heightMap[0][0] = firstValue;
        heightMap[0][size - 1] = secondValue;
        heightMap[size - 1][0] = thirdValue;
        heightMap[size - 1][size - 1] = fourthValue;
    }

    public void generate() {
        int step = size - 1;
        for (int halfStep = step / 2; halfStep > 0; halfStep /= 2, step /= 2) {
            for (int y = halfStep; y < size - 1; y += step) {
                for (int x = halfStep; x < size - 1; x += step) {
                    squareStep(x, y, halfStep);
                }
            }
            for (int y = 0; y < size; y += halfStep) {
                for (int x = (y + halfStep) % step; x < size; x += step) {
                    diamondStep(x, y, halfStep);
                }
            }
        }
    }

    private void squareStep(int x, int y, int halfStep) {
        float avg = (heightMap[x - halfStep][y - halfStep] + heightMap[x + halfStep][y - halfStep] +
                heightMap[x - halfStep][y + halfStep] + heightMap[x + halfStep][y + halfStep]) * 0.25f;
        float offset = useRandomness ? randValue(halfStep) : 0;
        heightMap[x][y] = avg + offset;
    }

    private void diamondStep(int x, int y, int halfStep) {
        float avg = 0;
        int n = 0;
        if (x - halfStep >= 0) { avg += heightMap[x - halfStep][y]; n++; }
        if (x + halfStep < size) { avg += heightMap[x + halfStep][y]; n++; }
        if (y - halfStep >= 0) { avg += heightMap[x][y - halfStep]; n++; }
        if (y + halfStep < size) { avg += heightMap[x][y + halfStep]; n++; }

        avg /= n;
        float offset = useRandomness ? randValue(halfStep) : 0;
        heightMap[x][y] = avg + offset;
    }

    private float randValue(int scale) {
        return (float)(Math.random() - 0.5) * scale * roughness;
    }

    // Getters and Setters
    public void setRoughness(float roughness) {
        this.roughness = roughness;
    }

    public float getRoughness() {
        return roughness;
    }

    public void setIterations(int iterations) {
        setSize((1 << iterations) + 1);
    }

    public int getIterations() {
        return (int) (Math.log(size - 1) / Math.log(2));
    }

    public void setCornerValues(float firstValue, float secondValue, float thirdValue, float fourthValue) {
        this.firstValue = firstValue;
        this.secondValue = secondValue;
        this.thirdValue = thirdValue;
        this.fourthValue = fourthValue;
        initializeCorners();
    }

    public float getFirstValue() {
        return firstValue;
    }

    public float getSecondValue() {
        return secondValue;
    }

    public float getThirdValue() {
        return thirdValue;
    }

    public float getFourthValue() {
        return fourthValue;
    }

    public float[][] getHeightMap() {
        return heightMap;
    }
}
