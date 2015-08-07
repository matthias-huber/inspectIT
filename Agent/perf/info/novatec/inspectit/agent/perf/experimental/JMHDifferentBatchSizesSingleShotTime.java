package info.novatec.inspectit.agent.perf.experimental;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Warmup;

/**
 * One idea to test asynchronous throughput with JMH was to use two benchmark methods which both run
 * during a measurement iteration. One method should be invoked a defined number of times (due to
 * Non-Steady-State-Problematic ->
 * http://shipilev.net/blog/2014/nanotrusting-nanotime/#_steady_state_considerations) and increment
 * how much work has been done. The other method should run only once and will be internally be
 * blocked until the other benchmark has performed a predefined work. If the work is reached, the
 * whole benchmark should be stopped.
 * <p>
 * This test will show if JMH allows to run grouped benchmarks with different batch sizes.
 * <p>
 * <b>Expected:</b><br>
 * Benchmark A should run 100 times, Benchmark B should run only ones.
 * <p>
 * <b>Actual:</b><br>
 * error: Colliding annotations: [@org.openjdk.jmh.annotations.Measurement(batchSize=1, timeUnit=SECONDS, time=-1, iterations=1)] vs. [@org.openjdk.jmh.annotations.Measurement(batchSize=1000, timeUnit=SECONDS, time=-1, iterations=1)]
 * 
 * @author Matthias Huber
 * 
 */
public class JMHDifferentBatchSizesSingleShotTime {

//	@Benchmark
	@Group("DifferentBatchSizes")
	@Warmup(iterations = 0)
	@Measurement(iterations = 1, batchSize = 1000)
	@BenchmarkMode(Mode.SingleShotTime)
	public void a() {
		System.out.println("A");
	}

//	@Benchmark
	@Group("DifferentBatchSizes")
	@Warmup(iterations = 0)
	@Measurement(iterations = 1, batchSize = 1)
	@BenchmarkMode(Mode.SingleShotTime)
	public void b() {
		System.out.println("B");
	}
	
}