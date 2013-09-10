package ch.psi.eiger.broker.sender;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jeromq.ZMQ;
import org.jeromq.ZMQ.Context;
import org.jeromq.ZMQ.Socket;

import ch.psi.zmq.ZMQUtil;

@SuppressWarnings("javadoc")
public class FileSender {
	
	//{"htype":["array-1.0"],"tag":"","source":"","shape":[456,616] ,"frame":9983,"type":"uint16","endianess":"little"}

	public static void main(String[] args) {
		FileSender sender = new FileSender();
		sender.init();
	}

	private Context context;
	private static Socket socket;

	public void init() {
		context = ZMQ.context(1);
		socket = ZMQUtil.bind(context, ZMQ.PUSH, "tcp://*:" + 8080, 10);

		File folder = new File("C://EigerRawFiles");
		File[] files = folder.listFiles();

		final byte[][] byteBuffers = new byte[files.length][];

		int i = 0;
		for (File file : files) {
			try (FileInputStream fis = new FileInputStream(file)) {
				ByteBuffer buf = ByteBuffer.allocate(11059200);
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

		int frame = 1;
		while (!Thread.currentThread().isInterrupted()) {
			for (byte[] buf : byteBuffers) {
				System.out.println("sending... ");
				socket.sendMore("{\"htype\":[\"array-1.0\"],\"tag\":\"\",\"source\":\"\",\"shape\":[456,616] ,\"frame\":" + frame++ + ",\"type\":\"uint16\",\"endianess\":\"little\"}");
				socket.send(buf);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
