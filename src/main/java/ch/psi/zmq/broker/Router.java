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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
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

	private ZMQ.Context context;
	private List<ZMQ.Socket> out = new ArrayList<>();
	private ZMQ.Socket in;
	
	private Map<ZMQ.Socket, AtomicBoolean> flag = new HashMap<>();
	private Timer timer = null;
	
	private Routing routing;
	private boolean terminate;
	
	public Router(Routing routing){
		this.routing = routing;
	}
	
	@Override
	public void run() {
		try{
		logger.info("Start routing: "+routing.getName());
		terminate = false;
		context = ZMQ.context();
		zmq.ZError.clear(); // Clear error code
		
		// Bind to destinations, i.e. create sockets.
		for(final ch.psi.zmq.broker.model.Destination d: routing.getDestinations()){
			int type;
			switch (d.getType()) {
			case PUB:
				type = ZMQ.PUB;
				break;

			default:
				type = ZMQ.PUSH;
				break;
			}
			ZMQ.Socket outSocket = context.socket(type);
			outSocket.setHWM(d.getBuffer());
//			outSocket.setRate(100000);
			outSocket.bind(d.getAddress());
			out.add(outSocket);
			
			// Support reduced frequency for sending messages out
			if(d.getFrequency()>0){
				if(timer==null){
					timer = new Timer(); // lazy creation timer
				}
				
				final AtomicBoolean b = new AtomicBoolean();
				flag.put(outSocket, b);
				timer.scheduleAtFixedRate(new TimerTask() {
					@Override
					public void run() {
						b.set(true); // clear flag
					}
				}, 0, d.getFrequency());
			}
		}
		
		// Open connection to source
		logger.info("Connect to source: "+routing.getSource().getAddress());
		int type;
		switch (routing.getSource().getType()) {
		case SUB:
			type = ZMQ.SUB;
			break;

		default:
			type = ZMQ.PULL;
			break;
		}
		in = context.socket(type);
		in.setHWM(routing.getSource().getBuffer());
		in.connect(routing.getSource().getAddress());
		if(routing.getSource().getType().equals(Routing.Type.SUB)){
			in.subscribe(""); // subscribe to all topics
		}
			
		
		boolean receiveMore;
		// Do Routing
		if(timer==null){
			logger.info("Enter routing loop without message reduction");
			while(!Thread.currentThread().isInterrupted()){
				byte[] message = in.recv();
				receiveMore = in.hasReceiveMore();
				for(ZMQ.Socket o: out){
					logger.finest("Message: "+message);
					o.send(message, receiveMore ? ZMQ.SNDMORE : 0);
				}
			}
		}
		else{
			logger.info("Enter routing loop with message reduction");
			List<byte[]> buffer = new ArrayList<>();
			while(!Thread.currentThread().isInterrupted()){
				// We need to take care that all related messages (multipart) are send out
				buffer.add(in.recv()); // buffer all related messages
				while(in.hasReceiveMore()){
					buffer.add(in.recv());
				}
				
				for(ZMQ.Socket o: out){
					
					int c = 1;
					int len = buffer.size();
					
					if((!flag.containsKey(o)) || flag.get(o).getAndSet(false)){
						for(byte[] message: buffer){
							logger.finest("Message: "+message);
							o.send(message, c<len ? ZMQ.SNDMORE : 0);
							c++;
						}
					}
				}
				
				buffer.clear();
			}
		}
			
		// Close connections
		terminate();
		}
		catch(Exception e){
			if(!terminate){
				logger.log(Level.SEVERE, "Routing failed", e);
			}
			// An exception occurs when terminating the router. Ignore this exception
		}
	}
	
	/**
	 * Terminate router
	 */
	public void terminate(){
		logger.info("Terminate routing: "+routing.getName());
		terminate = true;
		if(in!=null){
			in.close();
		}
		for(ZMQ.Socket o: out){
			o.close();
		}
		context.term();
		
		if(timer!=null){
			timer.cancel();
		}
		
		logger.info("Routing terminated");
	}

	public Routing getRouting(){
		return routing;
	}
}
