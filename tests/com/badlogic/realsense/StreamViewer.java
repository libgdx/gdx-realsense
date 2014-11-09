package com.badlogic.realsense;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.realsense.PXCCapture.Sample;
import com.badlogic.gdx.realsense.PXCCapture.StreamType;
import com.badlogic.gdx.realsense.PXCSenseManager;
import com.badlogic.gdx.realsense.utils.StreamTexture;

public class StreamViewer extends ApplicationAdapter {
	PXCSenseManager senseManager;
	StreamTexture colorTexture;
	StreamTexture depthTexture;
	StreamTexture irTexture;
	SpriteBatch batch;
	BitmapFont font;
	FPSLogger fpsLogger;


	@Override
	public void create() {		
		// create the sense manager and enable the streams
		senseManager = PXCSenseManager.CreateInstance();
		senseManager.EnableStream(StreamType.STREAM_TYPE_COLOR);
		senseManager.EnableStream(StreamType.STREAM_TYPE_DEPTH);
		senseManager.Init();
		
		// we need a texture to upload the image data to
		// the upload will be done manually without creating
		// an intermediate Pixmap, so we can save a copy. See
		// #render()
		colorTexture = new StreamTexture(senseManager, StreamType.STREAM_TYPE_COLOR);
		depthTexture = new StreamTexture(senseManager, StreamType.STREAM_TYPE_DEPTH);
		
		// we need a batch and a font to display our stuff
		batch = new SpriteBatch();
		font = new BitmapFont();
		fpsLogger = new FPSLogger();
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// acquire a new frame and update the 
		// stream textures accordingly
		senseManager.AcquireFrame();
		Sample sample = senseManager.QuerySample();
		colorTexture.updateFromFrame(sample);
		depthTexture.updateFromFrame(sample);
		senseManager.ReleaseFrame();
		
		// draw the streams, we stretch them to the window size
		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.begin();
		batch.draw(colorTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.draw(depthTexture, 0, 0, 320, 240);
		batch.end();
		
		// log fps
		fpsLogger.log();
	}

	@Override
	public void dispose() {
		senseManager.Release();
	}
	
	public static void main(String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1024;
		config.height = 768;
		new LwjglApplication(new StreamViewer(), config);
	}
}
