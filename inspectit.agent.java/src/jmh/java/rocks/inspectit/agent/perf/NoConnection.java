package rocks.inspectit.agent.perf;

import java.net.ConnectException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.connection.RegistrationException;
import rocks.inspectit.agent.java.connection.ServerUnavailableException;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.instrumentation.classcache.Type;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxAttributeDescriptor;

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
	@Override
	public void connect(String host, int port) throws ConnectException {
		// we don't connect
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reconnect() throws ConnectException {
		// we don't reconnect
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disconnect() {
		// we never disconnect
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isConnected() {
		// we are always connected
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendKeepAlive(long platformId) throws ServerUnavailableException {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendDataObjects(List<? extends DefaultData> dataObjects) throws ServerUnavailableException {
		// we don't send data
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AgentConfig register(String agentName, String version) throws ServerUnavailableException, RegistrationException, BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregister(long platformIdent) throws ServerUnavailableException, RegistrationException, BusinessException {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstrumentationDefinition analyze(long platformIdent, String hash, Type type) throws ServerUnavailableException, BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void instrumentationApplied(long platformIdent, Map<Long, long[]> methodToSensorMap) throws ServerUnavailableException {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<JmxAttributeDescriptor> analyzeJmxAttributes(long platformIdent, Collection<JmxAttributeDescriptor> attributeDescriptors) throws ServerUnavailableException {
		// TODO Auto-generated method stub
		return null;
	}

}