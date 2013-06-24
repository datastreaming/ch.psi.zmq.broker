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

import org.jeromq.ZMQ;

import ch.psi.eiger.broker.core.Broker;
import ch.psi.eiger.broker.core.Forwarder;

/**
 * Use this builder class for creating a broker from command line.
 * 
 * @author meyer_d2
 * 
 */
public final class BrokerEngine {

	private static final String ADDRESS_PATTERN = "(tcp://)[a-z0-9.*]{1,40}(:)[0-9]{4,5}";

	private Command currentCommandState;

	private Broker broker;

	private enum Command {

		Start("start"),

		CreateBroker("create broker"),

		AddForwarder("add forwarder"),

		ListForwarders("list forwarders"),

		RemoveForwarder("remove forwarder"),

		Exit("exit"),

		Help("help");

		private String name;

		private Command(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		private static Command findCommand(String name) throws CommandNotFoundException {
			for (Command cmd : Command.values()) {
				if (name.startsWith(cmd.name)) {
					return cmd;
				}
			}
			throw new CommandNotFoundException();
		}
	}

	@SuppressWarnings("javadoc")
	public BrokerEngine() {
		currentCommandState = Command.Start;
		writePrompt();
	}

	/**
	 * Starts the command line
	 */
	public void start() {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
			String in = null;
			while (!(in = br.readLine().trim()).equals(Command.Exit.name)) {
				if (in.equals(Command.Help.name)) {
					showHelpFile();
					writePrompt();
				} else {
					consume(in);
					writePrompt();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		broker.shutdown();
		System.out.println("bye");
	}

	private void showHelpFile() {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(BrokerEngine.class.getClassLoader().getResourceAsStream("help.txt")))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e1) {
		}
	}

	private BrokerEngine consume(String in) {
		try {
			currentCommandState = Command.findCommand(in);

			switch (currentCommandState) {
			case Start:
				break;
			case CreateBroker:
				if (broker != null) {
					System.out.println("Broker is already created!");
					break;
				}

				String bAddress = extractParameter(Command.CreateBroker, in);

				if (bAddress.matches(ADDRESS_PATTERN)) {
					broker = new Broker(bAddress);
					currentCommandState = Command.Start;
					System.out.println("Successfully created broker.");
				} else {
					illegalInput();
				}
				break;
			case AddForwarder:
				if (broker == null) {
					System.out.println("You must create a broker first!");
					break;
				}

				String fAddress = extractParameter(Command.AddForwarder, in);

				if (fAddress.matches(ADDRESS_PATTERN)) {
					broker.forwardTo(fAddress, ZMQ.PUSH, null);
					currentCommandState = Command.Start;
					System.out.println("Successfully created forwarding to " + fAddress);
				} else {
					illegalInput();
				}
				break;
			case ListForwarders:
				if (isForwarderListNotEmpty()) {
					for (Forwarder fw : broker.getForwarders()) {
						System.out.println(fw.getAddress());
					}
				}
				break;
			case RemoveForwarder:
				if (isForwarderListNotEmpty()) {
					String fAddressToRemove = extractParameter(Command.RemoveForwarder, in);

					for (Forwarder fw : broker.getForwarders()) {
						if (fw.getAddress().equals(fAddressToRemove)) {
							fw.shutdown();
							broker.getForwarders().remove(fw);
							System.out.println("Successfully removed forwarder.");
							break;
						}
					}
				}
			}
		} catch (CommandNotFoundException e) {
			illegalInput();
		}
		return this;
	}

	private String extractParameter(Command cmd, String in) {
		return in.substring(cmd.name.length()).trim();
	}

	private boolean isForwarderListNotEmpty() {
		if (broker == null) {
			System.out.println("You must create a broker first!");
			return false;
		}
		if (broker.getForwarders().isEmpty()) {
			System.out.println("The forwarder list is empty.");
			return false;
		}
		return true;
	}

	private void illegalInput() {
		System.out.println("Command not supported. Type help for a list of supported commands.");
	}

	private void writePrompt() {
		System.out.print("ZeroMQ Broker> ");
	}
}
