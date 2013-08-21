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

package ch.psi.eiger.broker.rest.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("javadoc")
@XmlRootElement(name = "Broker")
public class BrokerDto {

	@XmlAttribute
	public Integer id;

	@XmlElement
	public String address;

	@XmlAttribute(name = "highWaterMark")
	public Integer hwm;

	@XmlElementWrapper(name = "Forwarders")
	@XmlElementRef
	public List<ForwarderDto> forwarders = new LinkedList<>();

	public BrokerDto() {
	}

	public BrokerDto(Integer id, String address, Integer hwm) {
		super();
		this.id = id;
		this.address = address;
		this.hwm = hwm;
	}

	@Override
	public String toString() {
		return "BrokerDto [id=" + id + ", address=" + address + ", hwm=" + hwm + ", forwarders=" + Arrays.toString(forwarders.toArray()) + "]";
	}

}
