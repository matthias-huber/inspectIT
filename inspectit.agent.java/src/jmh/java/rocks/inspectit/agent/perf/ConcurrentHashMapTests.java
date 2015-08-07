/**
 *
 */
package rocks.inspectit.agent.perf;

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
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.ThreadParams;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

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

	@Param({ "0" })
	private int initialSize;

	@Param({ "0" })
	public int tokens;

	private Map<String, DefaultData> sensorDataObjects;

	private long methodIdent;
	private final long sensorTypeIdent = 1L;

	@Setup(Level.Iteration)
	public void init(ThreadParams threadParams) {
		sensorDataObjects = new ConcurrentHashMap<String, DefaultData>();
		methodIdent = threadParams.getThreadIndex();

		while (sensorDataObjects.size() != initialSize) {
			StringBuilder sb = new StringBuilder();
			sb.append(System.nanoTime() / 1000000.0d);
			sb.append('.');
			sb.append(methodIdent);
			sb.append('.');
			sb.append(sensorTypeIdent);
			sensorDataObjects.put(sb.toString(), new InvocationSequenceData());
		}

		// System.out.println("Size: " + initialSize);
	}

	@Benchmark
	public void put() {
		// Blackhole.consumeCPU(tokens);

		StringBuffer buffer = new StringBuffer();
		buffer.append(System.nanoTime() / 1000000.0d);
		buffer.append('.');
		buffer.append(methodIdent);
		buffer.append('.');
		buffer.append(sensorTypeIdent);
		sensorDataObjects.put(buffer.toString(), new InvocationSequenceData());
	}

	@TearDown(Level.Iteration)
	public void cleanUp() {
		sensorDataObjects = null;
	}
}