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

package ch.psi.zmq.broker.model;

import javax.xml.bind.annotation.XmlAttribute;

import ch.psi.zmq.broker.model.Routing.Type;


public class Destination {
	
	/**
	 * Address of the source in following format
	 * tcp://&gt;ip>:&gt;port>
	 */
	private String address;

	/**
	 * Type of connection. Default type is PUSH
	 */
	private Type type = Type.PUSH;
	
	/**
	 * Number of messages that can be buffered on the sending side
	 */
	private int buffer = 5;
	
	/**
	 * Update frequency in milliseconds
	 */
	private long frequency = 0;
	
	@XmlAttribute
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	@XmlAttribute
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	@XmlAttribute
	public int getBuffer() {
		return buffer;
	}
	public void setBuffer(int buffer) {
		this.buffer = buffer;
	}
	@XmlAttribute
	public long getFrequency() {
		return frequency;
	}
	public void setFrequency(long frequency) {
		this.frequency = frequency;
	}
	
}
