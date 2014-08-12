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

package ch.psi.zmq.broker.services;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.media.sse.SseFeature;

import ch.psi.zmq.broker.Broker;
import ch.psi.zmq.broker.model.Configuration;
import ch.psi.zmq.broker.model.Routing;

@Path("")
public class BrokerService {
	
	@Inject
	private Broker broker;
	
	@Inject
	private SseBroadcaster broadcaster;
	
	@GET
	@Path("broker")
	@Produces(MediaType.APPLICATION_JSON)
	public Configuration getConfiguration(){
		return broker.getConfiguration();
	}
	
	@PUT
	@Path("broker")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public void setConfiguration(Configuration configuration){
		broker.setConfiguration(configuration);
		
		// Broadcast new stream list
		OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
		OutboundEvent event = eventBuilder.name("broker")
				            .mediaType(MediaType.APPLICATION_JSON_TYPE)
				            .data(Configuration.class, broker.getConfiguration())
				            .build();
		broadcaster.broadcast(event);			
	}
	
	@DELETE
	@Path("broker")
	public void deleteConfiguration(){
		broker.setConfiguration(new Configuration());
		
		// Broadcast new stream list
		OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
		OutboundEvent event = eventBuilder.name("broker")
				            .mediaType(MediaType.APPLICATION_JSON_TYPE)
				            .data(Configuration.class, broker.getConfiguration())
				            .build();
		broadcaster.broadcast(event);
	}
	
	@PUT
	@Path("broker/{routing-id}")
	public void addRouting(@PathParam("routing-id") String name, Routing routing){
		routing.setName(name); // Ensure that name is the same as the one specified on the URL
		broker.addRouting(routing);
		
		// Broadcast new stream list
		OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
		OutboundEvent event = eventBuilder.name("broker")
		            .mediaType(MediaType.APPLICATION_JSON_TYPE)
		            .data(Configuration.class, broker.getConfiguration())
		            .build();
		broadcaster.broadcast(event);
	}
	
	@GET
	@Path("broker/{routing-id}")
	public Routing getRouting(@PathParam("routing-id") String name){
		for(Routing r: broker.getConfiguration().getRouting()){
			if(r.getName().equals(name)){
				return r;
			}
		}
		return null;
	}
	
	@DELETE
	@Path("broker/{routing-id}")
	public void deleteRouting(@PathParam("routing-id") String name){
		broker.removeRouting("^"+name+"$");
		
		// Broadcast new stream list
		OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
		OutboundEvent event = eventBuilder.name("broker")
				            .mediaType(MediaType.APPLICATION_JSON_TYPE)
				            .data(Configuration.class, broker.getConfiguration())
				            .build();
		broadcaster.broadcast(event);
	}
	
	@GET
    @Path("events")
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput subscribe() {
        EventOutput eventOutput = new EventOutput();
        broadcaster.add(eventOutput);
        return eventOutput;
    }
	
	@GET
    @Path("version")
    @Produces(MediaType.TEXT_PLAIN)
    public String getVersion(){
    	String version = getClass().getPackage().getImplementationVersion();
    	if(version==null){
    		version="0.0.0";
    	}
    	return version;
    }
}
