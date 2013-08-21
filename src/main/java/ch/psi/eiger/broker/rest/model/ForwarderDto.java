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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("javadoc")
@XmlRootElement(name = "Forwarder")
public class ForwarderDto {

	@XmlAttribute
	public Integer id;

	@XmlElement(name = "Address")
	public String address;

	@XmlAttribute(name = "highWaterMark")
	public Integer hwm;

	@XmlAttribute(name = "forwardTimeInterval")
	public Long fwTimeInterval;

	@XmlAttribute(name = "currentForwardMode")
	public String mode;

	public ForwarderDto() {
	}

	public ForwarderDto(Integer id, String address, Integer hwm, Long fwTimeInterval, String mode) {
		super();
		this.id = id;
		this.address = address;
		this.hwm = hwm;
		this.fwTimeInterval = fwTimeInterval;
		this.mode = mode;
	}

	@Override
	public String toString() {
		return "ForwarderDto [id=" + id + ", address=" + address + ", hwm=" + hwm + ", fwTimeInterval=" + fwTimeInterval + ", mode=" + mode + "]";
	}
}
