package model;

import chunks.Chunk;
import chunks.ChunkManager;
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
    int terrainSize = 126;
    float terrainScale = 5f;
    int width = 800, height = 600;
    double ox, oy;
    private boolean mouseButton1 = false;
    private long window;
    private ChunkManager chunkManager;
    OGLTextRenderer textRenderer;
    int shaderProgram, locMat;
    Camera cam = new Camera();
    Mat4 proj = new Mat4PerspRH(Math.PI / 4, 1, 0.01, 1000.0);

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(width, height, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    case GLFW_KEY_W:
                        cam = cam.forward(3);
                        break;
                    case GLFW_KEY_D:
                        cam = cam.right(1);
                        break;
                    case GLFW_KEY_S:
                        cam = cam.backward(1);
                        break;
                    case GLFW_KEY_A:
                        cam = cam.left(1);
                        break;
                    case GLFW_KEY_LEFT_CONTROL:
                        cam = cam.down(1);
                        break;
                    case GLFW_KEY_LEFT_SHIFT:
                        cam = cam.up(1);
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
                    proj = new Mat4PerspRH(Math.PI / 4, height / (double) width, 0.01, 1000.0);
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

        // Initialize shader program, text renderer, and chunk manager
        shaderProgram = ShaderUtils.loadProgram("/shader");
        glUseProgram(shaderProgram);
        locMat = glGetUniformLocation(shaderProgram, "mat");
        cam = cam.withPosition(new Vec3D(5, 5, 50)).withAzimuth(Math.PI * -2).withZenith(Math.PI * -0.125);
        textRenderer = new OGLTextRenderer(width, height);
        chunkManager = new ChunkManager(terrainSize, terrainScale);
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glViewport(0, 0, width, height);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Update camera matrices
            glUniformMatrix4fv(locMat, false, ToFloatArray.convert(cam.getViewMatrix().mul(proj)));

            // Adjust camera position to ensure the terrain is rendered horizontally
            int centerX = (int) cam.getPosition().getX();
            int centerZ = (int) cam.getPosition().getZ();
            int offsetX = centerX % terrainSize;
            int offsetZ = centerZ % terrainSize;
            int chunkX = centerX / terrainSize;
            int chunkZ = centerZ / terrainSize;

            // Generate and render chunks
            for (int dz = -1; dz <= 1; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int newChunkX = chunkX + dx;
                    int newChunkZ = chunkZ + dz;
                    chunkManager.generateChunk(newChunkX, newChunkZ);
                    Chunk chunk = chunkManager.getChunk(newChunkX, newChunkZ);
                    if (chunk != null) {
                        // Render both front and back faces
                        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                        chunk.getBuffers().draw(GL_TRIANGLES, shaderProgram);
                    }
                }
            }





            // Render text
            textRenderer.clear();
            textRenderer.addStr2D(3, 20, "Renderer: [LMB] camera, WSAD");
            textRenderer.draw();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public void run() {
        try {
            init();
            loop();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
            glDeleteProgram(shaderProgram);
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }
}
