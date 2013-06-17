package ch.psi.eiger.broker.sender;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;

import org.jeromq.ZMQ;
import org.jeromq.ZMQ.Context;
import org.jeromq.ZMQ.Socket;

import ch.psi.zmq.ZMQUtil;
import ch.psi.zmq.ZMQUtil.ImagePlusWrapper;

public class FileSender {

	public static void main(String[] args) {
		FileSender sender = new FileSender();
		sender.send();
	}

	private Context context;
	private Socket socket;

	public void send() {
		new Thread(new Runnable() {
			
			private ImagePlusWrapper img;

			@Override
			public void run() {
				context = ZMQ.context(1);
				socket = context.socket(ZMQ.PUSH);
				socket.bind("tcp://*:" + 5000);

				ByteBuffer buf = ByteBuffer.allocate(11059200);

				img = ZMQUtil.showImage("Detector");
				
				File folder = new File("D://EigerRawFiles");
				File[] files = folder.listFiles();

				int nOFiles = files.length;
				int index = 0;
				while (!Thread.currentThread().isInterrupted()) {
					File file = files[index++];
					index = index % nOFiles;
					socket.sendMore("{\"filename\" : \"" + file.getName() + "\", \"type\":\"raw\"}");
					try (FileInputStream fis = new FileInputStream(file)) {
						FileChannel fileChannel = fis.getChannel();
						int bytes = fileChannel.read(buf);
						buf.flip();
						System.out.println(MessageFormat.format("Send file {0}...", file.getAbsoluteFile()));
						img.updateImage(buf.array());
						socket.send(buf.array());
						buf.clear();
					} catch (IOException e) {
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}

				}
				socket.close();
				context.term();			}
		}).start();
	}
}
