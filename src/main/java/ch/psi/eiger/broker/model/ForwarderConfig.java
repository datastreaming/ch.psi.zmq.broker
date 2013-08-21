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

package ch.psi.eiger.broker.model;

import java.util.Hashtable;
import java.util.Objects;

import ch.psi.eiger.broker.exception.ForwarderConfigurationException;
import ch.psi.eiger.broker.rest.model.ForwarderDto;

public class ForwarderConfig implements Cloneable {

	private String address;

	private Integer hwm;

	private Long fwTimeInterval;

	public ForwarderConfig() {
	}

	public ForwarderConfig(Hashtable<String, String> properties) throws ForwarderConfigurationException {
		try {
			Objects.requireNonNull(properties, "Configuration cannot be null.");
			Objects.requireNonNull(properties.get("address"), "Parameter \"address\" could not be found.");
		} catch (NullPointerException e) {
			throw new ForwarderConfigurationException(e.getMessage(), e);
		}

		address = properties.get("address");

		if (properties.containsKey("hwm")) {
			try {
				hwm = Integer.parseInt(properties.get("hwm"));
			} catch (NumberFormatException e) {
				throw new ForwarderConfigurationException("The value of high water mark must be an integer.", e);
			}
		}

		if (properties.containsKey("fwTimeInterval")) {
			try {
				fwTimeInterval = Long.parseLong(properties.get("fwTimeInterval"));
			} catch (NumberFormatException e) {
				throw new ForwarderConfigurationException("The value for the forwarding interval must be a long.", e);
			}
		}
	}

	public ForwarderConfig(String address) {
		this.address = address;
	}

	public ForwarderConfig(String address, Integer hwm) {
		this(address);
		this.hwm = hwm;
	}

	public ForwarderConfig(ForwarderConfig config) {
		address = config.address;
		hwm = config.hwm;
	}

	public ForwarderConfig(String address, Integer hwm, Long fwTimeInterval) {
		this(address, hwm);
		this.fwTimeInterval = fwTimeInterval;
	}

	public ForwarderConfig(ForwarderDto dto) {
		address = dto.address;
		hwm = dto.hwm;
		fwTimeInterval = dto.fwTimeInterval;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Integer getHwm() {
		return hwm;
	}

	public void setHwm(Integer hwm) {
		this.hwm = hwm;
	}

	public Long getFwTimeInterval() {
		return fwTimeInterval;
	}

	public void setFwTimeInterval(Long fwTimeInterval) {
		this.fwTimeInterval = fwTimeInterval;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
