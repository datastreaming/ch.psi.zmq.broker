/**
 * 
 * Copyright 2013 Paul Scherrer Institute. All rights reserved.
 * 
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This code is distributed in the hope that it will be useful, but without any
 * warranty; without even the implied warranty of merchantability or fitness for
 * a particular purpose. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package ch.psi.zmq;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ShortProcessor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeromq.ZMQ;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("javadoc")
public class ZMQUtil {

	public static ZMQ.Socket connect(ZMQ.Context context, int type, String address, int highWaterMark) {
		ZMQ.Socket in = context.socket(type);
		in.setHWM(highWaterMark);
		in.connect(address);
		return in;
	}

	public static ZMQ.Socket bind(ZMQ.Context context, int type, String address, int highWaterMark) {
		ZMQ.Socket outSocket = context.socket(type);
		outSocket.setHWM(highWaterMark);
		outSocket.setRate(100000);
		outSocket.bind(address);
		return outSocket;
	}

	@Deprecated
	public static ImagePlusWrapper showImage(String title) {
		ImagePlusWrapper wrapper = new ImagePlusWrapper(title);
		return wrapper;
	}

	@Deprecated
	// FIXME the protocol has been changed.
	public static class ImagePlusWrapper {

		private ImagePlus img;
		private String title;
		private int numImageUpdates;
		private ObjectMapper mapper = new ObjectMapper(new JsonFactory());
		private int imageSizeY;
		private int imageSizeX;

		public ImagePlusWrapper(String title) {
			this.title = title;
		}

		@SuppressWarnings("unchecked")
		private void readHeader(byte[] h) {
			try {
				String header = new String(h);
				// hinfo.setHeader(header);
				Map<String, Object> m = mapper.readValue(header, new TypeReference<HashMap<String, Object>>() {
				});
				if (((List<String>) m.get("htype")).contains("array-1.0")) { // currently
																				// we
																				// only
																				// support
																				// array-1.0
																				// message
																				// types
					List<Integer> shape = (List<Integer>) m.get("shape");
					int nImageSizeX = shape.get(1);
					int nImageSizeY = shape.get(0);
					if (imageSizeX != nImageSizeX || imageSizeY != nImageSizeY) {
						imageSizeX = nImageSizeX;
						imageSizeY = nImageSizeY;
						if (img != null) {
							img.close();
							img = null;
						}
					}

					if (img == null) {
						// TODO eventually use ByteProcessor or BinaryProcessor
						// BinaryProcessor p = new
						// ij.process.BinaryProcessor(new
						// ByteProcessor(imageSizeX, imageSizeY));
						img = new ImagePlus("", new ShortProcessor(imageSizeX, imageSizeY));
						img.show();
					}
					img.setTitle(header);
				} else {
					System.err.println("Header type is not supported ...");
					if (img != null) {
						img.close();
						img = null;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void updateImage(byte[] header, byte[] content) {
			readHeader(header);
			try {
				if (content != null && img != null) {
					// TODO Check whether this is needed
					short[] shorts = new short[content.length / 2];
					ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
					img.getProcessor().setPixels(shorts);
					IJ.run(img, "Enhance Contrast", "saturated=0.35");
					img.updateAndDraw();
					numImageUpdates++;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}