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

package ch.psi.eiger.broker.core;


import java.util.Dictionary;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.jeromq.ZMQ;
import org.jeromq.ZMQ.Context;
import org.jeromq.ZMQException;

/**
 * Use this class for forwarding bytes to a specific socket.
 * 
 * @author meyer_d2
 *
 */
public class Forwarder implements Runnable {
	
//	private final Logger log;

    private final ZMQ.Socket outSocket;
	
	private final BlockingQueue<byte[]> queue;
    
    /**
     * @param context {@link Context}
     * @param type {@link ZMQ}
     * @param address e.g. "tcp://*:5100"
     * @param properties Additional configuration.
     */
    public Forwarder(Context context, int type, String address, Dictionary<String, String> properties) {		
        queue = new ArrayBlockingQueue<>(5);

		outSocket = context.socket(type);
		outSocket.bind(address);
//		log = LoggerFactory.getLogger(MessageFormat.format("{0}[{1}:{2}]", Forwarder.class.getName(), type, address));
	}

	/**
	 * Forward passed bytes to the out socket.
	 * 
	 * @param bytes Data
	 */
	public void forward(byte[] bytes) {
    	try {
    		queue.add(bytes);
    	} catch (IllegalStateException e) {
//    		log.warn(MessageFormat.format("Queue is full with {0} elements.", queue.size()));
    	}
    }
   
  
    @Override
    public void run() {
//    	log.info("Started forwarder.");

        while (!Thread.currentThread().isInterrupted()) {
            try {
            	byte[] bytes = queue.take();
//            	log.debug("sending ...");
                outSocket.send(bytes);
//            	log.debug("sent.");
            } catch (ZMQException e) {
//            	log.error("e");
                if (ZMQ.Error.ETERM.getCode() == e.getErrorCode()) {
                    break;
                }
                throw e;
            } 
            catch (InterruptedException e) {
//				log.error("", e);
			}
        }
//        outSocket.close();
    }

	/**
	 * Stops the forwarder.
	 */
	public void shutdown() {
		outSocket.close();
	}
}
