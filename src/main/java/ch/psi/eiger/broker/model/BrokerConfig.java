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

import ch.psi.eiger.broker.exception.BrokerConfigurationException;
import ch.psi.eiger.broker.rest.model.BrokerDto;

public class BrokerConfig {

	private String address;

	private Integer hwm;

	public BrokerConfig(String address) {
		this.address = address;
	}

	public BrokerConfig(String address, Integer hwm) {
		this.address = address;
		this.hwm = hwm;
	}

	public BrokerConfig(Hashtable<String, String> properties) throws BrokerConfigurationException {
		try {
			Objects.requireNonNull(properties, "Configuration cannot be null.");
			Objects.requireNonNull(properties.get("address"), "Parameter \"address\" could not be found.");
		} catch (NullPointerException e) {
			throw new BrokerConfigurationException(e.getMessage(), e);
		}

		address = properties.get("address");

		if (properties.get("hwm") != null) {
			try {
				hwm = Integer.parseInt(properties.get("hwm"));
			} catch (NumberFormatException e) {
				throw new BrokerConfigurationException("The value of high water mark must be an integer.", e);
			}
		}
	}

	public BrokerConfig(BrokerConfig config) {
		this.address = config.address;
		this.hwm = config.hwm;
	}

	public BrokerConfig(BrokerDto dto) {
		address = dto.address;
		hwm = dto.hwm;
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
}
