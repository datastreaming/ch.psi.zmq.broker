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

package ch.psi.zmq.broker;

import java.util.logging.Logger;

import org.zeromq.ZMQ;

public class Receiver {
	
	private static final Logger logger = Logger.getLogger(Receiver.class.getName());
	
	public static void main(String[] args){
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket socket = context.socket(ZMQ.PULL);
		socket.connect("tcp://localhost:9090");
//		socket.setsockopt(ZMQ.SUBSCRIBE, topicfilter);
		while(true){
			Object message = socket.recv();
			logger.info(""+message);
		}
	}

}
