package rocks.inspectit.agent.perf;

import rocks.inspectit.agent.java.core.impl.CoreService;
import rocks.inspectit.shared.all.communication.MethodSensorData;

/**
 * Used for JMH Benchmark to test the hooks without ConcurrentHashMap interaction.
 *
 * @author Matthias Huber
 *
 */
public class CoreServiceNoPut extends CoreService {

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
