package com.badlogic.realsense;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.realsense.PXCCapture.Sample;
import com.badlogic.gdx.realsense.PXCCapture.StreamType;
import com.badlogic.gdx.realsense.PXCHandConfiguration;
import com.badlogic.gdx.realsense.PXCHandData;
import com.badlogic.gdx.realsense.PXCHandData.AccessOrderType;
import com.badlogic.gdx.realsense.PXCHandData.AlertType;
import com.badlogic.gdx.realsense.PXCHandData.IHand;
import com.badlogic.gdx.realsense.PXCHandData.JointData;
import com.badlogic.gdx.realsense.PXCHandData.JointType;
import com.badlogic.gdx.realsense.PXCHandModule;
import com.badlogic.gdx.realsense.PXCSenseManager;
import com.badlogic.gdx.realsense.pxcStatus;
import com.badlogic.gdx.realsense.utils.StreamTexture;

public class HandTracking extends ApplicationAdapter {
	PXCSenseManager senseManager;
	PXCHandModule handModule;
	PXCHandConfiguration handConfig;
	PXCHandData handData;
	StreamTexture depthTexture;
	ShapeRenderer renderer;
	SpriteBatch batch;
	BitmapFont font;
	FPSLogger fpsLogger;
	Map<Integer, String> handToGesture = new HashMap<Integer, String>();

	@Override
	public void create() {		
		// create the sense manager and enable the streams,
		// get the hand module, and initialize the pipeline
		senseManager = PXCSenseManager.CreateInstance();
		senseManager.EnableStream(StreamType.STREAM_TYPE_DEPTH);
		senseManager.EnableHand();
		handModule = senseManager.QueryHand();	
		senseManager.Init();
		
		// configure the hand module, we want all gestures
		// we also want a PXCHandData instance which we'll
		// later query for gesture data. We also enable
		// alerts if a hand is detected or lost
		handData = handModule.CreateOutput();
		handConfig = handModule.CreateActiveConfiguration();
		handConfig.EnableAllGestures();
		handConfig.EnableAlert(AlertType.ALERT_HAND_DETECTED);
		handConfig.EnableAlert(AlertType.ALERT_HAND_NOT_DETECTED);
		handConfig.ApplyChanges();
		
		// we need a texture to upload the image data to
		// the upload will be done manually without creating
		// an intermediate Pixmap, so we can save a copy. See
		// #render()
		depthTexture = new StreamTexture(senseManager, StreamType.STREAM_TYPE_DEPTH);
		
		// we need a batch and a font to display our stuff
		renderer = new ShapeRenderer();
		batch = new SpriteBatch();
		font = new BitmapFont();
		fpsLogger = new FPSLogger();
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// acquire a new frame. update the 
		// stream textures and get hand tracking
		// data so we can draw it
		pxcStatus status = senseManager.AcquireFrame();
		if(status == pxcStatus.PXC_STATUS_NO_ERROR) {
			Sample sample = senseManager.QuerySample();
			depthTexture.updateFromFrame(sample);
			handData.Update();				
			senseManager.ReleaseFrame();
		}
		
		// draw the streams, we stretch them to the window size
		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.begin();
		batch.draw(depthTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
		
		// draw a few basic hand tracking infos
		batch.begin();
		font.draw(batch, "#Hands: " + handData.QueryNumberOfHands(), 0, Gdx.graphics.getHeight() - 16);
		StringBuilder builder = new StringBuilder();
		for(Integer hand: handToGesture.keySet()) {
			builder.append("hand #" + hand + "\n");
			builder.append("   gesture: " + handToGesture.get(hand) + "\n");
		}
		font.drawMultiLine(batch, builder.toString(), 0, Gdx.graphics.getHeight() - 32);
		batch.end();
		
		// draw the skeletons of all detected hands.
		// we get the coordinates in image space, which
		// corresponds to the depth cameras 640x480 coordinate
		// system. If we wanted to overlay this on the color
		// stream, we'd have to project the coordinates from
		// the depth image space to the color image space. See
		// the RealSense SDK documentation on coordinate spaces
		// and projections.
		renderer.getProjectionMatrix().setToOrtho2D(0, 0, 640, 480);
		int numHands = handData.QueryNumberOfHands();
		for(int i = 0; i < numHands; i++) {
			IHand hand = handData.QueryHandData(AccessOrderType.ACCESS_ORDER_BY_TIME, i);
			
			// we gather all joints' positions and then send them of to 
			// drawSkeleton which knows how joints are connected
			Vector2[] joints = new Vector2[PXCHandData.NUMBER_OF_JOINTS];
			for(int j = 0; j < PXCHandData.NUMBER_OF_JOINTS; j++) {
				JointData jointData = new JointData();
				hand.QueryTrackedJoint(PXCHandData.JointType.swigToEnum(j), jointData);
				joints[j] = new Vector2(jointData.getPositionImage().getX(), jointData.getPositionImage().getY());
			}
			drawSkeleton(joints);
		}
		
		// Now check all the fired gestures, we set the lastGesture
		// string accordingly
		for(int i = 0; i < handData.QueryFiredGesturesNumber(); i++) {
			PXCHandData.GestureData gestureData = new PXCHandData.GestureData();
			handData.QueryFiredGestureData(i, gestureData);
			handToGesture.put(gestureData.getHandId(), gestureData.GetCName());
		}
		
		// Check alerts, we need to remove any hands that are no
		// long detected.
		for(int i = 0; i < handData.QueryFiredAlertsNumber(); i++) {
			PXCHandData.AlertData alertData = new PXCHandData.AlertData();
			handData.QueryFiredAlertData(i, alertData);
			if(alertData.getLabel() == AlertType.ALERT_HAND_DETECTED) {
				Gdx.app.log("HandTracking", "Detected hand");
			} else if(alertData.getLabel() == AlertType.ALERT_HAND_NOT_DETECTED) {
				Gdx.app.log("HandTracking", "Hand lost");
				handToGesture.remove(alertData.getHandId());
			}
		}
		
		// log fps
		fpsLogger.log();
	}
	
	/**
	 * Draws a set of joint positions. The index of a joint
	 * corresponds to its {@link JointType} value. See the
	 * RealSense documentation on joints and how they are connected
	 * @param joints
	 */
	private void drawSkeleton(Vector2[] joints) {
		renderer.begin(ShapeType.Filled);
		renderer.setColor(Color.GREEN);
		for(Vector2 joint: joints) {
			renderer.circle(joint.x, 480 - joint.y, 2);
		}
		renderer.end();
	}

	@Override
	public void dispose() {
		senseManager.Release();
	}
	
	public static void main(String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1024;
		config.height = 768;
		new LwjglApplication(new HandTracking(), config);
	}
}
