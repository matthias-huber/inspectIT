/**
 *
 */
package rocks.inspectit.agent.java.core.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.lmax.disruptor.EventHandler;

import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.connection.ServerUnavailableException;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * TODO: description => Sending thread, should buffer data before sending to reduce network overhead
 * and send after a while if buffer not full
 *
 * @author Matthias Huber
 *
 */
public class DefaultDataHandler implements EventHandler<DefaultDataWrapper> {

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * The default wait time.
	 */
	public static final long DEFAULT_WAIT_TIME = 5000L;

	/**
	 * The wait time. TODO: init from configuration
	 */
	private long time = DEFAULT_WAIT_TIME;

	private long lastSendingTime = 0;

	/**
	 * The connection to the Central Measurement Repository.
	 */
	private IConnection connection;

	private List<DefaultData> defaultDatas = new ArrayList<DefaultData>(20);

	/**
	 * Defines if there was an exception before while trying to send the data. Used to throttle the
	 * printing of log statements.
	 */
	private boolean sendingExceptionNotice = false;

	public DefaultDataHandler(IConnection connection) {
		this.connection = connection;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEvent(DefaultDataWrapper defaultDataWrapper, long sequence, boolean endOfBatch) {
		// System.out.println("Called with sequence " + sequence);
		defaultDatas.add(defaultDataWrapper.getDefaultData());

		// System.out.println("EndOfBatch: " + endOfBatch);

		if (endOfBatch) {
			// System.out.println("Sending...");
			try {
				connection.sendDataObjects(defaultDatas);
			} catch (ServerUnavailableException serverUnavailableException) {
				if (serverUnavailableException.isServerTimeout()) {
					log.warn("Timeout on server when sending actual data. Data might be lost!", serverUnavailableException);
				} else {
					if (!sendingExceptionNotice) {
						sendingExceptionNotice = true;
						log.error("Connection problem appeared, stopping sending actual data!", serverUnavailableException);
					}
				}

			}
			defaultDatas.clear();
		}
	}

}