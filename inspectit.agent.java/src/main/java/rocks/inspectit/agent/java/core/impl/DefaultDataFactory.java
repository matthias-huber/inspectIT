/**
 *
 */
package rocks.inspectit.agent.java.core.impl;

import com.lmax.disruptor.EventFactory;

/**
 * TODO: description
 *
 * @author Matthias Huber
 *
 */
public class DefaultDataFactory implements EventFactory<DefaultDataWrapper> {

	/**
	 * {@inheritDoc}
	 */
	public DefaultDataWrapper newInstance() {
		return new DefaultDataWrapper();
	}

}
