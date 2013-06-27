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

package ch.psi.eiger.broker.core;

import java.util.Hashtable;
import java.util.TreeMap;

import ch.psi.eiger.broker.exception.BrokerConfigurationException;
import ch.psi.eiger.broker.exception.IllegalBrokerOperationException;

/**
 * 
 * 
 * @author meyer_d2
 * 
 */
public interface Broker {

	/**
	 * Returns a copy of the internal map with added forwarders.
	 * 
	 * @return {@link TreeMap}
	 */
	public TreeMap<Integer, Forwarder> getForwarders();

	/**
	 * 
	 */
	public void shutdown();

	/**
	 * Add a forwarder
	 * 
	 * @param forwarder
	 *            {@link Forwarder}
	 * @return A unique id that maps to the added forwarder.
	 */
	public Integer addForwarder(Forwarder forwarder);

	/**
	 * Returns a copy of specified properties.
	 * 
	 * @return {@link Hashtable}
	 */
	public abstract Hashtable<String, String> getProperties();

	/**
	 * 
	 */
	public void start();

	/**
	 * Configures the forwarder with specified properties.
	 * 
	 * @param properties
	 *            Key value pairs
	 * @throws BrokerConfigurationException
	 *             If mandatory parameters are not specified or values are not
	 *             valid.
	 */
	public void configure(Hashtable<String, String> properties) throws BrokerConfigurationException;

	/**
	 * Removes the forwarder specified by address.
	 * 
	 * @param address
	 *            Unique address that identifies a forwarder.
	 * @throws IllegalBrokerOperationException
	 *             If there is no broker with the specified address.
	 */
	public void removeForwarderByAddress(String address) throws IllegalBrokerOperationException;

	/**
	 * Removes the forwarder specified by id.
	 * 
	 * @param fwId
	 *            Forwarder's id.
	 * @throws IllegalBrokerOperationException
	 *             If there is no broker with the specified id.
	 */
	public void removeForwarderById(Integer fwId) throws IllegalBrokerOperationException;
}