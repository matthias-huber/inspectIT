package rocks.inspectit.agent.perf.experimental;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Timeout;
import org.openjdk.jmh.annotations.Warmup;

/**
 * One idea to test asynchronous throughput with JMH was to block one benchmark thread until a
 * specific amount of elements arrived at the buffer. The execution time of the benchmark invocation
 * could then be used to calculate the throughput.
 * <p>
 * We weren't sure if JMH allows us to use locks. This test will show that the benchmark method
 * will be blocked until the JMH timeout occurs and the thread will be interrupted.
 * <p>
 * <b>Expected:</b><br>
 * Thread will be interrupted after the timeout {@link Timeout} on class level.
 * <p>
 * <b>Actual:</b><br>
 * Thread is interrupted after the timout has been reached.
 * 
 * @author Matthias Huber
 * 
 */
@State(Scope.Benchmark)
@Fork(1)
@Timeout(time = 1, timeUnit = TimeUnit.MINUTES)
public class JMHLockTimeoutTest {

	private Lock lock = new ReentrantLock();
	private Condition neverEndingCondition;

	@Setup
	public void init() {
		neverEndingCondition = lock.newCondition();
	}

//	@Benchmark
	@Warmup(iterations = 0, batchSize = 1)
	@Measurement(iterations = 1, batchSize = 1)
	@BenchmarkMode(Mode.SingleShotTime)
	public void blockUntilTheEndOfTheWorld() throws InterruptedException {
		lock.lock();
		neverEndingCondition.await();
	}
}
