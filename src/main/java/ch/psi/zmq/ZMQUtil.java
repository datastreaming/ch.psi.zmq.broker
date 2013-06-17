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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.MessageFormat;

import org.jeromq.ZMQ;

public class ZMQUtil {

	public static ZMQ.Socket connect(ZMQ.Context context, int type, String address) {
		ZMQ.Socket in = context.socket(type);
		in.connect(address);
		return in;
	}

	public static ImagePlusWrapper showImage(String title, byte[] bytes) {
		ImagePlusWrapper wrapper = showImage(title);
		wrapper.updateImage(bytes);
		return wrapper;
	}

	public static ImagePlusWrapper showImage(String title) {
		ImagePlusWrapper wrapper = new ImagePlusWrapper(title);
		return wrapper;
	}

	public static class ImagePlusWrapper {

		private ImagePlus img;
		private String title;
		private int numImageUpdates;

		public ImagePlusWrapper(String title) {
			this.title = title;
		}

		public void updateImage(byte[] content) {
			if (content.length != 11059200) {
				return;
			}
			try {
				if (img == null) {
					img = new ImagePlus(generateTitle(), new ShortProcessor(2560, 2160));
					img.show();
				}
				short[] shorts = new short[content.length / 2];
				ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
				img.getProcessor().setPixels(shorts);
				img.updateAndDraw();
				IJ.run(img, "Enhance Contrast", "saturated=0.35");
				img.setTitle(generateTitle());
			} catch (Exception ex) {
			}
		}

		private String generateTitle() {
			return MessageFormat.format("{0}: Frame#: {1}", title, numImageUpdates++);
		}
	}
}