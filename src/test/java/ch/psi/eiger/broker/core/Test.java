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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class Test implements PropertyChangeListener {

	public static void main(String[] args) {
		FrequencyMeasurer m = new FrequencyMeasurer();
		m.addFrequencyListener(new Test());

		for (int i = 0; i < 1_000_000_000; i++) {
			try {
				Thread.sleep(new Random().nextInt(10));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			m.measureFrequency(i);
		}
	}

	public static class FrequencyMeasurer {

		private Timer timer;

		private volatile Long counter;

		private Set<PropertyChangeListener> listeners;

		public FrequencyMeasurer() {
			listeners = new HashSet<>();
			counter = 0L;
		}

		public <T> T measureFrequency(T object) {
			counter++;
			return object;
		}

		public void addFrequencyListener(PropertyChangeListener listener) {
			if (listeners.isEmpty()) {
				if (timer != null) {
					timer.cancel();
				}
				timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask() {

					{
						counter = 0L;
					}

					@Override
					public void run() {
						Long freq = counter;
						counter = 0L;
						notifyListeners(freq);
					}
				}, 0, 1000);
			}

			listeners.add(listener);
		}

		private void notifyListeners(Long freq) {
			for (PropertyChangeListener listener : listeners) {
				listener.propertyChange(new PropertyChangeEvent(this, "Frequency", null, freq));
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		System.out.println(evt.getNewValue());

	}
}
