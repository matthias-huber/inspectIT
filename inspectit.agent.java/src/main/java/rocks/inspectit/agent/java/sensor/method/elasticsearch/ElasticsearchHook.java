/**
 *
 */
package rocks.inspectit.agent.java.sensor.method.elasticsearch;

import java.lang.reflect.InvocationTargetException;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IIdManager;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.util.ThreadLocalStack;
import rocks.inspectit.agent.java.util.Timer;

/**
 * TODO
 *
 * @author Matthias Huber
 *
 */
public class ElasticsearchHook implements IMethodHook {

	/** the id manager. */
	private final IIdManager idManager;

	/**
	 * The stack containing the start time values.
	 */
	private final ThreadLocalStack<Double> timeStack = new ThreadLocalStack<Double>();

	/**
	 * The timer used for accurate measuring.
	 */
	private final Timer timer;

	/**
	 *
	 */
	public ElasticsearchHook(IIdManager idManager, Timer timer) {
		this.idManager = idManager;
		this.timer = timer;
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
		System.out.println("beforeBodyCalled");
	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
	}

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		double endTime = timeStack.pop().doubleValue();
		double startTime = timeStack.pop().doubleValue();

		double duration = endTime - startTime;

		System.out.println("Duration: " + duration);
		for (Object param : parameters) {
			System.out.println(param.getClass().getCanonicalName());
		}

		Object searchRequest = null;
		for (Object param : parameters) {
			if ("org.elasticsearch.action.search.SearchRequest".equals(param.getClass().getCanonicalName())) {
				System.out.println("FOUND");
				searchRequest = param;
			}
		}

		try {
			String[] indices = (String[]) searchRequest.getClass().getDeclaredMethod("indices", null).invoke(searchRequest, null);
			for (String index : indices) {
				System.out.println(index);
			}
			String[] types = (String[]) searchRequest.getClass().getDeclaredMethod("types", null).invoke(searchRequest, null);
			for (String type : types) {
				System.out.println(type);
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
