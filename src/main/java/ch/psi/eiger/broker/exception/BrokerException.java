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
 * Base class for any domain specific exceptions.
 * 
 * @author meyer_d2
 * 
 */
public abstract class BrokerException extends Exception {

	private static final long serialVersionUID = 9165579807227752263L;

	@SuppressWarnings("javadoc")
	public BrokerException(String msg, Throwable e) {
		super(msg, e);
	}

	@SuppressWarnings("javadoc")
	public BrokerException(String msg) {
		super(msg);
	}
}
