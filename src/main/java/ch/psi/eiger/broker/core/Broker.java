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

import java.util.Collections;
import java.util.Dictionary;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jeromq.ZMQ;
import org.jeromq.ZMQ.Context;
import org.jeromq.ZMQ.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.zmq.ZMQUtil;

/**
 * This broker class allows registering forwarders they notified by any
 * activities on the specified input socket.
 * 
 * @author meyer_d2
 * 
 */
public class Broker {

	private static final Logger log = LoggerFactory.getLogger(Broker.class);
	private ExecutorService es = Executors.newFixedThreadPool(8);
	private Context context;
	private Socket in;

	private Set<Forwarder> forwarders;

	/**
	 * @param address
	 *            e.g "tcp://localhost:5000"
	 */
	public Broker(final String address) {
		forwarders = Collections.newSetFromMap(new ConcurrentHashMap<Forwarder, Boolean>());

		context = ZMQ.context(1);
		in = ZMQUtil.connect(context, ZMQ.PULL, address, 4);

		es.submit(new Runnable() {
			@Override
			public void run() {

				while (!Thread.currentThread().isInterrupted()) {
					byte[] data = in.recv();					
					byte[] content = null;
					boolean hasMore = in.hasReceiveMore();
					for (Forwarder fw : forwarders) {
						fw.send(data, hasMore);
					}
				
					while(hasMore) {
						content = in.recv();
						for (Forwarder fw : forwarders) {
							fw.send(content, (hasMore = in.hasReceiveMore()));
						}
					}
				}
			}
		});
	}

	/**
	 * Adds a forwarder for the specified address and type.
	 * 
	 * @param address
	 *            e.g. "tcp://*:5100"
	 * @param type
	 *            {@link ZMQ}
	 * @param properties
	 *            Additional configuration.
	 */
	public void forwardTo(String address, int type, Dictionary<String, String> properties) {
		//TODO create threads...
		Forwarder fw = new Forwarder(context, type, address, properties);
		forwarders.add(fw);
	}
	
	public Set<Forwarder> getForwarders() {
		return forwarders;
	}

	/**
	 * Stops the broker and all forwarders.
	 */
	public void shutdown() {
		for (Forwarder fw : forwarders) {
			fw.shutdown();
		}

		in.close();
		context.term();
		es.shutdownNow();
		forwarders.clear();
	}
}