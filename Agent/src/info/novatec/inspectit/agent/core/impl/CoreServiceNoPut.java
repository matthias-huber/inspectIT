/**
 *
 */
package info.novatec.inspectit.agent.core.impl;

import info.novatec.inspectit.agent.buffer.IBufferStrategy;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.connection.IConnection;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.sending.ISendingStrategy;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.MethodSensorData;

import java.util.List;

/**
 * Used for JMH Benchmark to test the hooks without ConcurrentHashMap interaction.
 *
 * @author Matthias Huber
 *
 */
public class CoreServiceNoPut extends CoreService {

	public CoreServiceNoPut(IConfigurationStorage configurationStorage, IConnection connection, IBufferStrategy<DefaultData> bufferStrategy, List<ISendingStrategy> sendingStrategies,
			IIdManager idManager) {
		super(configurationStorage, connection, bufferStrategy, sendingStrategies, idManager);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addMethodSensorData(long sensorTypeIdent, long methodIdent, String prefix, MethodSensorData methodSensorData) {
		StringBuffer buffer = new StringBuffer();
		if (null != prefix) {
			buffer.append(prefix);
			buffer.append('.');
		}
		buffer.append(methodIdent);
		buffer.append('.');
		buffer.append(sensorTypeIdent);
		// sensorDataObjects.put(buffer.toString(), methodSensorData);
		// notifyListListeners();
	}

}
