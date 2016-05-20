/**
 *
 */
package rocks.inspectit.agent.java.sensor.method.elasticsearch;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.core.IIdManager;
import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;
import rocks.inspectit.agent.java.util.Timer;

/**
 * TODO
 *
 * @author Matthias Huber
 *
 */
public class ElasticsearchSensor extends AbstractMethodSensor implements IMethodSensor {

	/**
	 * Used for creating and resolving ids necessary to communicate with the server.
	 */
	@Autowired
	private IIdManager idManager;

	/**
	 * The timer used for accurate measuring.
	 */
	@Autowired
	private Timer timer;

	/** hook instance. */
	private ElasticsearchHook hook;

	/**
	 * {@inheritDoc}
	 */
	public void init(Map<String, Object> parameter) {
		hook = new ElasticsearchHook(idManager, timer);
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return hook;
	}

}
