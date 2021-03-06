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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import ch.psi.zmq.broker.model.Configuration;
import ch.psi.zmq.broker.model.Routing;

public class Broker {
	
	private static final Logger logger = Logger.getLogger(Broker.class.getName());

	private final List<Router> routers = new ArrayList<>();
	
	private ExecutorService eservice;
	
	public Broker(){
		eservice = Executors.newCachedThreadPool();
	}
	
	/**
	 * Configure a routing on the broker
	 * @param routing
	 */
	public void addRouting(Routing routing){
		// Start new routing (thread)
		Router r = new Router(routing);
		eservice.submit(r);
		routers.add(r);
	}
	
	/**
	 * Remove routing from broker if name of the routing matches the specified pattern
	 * @param pattern	regular expression pattern
	 */
	public void removeRouting(String pattern){
		
		List<Router> rr = new ArrayList<>();
		// Find all routings that matches pattern
		for(Router r: routers){
			if(r.getRouting().getName().matches(pattern)){
				rr.add(r);
			}
		}
		// Remove found routings
		for(Router r: rr){
			r.terminate();
			routers.remove(r);
		}
	}
	
	/**
	 * Get current configuration of the broker.
	 * @return
	 */
	public Configuration getConfiguration(){
		List<Routing> r = new ArrayList<>();
		for(Router ro: routers){
			r.add(ro.getRouting());
		}
		Configuration c = new Configuration();
		c.setRouting(r);
		return c;
	}
	
	/**
	 * Set new configuration for broker. Before adding the configured routings, etc. all old will be removed first.
	 * @param configuration	Configuration to load for the broker
	 */
	public void setConfiguration(Configuration configuration){
		// Clean broker
		for(Router r: routers){
			r.terminate();
		}
		routers.clear();
		
		// Setup new configuration
		if(configuration.getRouting()!=null){
			for(Routing r: configuration.getRouting()){
				addRouting(r);
			}
		}
	}
	
	/**
	 * Terminate broker
	 */
	public void terminate(){
		logger.info("Terminate broker");
		
		// Terminate routers
		for(Router r: routers){
			r.terminate();
		}
		
		// Terminate threadpool
		eservice.shutdownNow();
		logger.info("Broker terminated");
	}
}
