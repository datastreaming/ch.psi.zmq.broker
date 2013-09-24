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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jeromq.ZMQ;

import ch.psi.zmq.broker.model.Routing;

/**
 * Active component that is actually doing the routing of messages.
 * Usually each Router runs on its own thread.
 * @author ebner
 *
 */
public class Router implements Runnable{
	
	private static final Logger logger = Logger.getLogger(Router.class.getName());
	private final static int SOURCE_HIGH_WATER_MARK = 10; 
//	private final static int DESTINATION_HIGH_WATER_MARK = 10; 

	private Routing routing;
	public Router(Routing routing){
		this.routing = routing;
	}
	
	@Override
	public void run() {
		logger.info("Start routing: "+routing.getName());
		ZMQ.Context context = ZMQ.context();
		
		// Bind to destinations, i.e. create sockets.
		final List<ZMQ.Socket> out = new ArrayList<>();
		for(ch.psi.zmq.broker.model.Destination d: routing.getDestinations()){
			ZMQ.Socket outSocket = context.socket(ZMQ.PUSH);
//			outSocket.setHWM(DESTINATION_HIGH_WATER_MARK);
//			outSocket.setRate(100000);
			outSocket.bind(d.getAddress());
			out.add(outSocket);
		}
		
		// Open connection to source
		logger.info("Connect to source: "+routing.getSource().getAddress());
		final ZMQ.Socket in = context.socket(ZMQ.PULL);
		in.setHWM(SOURCE_HIGH_WATER_MARK);
		in.connect(routing.getSource().getAddress());
		
		logger.info("Enter routing loop");
		// Do Routing
		while(!Thread.currentThread().isInterrupted()){
			byte[] message = in.recv();
			for(ZMQ.Socket o: out){
				logger.fine("Message: "+message);
				o.send(message);
			}
		}
		
		// Close connections
		logger.info("Terminate routing");
		in.close();
		for(ZMQ.Socket o: out){
			o.close();
		}
	}

}
