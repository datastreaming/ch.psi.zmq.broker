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

/**
 * This class defines the configuration model for a broker.
 * 
 * @author meyer_d2
 * 
 */
public class BrokerConfig {

	private String address;

	private Integer hwm;

	/**
	 * @param address
	 *            Host and port.
	 */
	public BrokerConfig(String address) {
		this.address = address;
	}

	/**
	 * @param address
	 *            Host and port
	 * @param hwm
	 *            High water mark value.
	 */
	public BrokerConfig(String address, Integer hwm) {
		this.address = address;
		this.hwm = hwm;
	}

	/**
	 * @param properties
	 *            Parameter and value map as configuration.
	 * @throws BrokerConfigurationException
	 *             If the configuration is not valid.
	 */
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

	/**
	 * Copy constructor.
	 * 
	 * @param config
	 *            Configuration object that has to be copied.
	 */
	public BrokerConfig(BrokerConfig config) {
		this.address = config.address;
		this.hwm = config.hwm;
	}

	/**
	 * Creates a configuration based on the data transfer object.
	 * 
	 * @param dto
	 *            {@link BrokerDto}
	 */
	public BrokerConfig(BrokerDto dto) {
		address = dto.address;
		hwm = dto.hwm;
	}

	/**
	 * @return Returns host and port.
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @return High water mark.
	 */
	public Integer getHwm() {
		return hwm;
	}

	/**
	 * Sets a new high water mark. Has only an impact as long as the broker is
	 * not started.
	 * 
	 * @param hwm
	 *            High water mark.
	 */
	public void setHwm(Integer hwm) {
		this.hwm = hwm;
	}
}
