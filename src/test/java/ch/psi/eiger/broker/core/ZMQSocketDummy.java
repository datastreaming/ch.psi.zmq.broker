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

import org.jeromq.ZMQ;

import zmq.Msg;
import zmq.Pipe;
import zmq.SocketBase;

/**
 * This is a workaround because there is no interface for a zmq-socket available.
 * An interface is used to create a mock.
 * 
 * @author meyer_d2
 *
 */
public class ZMQSocketDummy extends ZMQ.Socket {

	protected static String lastMessage;

	protected ZMQSocketDummy() {
		super(new SocketBase(null, 0, 0) {
			
			@Override
			protected void xterminated(Pipe pipe_) {				
			}
			
			@Override
			protected void xattach_pipe(Pipe pipe_, boolean icanhasall_) {				
			}
			
		    @Override
			protected boolean xsend(Msg msg_, int flags_) {
				lastMessage = new String(msg_.data());
		    	return true;
		    }
		});
		lastMessage = null;
	}
}
