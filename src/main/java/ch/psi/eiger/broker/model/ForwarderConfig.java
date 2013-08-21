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
import ch.psi.eiger.broker.rest.model.BrokerDto;
import ch.psi.eiger.broker.rest.model.ForwarderDto;

/**
 * This class defines the configuration model for a forwarder.
 * 
 * @author meyer_d2
 * 
 */
public class ForwarderConfig {

	private String address;

	private Integer hwm;

	private Long fwTimeInterval;

	@SuppressWarnings("javadoc")
	public ForwarderConfig() {
	}

	/**
	 * @param properties
	 *            Parameter and value map as configuration.
	 * @throws ForwarderConfigurationException
	 *             If the configuration is not valid.
	 */
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

	/**
	 * @param address
	 *            Host and port.
	 */
	public ForwarderConfig(String address) {
		this.address = address;
	}

	/**
	 * @param address
	 *            Host and port
	 * @param hwm
	 *            High water mark value.
	 */
	public ForwarderConfig(String address, Integer hwm) {
		this(address);
		this.hwm = hwm;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param config
	 *            Configuration object that has to be copied.
	 */
	public ForwarderConfig(ForwarderConfig config) {
		address = config.address;
		hwm = config.hwm;
	}

	/**
	 * @param address
	 *            Host and port
	 * @param hwm
	 *            High water mark value.
	 * @param fwTimeInterval
	 *            Frame reduction value e.g. the value 1000 will pass every
	 *            second a frame.
	 */
	public ForwarderConfig(String address, Integer hwm, Long fwTimeInterval) {
		this(address, hwm);
		this.fwTimeInterval = fwTimeInterval;
	}

	/**
	 * Creates a configuration based on the data transfer object.
	 * 
	 * @param dto
	 *            {@link BrokerDto}
	 */
	public ForwarderConfig(ForwarderDto dto) {
		address = dto.address;
		hwm = dto.hwm;
		fwTimeInterval = dto.fwTimeInterval;
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

	/**
	 * Returns the frame reduction value in milliseconds.
	 * 
	 * E.g. a value of 2500 will pass after 2.5 seconds a frame.
	 * 
	 * @return value in milliseconds.
	 */
	public Long getFwTimeInterval() {
		return fwTimeInterval;
	}
}
