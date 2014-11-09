package com.badlogic.gdx.realsense.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.realsense.PXCCapture.Sample;
import com.badlogic.gdx.realsense.PXCCapture.StreamType;
import com.badlogic.gdx.realsense.PXCImage;
import com.badlogic.gdx.realsense.PXCImage.Access;
import com.badlogic.gdx.realsense.PXCImage.ImageData;
import com.badlogic.gdx.realsense.PXCImage.ImageInfo;
import com.badlogic.gdx.realsense.PXCImage.PixelFormat;
import com.badlogic.gdx.realsense.PXCSenseManager;
import com.badlogic.gdx.realsense.pxcStatus;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * A texture that takes its data from one of a camera's
 * streams. Call {@link #updateFromFrame()} to update
 * the texture with the latest image data from the camera.
 * @author Mario
 *
 */
public class StreamTexture extends Texture {	
	private final PXCSenseManager senseManager;
	private final StreamType type;
	private ByteBuffer buffer;
	private final ImageData imageData;
	private int width = 1;
	private int height = 1;
	
	/**
	 * Creates a new StreamTexture that can update itself
	 * via a {@link PXCSenseManager}. Only supports color,
	 * depth and IR streams.
	 * @param senseManager the manager
	 * @param type the type of the stream
	 */
	public StreamTexture(PXCSenseManager senseManager, StreamType type) {
		super(1, 1, Format.RGB888);
		this.senseManager = senseManager;
		this.type = type;
		this.imageData = new ImageData();
	}
	
	private void ensureCapacity(int numBytes) {
		if(buffer == null || buffer.capacity() < numBytes) {
			if(buffer != null) {
				BufferUtils.disposeUnsafeByteBuffer(buffer);
			}
			buffer = BufferUtils.newUnsafeByteBuffer(numBytes);
		}
	}
	
	private PXCImage getImage(Sample sample) {
		if(type == StreamType.STREAM_TYPE_COLOR) {
			return sample.getColor();
		} if(type == StreamType.STREAM_TYPE_DEPTH) {
			return sample.getDepth();
		} if(type == StreamType.STREAM_TYPE_IR) {
			return sample.getIr();
		} else {
			throw new GdxRuntimeException("Unsupported stream type, must be color, depth or ir");
		}
	}
	
	

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	/**
	 * Updates this texture based on the latest data from
	 * {@link PXCSenseManager}.
	 */
	public void updateFromFrame(Sample sample) {
		PXCImage image = null;
		PixelFormat pixelFormat = null;
		int internalFormat = 0;
		int format = 0;
		
		if(type == StreamType.STREAM_TYPE_COLOR) {
			image = sample.getColor();
			pixelFormat = PixelFormat.PIXEL_FORMAT_RGB24;
			internalFormat = GL20.GL_RGB;
			format = /* GL_BGR */ 0x80E0;
		} else if(type == StreamType.STREAM_TYPE_DEPTH) {
			image = sample.getDepth();
			pixelFormat = PixelFormat.PIXEL_FORMAT_RGB32;
			internalFormat = format = /* GL_BGRA */ 0x80E1;
		} else if(type == StreamType.STREAM_TYPE_IR) {
			image = sample.getIr();
			pixelFormat = PixelFormat.PIXEL_FORMAT_RGB24;
			internalFormat = format = /* GL_BGR */ 0x80E0;
		} else {
			throw new GdxRuntimeException("Unknown stream type: " + type);
		}
		
		ImageInfo imageInfo = image.QueryInfo();
		width = imageInfo.getWidth();
		height = imageInfo.getHeight();				
		
		pxcStatus status = image.AcquireAccess(Access.ACCESS_READ, pixelFormat, imageData);
		if(status == pxcStatus.PXC_STATUS_NO_ERROR) {
			ensureCapacity(imageData.getPlanePitch(0) * imageInfo.getHeight());
			imageData.getPlaneData(0, buffer, imageData.getPlanePitch(0) * imageInfo.getHeight());
			
			bind(0);
			Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
			Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, internalFormat, width, height, 0,
					format, GL20.GL_UNSIGNED_BYTE, buffer);
			Gdx.gl.glBindTexture(glTarget, 0);
			image.ReleaseAccess(imageData);
		} else {
			Gdx.app.log("StreamTexture", "couldn't acquire pixels");
		}
	}

	@Override
	public void dispose() {
		if(buffer != null) {
			BufferUtils.disposeUnsafeByteBuffer(buffer);
		}
		super.dispose();
	}
}
