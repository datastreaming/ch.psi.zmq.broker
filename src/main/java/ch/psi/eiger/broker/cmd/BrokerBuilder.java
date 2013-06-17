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

import java.text.MessageFormat;

import org.jeromq.ZMQ;

import ch.psi.eiger.broker.core.Broker;

/**
 * Use this builder class for creating a broker from command line.
 * 
 * @author meyer_d2
 *
 */
public class BrokerBuilder {
	
	private static final String ADDRESS_PATTERN = "(tcp://)[a-z0-9.*]{1,40}(:)[0-9]{4,5}";
	
	/**
	 * This enumeration defines all supported string-based commands for creating a broker. 
	 * 
	 * @author meyer_d2
	 *
	 */
	public enum Command {
		/**
		 * Starting point
		 */
		Start("", "broker"),
		/**
		 * String pattern for creating a broker
		 */
		CreateBroker("create broker", "broker address"),
		/**
		 * String pattern for adding a forwarder
		 */
		AddForwarder("add forwarder", "forwarder address");
		
		private String name;
		
		private String prompt;
		
		private Command(String name, String prompt) {
			this.name = name;
			this.prompt = prompt;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		private String getPrompt() {
			return prompt;
		}
		
		private static Command valueByName(String name) {
			for (Command cmd : Command.values()) {
				if (cmd.name.equals(name)) {
					return cmd;
				}
			}
			throw new IllegalStateException();
		}
	}
	
	private Command currentCommandState = Command.Start;
	
	@SuppressWarnings("javadoc")
	public BrokerBuilder() {
		writePrompt();
	}
	
	/**
	 * Use this method for processing string input from command line.
	 * 
	 * @param in Any string.
	 * @return Itself
	 */
	public BrokerBuilder consume(String in) {
		try {
		switch (currentCommandState) {
		case Start:
			currentCommandState = Command.valueByName(in);
			writePrompt();
			break;
		case CreateBroker:
			if (CMDBroker.bf != null) {
				printOut("Broker is already created!");
				break;
			}
			
			if (in.matches(ADDRESS_PATTERN)) {
				CMDBroker.bf = new Broker(in);
				currentCommandState = Command.Start;
				printOut("Successfully created broker.");
			}
			else {
				printOut("Input string does not match regex " + ADDRESS_PATTERN + ". Example: tcp://localhost:5000");
			}
			break;
		case AddForwarder:
			if (CMDBroker.bf == null) {
				printOut("You must create a broker first!");
				break;
			}
			if (in.matches(ADDRESS_PATTERN)) {
				CMDBroker.bf.forwardTo(in, ZMQ.PUSH, null);
				currentCommandState = Command.Start;
				printOut("Successfully created forwarding to " + in);
			}
			else {
				printOut("Input string does not match regex " + ADDRESS_PATTERN + ". Example: tcp://*:5100");
			}
			break;
		}
		} catch (IllegalStateException e) {
			printOut("Command not supported!");
		}
		return this;
	}
	
	private void printOut(String text) {
		System.out.println(MessageFormat.format("{0}> {1}", currentCommandState.getPrompt(), text));
		writePrompt();
	}

	private void writePrompt() {
		System.out.print(MessageFormat.format("{0}> ", currentCommandState.getPrompt()));
	}
}
