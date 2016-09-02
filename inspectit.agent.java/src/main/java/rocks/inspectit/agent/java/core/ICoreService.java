package rocks.inspectit.agent.java.core;

import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * Interface definition for the core service. The core service is the central point of the Agent
 * where all data is collected, triggered etc.
 *
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * @author Alfred Krauss
 *
 */
public interface ICoreService {

	/**
	 * Start this component.
	 */
	void start();

	/**
	 * Stop this component.
	 */
	void stop();

	// TODO: description
	void addDefaultData(DefaultData defaultData);

}
