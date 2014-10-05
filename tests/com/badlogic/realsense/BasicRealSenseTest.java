package com.badlogic.realsense;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.realsense.realsenseConstants;

public class BasicRealSenseTest extends ApplicationAdapter {
	@Override
	public void create() {
		Gdx.app.log("BasicRealSenseTest", realsenseConstants.PXC_VERSION_MAJOR + "." + realsenseConstants.PXC_VERSION_MINOR);
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(1, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}
}
