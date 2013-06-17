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

package ch.psi.eiger.broker.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ch.psi.eiger.broker.core.Broker;

/**
 * Use this class for creating a broker from command line.
 * 
 * @author meyer_d2
 * 
 */
public final class CMDBroker {

	private BrokerBuilder currentState = new BrokerBuilder();

	static Broker bf;

	@SuppressWarnings("javadoc")
	public static void main(String[] args) throws InterruptedException, IOException {
		System.out.println("Command Line Interface");
		new CMDBroker();
	}

	private CMDBroker() throws IOException {
		new Thread(new Runnable() {

			@Override
			public void run() {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String in = null;
				try {
					while (!(in = br.readLine()).equals("q")) {
						currentState = currentState.consume(in);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (bf != null) {
					try {
					bf.shutdown();
					} catch (Exception e) {
					}
				}
			}
		}).start();
	}
}
