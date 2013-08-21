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

package ch.psi.eiger.broker.exception;

/**
 * Use this exception if a parameter is invalid.
 * 
 * @author meyer_d2
 * 
 */
public class BrokerConfigurationException extends BrokerException {

	private static final long serialVersionUID = 8042398553133273188L;

	@SuppressWarnings("javadoc")
	public BrokerConfigurationException(String msg, Throwable e) {
		super(msg, e);
	}

	@SuppressWarnings("javadoc")
	public BrokerConfigurationException(String msg) {
		super(msg);
	}

}
