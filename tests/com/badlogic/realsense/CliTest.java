package com.badlogic.realsense;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.realsense.PXCCapture.Sample;
import com.badlogic.gdx.realsense.PXCCapture.StreamType;
import com.badlogic.gdx.realsense.PXCImage.Access;
import com.badlogic.gdx.realsense.PXCImage.ImageData;
import com.badlogic.gdx.realsense.PXCImage.ImageInfo;
import com.badlogic.gdx.realsense.PXCImage.PixelFormat;
import com.badlogic.gdx.realsense.PXCSenseManager;
import com.badlogic.gdx.realsense.PXCSession;
import com.badlogic.gdx.realsense.PXCSession.ImplVersion;
import com.badlogic.gdx.realsense.pxcStatus;
import com.badlogic.gdx.utils.GdxNativesLoader;

public class CliTest {
	public static void main(String[] args) {
		GdxNativesLoader.load();
		
		PXCSession session = PXCSession.CreateInstance();
		ImplVersion version = session.QueryVersion();
		System.out.println(version.getMajor() + "." + version.getMinor());
		session.Release();
		
		PXCSenseManager senseManager = PXCSenseManager.CreateInstance();
		System.out.println(senseManager.EnableStream(StreamType.STREAM_TYPE_COLOR));
		System.out.println(senseManager.EnableStream(StreamType.STREAM_TYPE_DEPTH));
		System.out.println(senseManager.Init());
		
		// we don't want to allocate the ImageData struct every frame
		ImageData data = new ImageData();
		// we need a buffer to fetch the image data into
		ByteBuffer buffer = ByteBuffer.allocateDirect(10 * 1024 * 1024);
		buffer.order(ByteOrder.nativeOrder());
		while(true) {
			senseManager.AcquireFrame();
			Sample sample = senseManager.QuerySample();
			ImageInfo colorInfo = sample.getColor().QueryInfo();			
			System.out.println("Got color image: " + colorInfo.getWidth() + "x" + colorInfo.getHeight());
			sample.getColor().AcquireAccess(Access.ACCESS_READ,PixelFormat.PIXEL_FORMAT_RGB32, data);
			data.getPlaneData(0, buffer, data.getPlanePitch(0) * colorInfo.getHeight());		
			Pixmap pixmap = new Pixmap(colorInfo.getWidth(), colorInfo.getHeight(), Pixmap.Format.RGBA8888);
			ByteBuffer otherBuffer = pixmap.getPixels();
			for(int i = 0; i < data.getPlanePitch(0) * colorInfo.getHeight(); i++) {
				otherBuffer.put(i, buffer.get(i));
			}
			PixmapIO.writePNG(new FileHandle("color.png"), pixmap);
			pixmap.dispose();
			
			ImageInfo depthInfo = sample.getDepth().QueryInfo();
			System.out.println("Got depth image: " + depthInfo.getWidth() + "x" + depthInfo.getHeight());
			sample.getDepth().AcquireAccess(Access.ACCESS_READ,PixelFormat.PIXEL_FORMAT_RGB32, data);
			data.getPlaneData(0, buffer, data.getPlanePitch(0) * depthInfo.getHeight());		
			pixmap = new Pixmap(depthInfo.getWidth(), depthInfo.getHeight(), Pixmap.Format.RGBA8888);
			otherBuffer = pixmap.getPixels();
			for(int i = 0; i < data.getPlanePitch(0) * depthInfo.getHeight(); i++) {
				otherBuffer.put(i, buffer.get(i));
			}
			PixmapIO.writePNG(new FileHandle("depth.png"), pixmap);
			pixmap.dispose();
			senseManager.ReleaseFrame();
		}
		// senseManager.Release();
	}
}
