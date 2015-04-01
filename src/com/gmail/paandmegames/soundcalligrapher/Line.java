package com.gmail.paandmegames.soundcalligrapher;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;


public class Line {


	
	private int mPositionHandle;
	private int mColorHandle;
	private static final int COORDS_PER_VERTEX = 3;
	private final int vertexCount;
	private final int vertexStride;

	private float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
	private FloatBuffer vertexBuffer;
	
	public Line(int x0, int y0, int x, int y) {
		
		float lineCoords[] = {x0, y0, 0.0f, x, y, 0.0f};	
		// (number of coordinate values * 4 bytes per float)
		ByteBuffer bb = ByteBuffer.allocateDirect(lineCoords.length * 4);
		// use the device hardware's native byte order
		bb.order(ByteOrder.nativeOrder());
		// create a floating point buffer from the ByteBuffer
		vertexBuffer = bb.asFloatBuffer();
		// add the coordinates to the FloatBuffer
		vertexBuffer.put(lineCoords);
		// set the buffer to read the first coordinate
		vertexBuffer.position(0);
		
		vertexCount = lineCoords.length / COORDS_PER_VERTEX;
		vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

	}


public void draw(int program) {
    // Add program to OpenGL ES environment
    GLES20.glUseProgram(program);

    // get handle to vertex shader's vPosition member
    mPositionHandle = GLES20.glGetAttribLocation(program, "vPosition");

    // Enable a handle to the line vertices
    GLES20.glEnableVertexAttribArray(mPositionHandle);

    // Prepare the triangle coordinate data
    GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                                 GLES20.GL_FLOAT, false,
                                 vertexStride, vertexBuffer);

    // get handle to fragment shader's vColor member
    mColorHandle = GLES20.glGetUniformLocation(program, "vColor");

    // Set color for drawing the line
    GLES20.glUniform4fv(mColorHandle, 1, color, 0);

    // Draw the line
    GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount);

    // Disable vertex array
    GLES20.glDisableVertexAttribArray(mPositionHandle);
}


}