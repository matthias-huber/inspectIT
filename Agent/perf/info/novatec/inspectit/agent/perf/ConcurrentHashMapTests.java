/**
 *
 */
package info.novatec.inspectit.agent.perf;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.ThreadParams;

/**
 * Non-Steady-State Verification Test for ConcurrentHashMap.
 *
 * We assume that benchmarks of the hooks currently do not have the steady-state because of the used
 * ConcurrentHashMap. This benchmark shall verify the non-steady-state characteristic of the used
 * ConcurrentHashMap operations.
 *
 * @author Matthias Huber
 */
@State(Scope.Benchmark)
@Warmup(iterations = 10, batchSize = 100000)
@Measurement(iterations = 10, batchSize = 10000)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(3)
public class ConcurrentHashMapTests {

	private Map<String, DefaultData> sensorDataObjects;

	private long methodIdent;
	private final long sensorTypeIdent = 1L;
	private final DefaultData defaultData = new InvocationSequenceData();

	@Setup(Level.Iteration)
	public void init(ThreadParams threadParams) {
		sensorDataObjects = new ConcurrentHashMap<String, DefaultData>();
		methodIdent = threadParams.getThreadIndex();
	}

	@Benchmark
	public void put() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(System.nanoTime() / 1000000.0d);
		buffer.append('.');
		buffer.append(methodIdent);
		buffer.append('.');
		buffer.append(sensorTypeIdent);
		sensorDataObjects.put(buffer.toString(), defaultData);
	}

	@TearDown(Level.Iteration)
	public void cleanUp() {
		sensorDataObjects = null;
	}
}