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

import ch.psi.eiger.broker.exception.ForwarderConfigurationException;
import ch.psi.eiger.broker.model.ForwarderConfig;

/**
 * Defines the minimum of functionality for a forwarder.
 * 
 * @author meyer_d2
 * 
 */
public interface Forwarder {

	/**
	 * Configures the forwarder with specified properties.
	 * 
	 * @param config
	 *            Forwarder's configuration
	 * @throws ForwarderConfigurationException
	 *             If mandatory parameters are not specified or values are not
	 *             valid.
	 */
	public void configure(ForwarderConfig config) throws ForwarderConfigurationException;

	/**
	 * Starts the forwarder.
	 * 
	 */
	public void start();

	/**
	 * Returns the socket address.
	 * 
	 * @return E.g. tcp://*:5100
	 */
	public String getAddress();

	/**
	 * Forwards bytes to the socket.
	 * 
	 * @param data
	 *            Received bytes.
	 * @param hasReceiveMore
	 *            true if the frame contains of multiple data packages.
	 * @param frameNo
	 *            Internal frame number.
	 */
	public void send(byte[] data, boolean hasReceiveMore, long frameNo);

	/**
	 * Returns a copy of the configuration.
	 * 
	 * @return {@link Hashtable }
	 */
	public ForwarderConfig getConfig();

	/**
	 * Brings the forwarder down in a secure way.
	 */
	public void shutdown();

	/**
	 * @return A unique id.
	 */
	public Integer getId();

	/**
	 * Returns the current mode for frame reduction.
	 * 
	 * @return {@link Mode}
	 */
	public Mode getMode();

	/**
	 * Is used to support frame reduction.
	 * 
	 * @author meyer_d2
	 * 
	 */
	public static enum Mode {
		/**
		 * Default mode, pass any frames through.
		 */
		PassAnyFramesThrough,
		/**
		 * Pass only next frame through.
		 */
		PassNextFrameThrough,
		/**
		 * Ignore received frame.
		 */
		IgnoreFrame;
	}

}