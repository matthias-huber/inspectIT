package info.novatec.inspectit.agent.perf;

import info.novatec.inspectit.agent.config.impl.JmxSensorConfig;
import info.novatec.inspectit.agent.config.impl.JmxSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.connection.IConnection;
import info.novatec.inspectit.agent.connection.RegistrationException;
import info.novatec.inspectit.agent.connection.ServerUnavailableException;
import info.novatec.inspectit.communication.DefaultData;

import java.net.ConnectException;
import java.util.List;

import org.openjdk.jmh.annotations.Benchmark;

/**
 * This implementation of {@link IConnection} is <b>only</b> intended to be used within JMH
 * {@link Benchmark}s.
 * <p>
 * The implementation does not create a connection. It ensures to be always connected (^^) and will
 * not try to send anything.
 *
 * @author Matthias Huber
 *
 */
public class NoConnection implements IConnection {

	/**
	 * {@inheritDoc}
	 */
	public void connect(String host, int port) throws ConnectException {
		// we don't connect

	}

	/**
	 * {@inheritDoc}
	 */
	public void disconnect() {
		// we never disconnect
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isConnected() {
		// we are always connected
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendKeepAlive(long platformId) throws ServerUnavailableException {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	public void sendDataObjects(List<? extends DefaultData> dataObjects) throws ServerUnavailableException {
		// we don't send data
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerPlatform(String agentName, String version) throws ServerUnavailableException, RegistrationException {
		return 42;
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterPlatform(String agentName) throws RegistrationException, ServerUnavailableException {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	public long registerMethod(long platformId, RegisteredSensorConfig sensorConfig) throws ServerUnavailableException, RegistrationException {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerMethodSensorType(long platformId, MethodSensorTypeConfig methodSensorTypeConfig) throws ServerUnavailableException, RegistrationException {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerPlatformSensorType(long platformId, PlatformSensorTypeConfig platformSensorTypeConfig) throws ServerUnavailableException, RegistrationException {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerJmxSensorType(long platformId, JmxSensorTypeConfig jmxSensorTypeConfig) throws ServerUnavailableException, RegistrationException {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerJmxDefinitionData(long platformIdent, JmxSensorConfig config) throws ServerUnavailableException, RegistrationException {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addSensorTypeToMethod(long sensorTypeId, long methodId) throws ServerUnavailableException, RegistrationException {
		// TODO Auto-generated method stub

	}

}