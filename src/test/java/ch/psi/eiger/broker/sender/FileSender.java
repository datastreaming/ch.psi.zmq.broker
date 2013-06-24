package ch.psi.eiger.broker.sender;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jeromq.ZMQ;
import org.jeromq.ZMQ.Context;
import org.jeromq.ZMQ.Socket;

import ch.psi.zmq.ZMQUtil;

public class FileSender {

	private ExecutorService execService = Executors.newCachedThreadPool();

	public static void main(String[] args) {
		FileSender sender = new FileSender();
		sender.init();
	}

	private Context context;
	private static Socket socket;

	public void init() {
		context = ZMQ.context(1);
		socket = ZMQUtil.bind(context, ZMQ.PUSH, "tcp://*:" + 5000, 10);

		File folder = new File("C://EigerRawFiles");
		File[] files = folder.listFiles();

		final byte[][] byteBuffers = new byte[files.length][];

		int i = 0;
		for (File file : files) {
			try (FileInputStream fis = new FileInputStream(file)) {
				FileChannel fileChannel = fis.getChannel();
				ByteBuffer buf = ByteBuffer.allocate(11059200);
				int bytes = fileChannel.read(buf);
				buf.flip();
				byteBuffers[i] = buf.array();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		while (!Thread.currentThread().isInterrupted()) {
			for (byte[] buf : byteBuffers) {
				System.out.println("sending... ");
				socket.sendMore("test");
				socket.send(buf);
			}
		}
	}
}
