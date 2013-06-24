package ch.psi.eiger.broker.consumer;
import java.io.IOException;

import org.jeromq.ZMQ;

import ch.psi.zmq.ZMQUtil;
import ch.psi.zmq.ZMQUtil.ImagePlusWrapper;

public class Consumer {
	
	public static void main(String[] args) throws IOException {
//		Objects.requireNonNull(args, "Please specify the address for listening...");
		Consumer c = new Consumer();
		c.setupConnection(args);
	}
	
	private void setupConnection(final String[] args) throws IOException {
		Thread t = new Thread(new Runnable() {
			
			private ImagePlusWrapper img;

			@Override
			public void run() {
				ZMQ.Context context = ZMQ.context(1);
				ZMQ.Socket in = ZMQUtil.connect(context, ZMQ.PULL, "tcp://localhost:5100", 1);
				
				img = ZMQUtil.showImage("Consumer 1");

				while (!Thread.currentThread().isInterrupted()) {
					byte[] content = in.recv();
//					img.updateImage(content);
				}

				in.close();
				context.term();
				}
		});
		t.start();
	}
}
