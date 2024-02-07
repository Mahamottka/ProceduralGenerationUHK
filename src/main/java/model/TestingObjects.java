package model;

import static global.GlutUtils.glutSolidSphere;
import static org.lwjgl.opengl.GL11.*;

public class TestingObjects {
    public TestingObjects() {

    }

    public void render() {
        glPushMatrix();
        glTranslatef(100, 0, 0);

        glColor3f(1, 0, 0);
        for (int i = 0; i < 10; i++) {
            glTranslatef(-10, 0, 0);
            glutSolidSphere(5, 30, 30);
        }
        glColor3f(0.5f, 0, 0);
        for (int i = 0; i < 10; i++) {
            glTranslatef(-10, 0, 0);
            glutSolidSphere(5, 30, 30);
        }

        glPopMatrix();

        glPushMatrix();
        glTranslatef(0, 100, 0);

        glColor3f(0, 1, 0);
        for (int i = 0; i < 10; i++) {
            glTranslatef(0, -10, 0);
            glutSolidSphere(5, 30, 30);
        }
        glColor3f(0, 0.5f, 0);
        for (int i = 0; i < 10; i++) {
            glTranslatef(0, -10, 0);
            glutSolidSphere(5, 30, 30);
        }
        glPopMatrix();

        glPushMatrix();
        glTranslatef(0, 0, 100);
        glColor3f(0, 0, 1);
        for (int i = 0; i < 10; i++) {
            glTranslatef(0, 0, -10);
            glutSolidSphere(5, 30, 30);
        }
        glColor3f(0, 0, 0.5f);
        for (int i = 0; i < 10; i++) {
            glTranslatef(0, 0, -10);
            glutSolidSphere(5, 30, 30);
        }
        glPopMatrix();

        glTranslatef(0, 0, 0);
        glColor3f(1, 1, 1);
        glutSolidSphere(8, 30, 30);
    }
}
