package model;

import chunks.Chunk;
import chunks.ChunkManager;

import java.nio.FloatBuffer;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import lwjglutils.OGLTextRenderer;
import lwjglutils.ShaderUtils;
import lwjglutils.ToFloatArray;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;
import transforms.Camera;
import transforms.Mat4;
import transforms.Mat4PerspRH;
import transforms.Vec3D;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Renderer {
    int seed = 333;
    int terrainSize = 128;
    float terrainScale = 2f;
    int width = 800, height = 600;
    private boolean wireframe = false;
    double ox, oy;
    private boolean mouseButton1 = false;
    private long window;
    private ChunkManager chunkManager;
    OGLTextRenderer textRenderer;
    int shaderProgram, locMat, locNormalMat, locLightDir;
    ImInt inputInt = new ImInt(0); // You can set this to a default seed if needed
    ImString inputText = new ImString(50); // Initialize with "100" as a default seed
    private int selectedMode = 0;
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    Camera cam = new Camera();
    Mat4 proj = new Mat4PerspRH(Math.PI / 4, 1, 0.01, 3000.0);

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(width, height, "Procedural generation", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    case GLFW_KEY_W:
                        cam = cam.forward(5);
                        break;
                    case GLFW_KEY_D:
                        cam = cam.right(5);
                        break;
                    case GLFW_KEY_S:
                        cam = cam.backward(5);
                        break;
                    case GLFW_KEY_A:
                        cam = cam.left(5);
                        break;
                    case GLFW_KEY_LEFT_CONTROL:
                        cam = cam.down(5);
                        break;
                    case GLFW_KEY_LEFT_SHIFT:
                        cam = cam.up(5);
                        break;
                    case GLFW_KEY_P:
                        if (action == GLFW_PRESS) { // Only toggle on key press, not repeat
                            wireframe = !wireframe; // Toggle the wireframe boolean
                            if (wireframe) {
                                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                            } else {
                                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                            }
                        }
                        break;

                }
            }
        });

        glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
                if (mouseButton1) {
                    cam = cam.addAzimuth((double) Math.PI * (ox - x) / width)
                            .addZenith((double) Math.PI * (oy - y) / width);
                    ox = x;
                    oy = y;
                }
            }
        });

        glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                mouseButton1 = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS;
                if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS) {
                    mouseButton1 = true;
                    DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                    DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                    glfwGetCursorPos(window, xBuffer, yBuffer);
                    ox = xBuffer.get(0);
                    oy = yBuffer.get(0);
                }
                if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_RELEASE) {
                    mouseButton1 = false;
                    DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                    DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                    glfwGetCursorPos(window, xBuffer, yBuffer);
                    double x = xBuffer.get(0);
                    double y = yBuffer.get(0);
                    cam = cam.addAzimuth((double) Math.PI * (ox - x) / width)
                            .addZenith((double) Math.PI * (oy - y) / width);
                    ox = x;
                    oy = y;
                }
            }
        });

        glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0 && (w != width || h != height)) {
                    width = w;
                    height = h;
                    proj = new Mat4PerspRH(Math.PI / 4, height / (double) width, 0.01, 3000.0); // Update the far plane here as well
                    if (textRenderer != null)
                        textRenderer.resize(width, height);
                }
            }
        });


        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        GL.createCapabilities();
        glClearColor(0.2f, 0.2f, 0.2f, 1.0f);

        // Enable depth test
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL); // Set the type of depth test
        shaderProgram = ShaderUtils.loadProgram("/shader");
        glUseProgram(shaderProgram);
        locMat = glGetUniformLocation(shaderProgram, "mat");
        locNormalMat = glGetUniformLocation(shaderProgram, "normalMat");
        locLightDir = glGetUniformLocation(shaderProgram, "lightDir");


        cam = cam.withPosition(new Vec3D(0, 0, 50)).withAzimuth(0).withZenith(0);
        textRenderer = new OGLTextRenderer(width, height);
        chunkManager = new ChunkManager(terrainSize, terrainScale, seed);

        initImGui();
        initDiamondSquare();
    }

    private void initImGui() {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        imGuiGlfw.init(window, true);
        imGuiGl3.init("#version 330");
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
            imGuiGlfw.newFrame();
            ImGui.newFrame();

            drawImGui();

            ImGui.render();
            glViewport(0, 0, width, height);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            renderScene();
            imGuiGl3.renderDrawData(ImGui.getDrawData());

            glfwSwapBuffers(window);
        }
    }

    private void drawImGui() {
        ImGuiIO io = ImGui.getIO();
        float windowPosX = io.getDisplaySizeX() - 200;
        float windowPosY = 0;

        ImGui.setNextWindowPos(windowPosX, windowPosY, ImGuiCond.Always);
        ImGui.setNextWindowSize(200, 170, ImGuiCond.Once);

        if (ImGui.begin("Control Panel", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse)) {
            if (ImGui.radioButton("Perlin", selectedMode == 0)) selectedMode = 0;
            if (ImGui.radioButton("Diamond-Square", selectedMode == 1)) selectedMode = 1;

            // Show the following UI elements only if 'Perlin' is selected
            if (selectedMode == 0) {
                // Input field for seed with numeric only restriction
                ImGui.inputInt("Seed", inputInt, 0, 0, ImGuiInputTextFlags.CharsDecimal);

                // "Set" button to apply the seed change
                if (ImGui.button("Set")) {
                    int newSeed = inputInt.get();
                    if (newSeed != seed) {
                        seed = newSeed;
                        chunkManager.setSeed(seed); // Update the seed in Chunk Manager
                        chunkManager.regenerateTerrain(); // Regenerate the terrain
                        System.out.println("Set clicked, new value: " + seed);
                    }
                }

                ImGui.text("Current Seed: " + seed);

                // Terrain scale adjustment using ImFloat
                ImFloat terrainScaleRef = new ImFloat(chunkManager.getTerrainScale());
                if (ImGui.inputFloat("Scale", terrainScaleRef, 0.1f, 1.0f, "%.2f")) {
                    float newScale = terrainScaleRef.get();
                    if (newScale < 0.3f) {
                        newScale = 0.3f; // Set to minimum value if below 0.3
                        terrainScaleRef.set(newScale); // Optionally reset the ImGui display to show this minimum
                    }
                    if (newScale > 100f){
                        newScale = 100f;
                        terrainScaleRef.set(newScale);
                    }
                    chunkManager.setTerrainScale(newScale); // Update the scale in Chunk Manager
                    chunkManager.regenerateTerrain(); // Optional: Regenerate the terrain if you want instant visual feedback
                }
            }

            ImGui.end();
        }
    }




    private void renderScene() {
        glUseProgram(shaderProgram);

        if (wireframe) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        } else {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }

        glUniformMatrix4fv(locMat, false, ToFloatArray.convert(cam.getViewMatrix().mul(proj)));

        switch (selectedMode) {
            case 0:
                renderChunks();
                break;
            case 1:
                renderDiamondSquare();
                break;
        }

        // Update and draw text renderer
        textRenderer.clear();
        textRenderer.addStr2D(20, 20, String.format("Camera Position: X=%.2f, Y=%.2f", cam.getPosition().getX(), cam.getPosition().getY()));
        textRenderer.draw();
    }

    private void renderChunks() {
        int numberOfChunks = 6;
        // Calculate the current central chunk based on the camera's X and Y position
        int currentCentralChunkX = (int) Math.floor(cam.getPosition().getX() / (terrainSize * terrainScale));
        int currentCentralChunkY = (int) Math.floor(cam.getPosition().getY() / (terrainSize * terrainScale));

        // Generate and render grid around the current central chunk along X and Y axes
        for (int dy = -numberOfChunks; dy <= numberOfChunks; dy++) {
            for (int dx = -numberOfChunks; dx <= numberOfChunks; dx++) {
                int chunkX = currentCentralChunkX + dx;
                int chunkY = currentCentralChunkY + dy;
                chunkManager.generateChunk(chunkX, chunkY);
                Chunk chunk = chunkManager.getChunk(chunkX, chunkY);
                if (chunk != null) {
                    // Render the chunk
                    chunk.getBuffers().draw(GL_TRIANGLES, shaderProgram);
                }
            }
        }
    }
    // Declare a DiamondSquare instance and a VAO + VBO for rendering it
    private DiamondSquare diamondSquare;
    private int dsVaoId;
    private int dsVboId;

    private void initDiamondSquare() {
        int iterations = 7; // 129x129 grid
        float roughness = 0.6f;
        diamondSquare = new DiamondSquare(iterations, roughness);
        diamondSquare.generate();

        dsVaoId = glGenVertexArrays();
        dsVboId = glGenBuffers();
        glBindVertexArray(dsVaoId);

        // Convert the height map to a vertex buffer (assuming x, y, z positions for simplicity)
        float[] vertices = new float[(terrainSize + 1) * (terrainSize + 1) * 3];
        int index = 0;
        for (int i = 0; i <= terrainSize; i++) {
            for (int j = 0; j <= terrainSize; j++) {
                vertices[index++] = i * terrainScale; // x
                vertices[index++] = j * terrainScale; // y
                vertices[index++] = diamondSquare.getHeightMap()[i][j] * terrainScale; // z (height)
            }
        }

        // Upload vertex data
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
        verticesBuffer.put(vertices).flip();

        glBindBuffer(GL_ARRAY_BUFFER, dsVboId);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void renderDiamondSquare() {
        glBindVertexArray(dsVaoId);
        glEnableVertexAttribArray(0);

        if (wireframe) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        } else {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }

        glUniformMatrix4fv(locMat, false, ToFloatArray.convert(cam.getViewMatrix().mul(proj)));

        // Draw the terrain
        for (int i = 0; i < terrainSize - 1; i++) {
            glBegin(GL_TRIANGLE_STRIP);
            for (int j = 0; j < terrainSize; j++) {
                // Vertex 1
                glVertex3f(j * terrainScale, i * terrainScale, diamondSquare.getHeightMap()[j][i] * terrainScale);

                // Vertex 2
                glVertex3f(j * terrainScale, (i + 1) * terrainScale, diamondSquare.getHeightMap()[j][i + 1] * terrainScale);
            }
            glEnd();
        }

        glBindVertexArray(0);
    }

    public void run() {
        try {
            init();
            loop();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
