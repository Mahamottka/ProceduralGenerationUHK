package model;

import noises.PerlinNoise;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import lwjglutils.ShaderUtils;
import lwjglutils.ToFloatArray;
import transforms.Camera;
import transforms.Mat4;
import transforms.Mat4PerspRH;
import transforms.Vec3D;
import lwjglutils.OGLBuffers;
import lwjglutils.OGLTextRenderer;
import lwjglutils.OGLUtils;
import utils.FastNoiseLite;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Renderer {
    int width = 300, height = 300;
    double ox, oy;
    private boolean mouseButton1 = false;
    private long window;
    OGLBuffers buffers;
    OGLTextRenderer textRenderer;
    int shaderProgram, locMat;
    boolean depthTest = true, cCW = true, renderFront = false, renderBack = false;
    Camera cam = new Camera();
    Mat4 proj = new Mat4PerspRH(Math.PI / 4, 1, 0.01, 1000.0);
    private final int terrainSize = 128;
    private final float terrainScale = 5f;

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
                        cam = cam.forward(1);
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
                    case GLFW_KEY_P:
                        renderFront = !renderFront;
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
        OGLUtils.printOGLparameters();
        glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        createTerrain();
        shaderProgram = ShaderUtils.loadProgram("/shader");
        glUseProgram(this.shaderProgram);
        locMat = glGetUniformLocation(shaderProgram, "mat");
        cam = cam.withPosition(new Vec3D(5, 5, 2.5)).withAzimuth(Math.PI * 1.25).withZenith(Math.PI * -0.125);
        glDisable(GL_CULL_FACE);
        textRenderer = new OGLTextRenderer(width, height);
    }

    void createTerrain() {
        PerlinNoise perlinNoise = new PerlinNoise(terrainSize, terrainSize, terrainScale);
        float[][] noiseData = perlinNoise.getNoiseData();

        float[] vertices = new float[terrainSize * terrainSize * 3];
        int[] indices = new int[(terrainSize - 1) * (terrainSize - 1) * 6];

        float heightMultiplier = 20.0f; // Adjust this value to increase/decrease terrain height

        for (int z = 0; z < terrainSize; z++) {
            for (int x = 0; x < terrainSize; x++) {
                vertices[3 * (z * terrainSize + x)] = x;
                vertices[3 * (z * terrainSize + x) + 1] = z;
                // Apply the noise data with a height multiplier
                vertices[3 * (z * terrainSize + x) + 2] = noiseData[x][z] * heightMultiplier;
            }
        }

        int index = 0;
        for (int z = 0; z < terrainSize - 1; z++) {
            for (int x = 0; x < terrainSize - 1; x++) {
                indices[index++] = z * terrainSize + x;
                indices[index++] = z * terrainSize + x + 1;
                indices[index++] = (z + 1) * terrainSize + x;
                indices[index++] = (z + 1) * terrainSize + x;
                indices[index++] = z * terrainSize + x + 1;
                indices[index++] = (z + 1) * terrainSize + x + 1;
            }
        }

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 3)
        };

        buffers = new OGLBuffers(vertices, attributes, indices);
    }



    private void loop() {
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {

            String text = new String(this.getClass().getName() + ": [LMB] camera, WSAD");

            if (!renderFront) {
                glPolygonMode(GL_FRONT, GL_FILL);
                text += ", front [p]olygons: fill";
            } else {
                glPolygonMode(GL_FRONT, GL_LINE);
                text += ", front [p]olygons: line";
            }


            glViewport(0, 0, width, height);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            // set the current shader to be used
            glUseProgram(shaderProgram);

            glUniformMatrix4fv(locMat, false,
                    ToFloatArray.convert(cam.getViewMatrix().mul(proj)));

            // bind and draw
            buffers.draw(GL_TRIANGLES, shaderProgram);

            textRenderer.clear();
            textRenderer.addStr2D(3, 20, text);
            textRenderer.draw();

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    public void run() {
        try {
            init();

            loop();

            // Free the window callbacks and destroy the window
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            // Terminate GLFW and free the error callback
            glDeleteProgram(shaderProgram);
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }

    }

}