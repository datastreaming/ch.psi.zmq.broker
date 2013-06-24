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

import java.text.MessageFormat;
import java.util.Dictionary;

import org.jeromq.ZMQ;
import org.jeromq.ZMQ.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.zmq.ZMQUtil;

/**
 * Use this class for forwarding bytes to a specific socket.
 * 
 * @author meyer_d2
 * 
 */
public class Forwarder {

	private final Logger log;

	private final ZMQ.Socket outSocket;

	private String address;

	/**
	 * @param context
	 *            {@link Context}
	 * @param type
	 *            {@link ZMQ}
	 * @param address
	 *            e.g. "tcp://*:5100"
	 * @param properties
	 *            Additional configuration.
	 */
	public Forwarder(Context context, int type, String address, Dictionary<String, String> properties) {
		this.address = address;
		outSocket = ZMQUtil.bind(context, type, address, 4);
		log = LoggerFactory.getLogger(MessageFormat.format("{0}[{1}:{2}]", Forwarder.class.getName(), type, address));
	}
	
	/**
	 * Stops the forwarder.
	 */
	public void shutdown() {
		outSocket.close();
	}

	public String getAddress() {
		return address;
	}

	public void send(byte[] data, boolean hasReceiveMore) {
		outSocket.send(data, hasReceiveMore ? ZMQ.SNDMORE : 0);
	}
}