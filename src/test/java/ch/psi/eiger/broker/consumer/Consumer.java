package ch.psi.eiger.broker.consumer;
import java.io.IOException;

import org.jeromq.ZMQ;

import ch.psi.zmq.ZMQUtil;
import ch.psi.zmq.ZMQUtil.ImagePlusWrapper;

public class Consumer {
	
	public static void main(String[] args) throws IOException {
		Consumer c = new Consumer();
		c.setupConnection();
	}
	
	private void setupConnection() throws IOException {
		Thread t = new Thread(new Runnable() {
			
			private ImagePlusWrapper img;

			@Override
			public void run() {
				ZMQ.Context context = ZMQ.context(1);
				ZMQ.Socket in = ZMQUtil.connect(context, ZMQ.PULL, "tcp://localhost:5200");
				
				img = ZMQUtil.showImage("Consumer 1");

				while (!Thread.currentThread().isInterrupted()) {
					byte[] content = in.recv();
					img.updateImage(content);
				}

				in.close();
				context.term();
				}
		});
		t.start();
	}
}
