/**
 *
 */
package rocks.inspectit.shared.all.communication.data;

/**
 * @author Matthias Huber
 *
 */
public class ElasticsearchQueryData extends InvocationAwareData {

	/** Serial version id. */
	private static final long serialVersionUID = -4172540264820761255L;

	private String[] indices;

	private String[] types;

	private String query;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getInvocationAffiliationPercentage() {
		return getObjectsInInvocationsCount() / 1;
	}

}
