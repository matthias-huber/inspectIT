package info.novatec.inspectit.agent.perf.experimental;


import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Control;

/**
 * One idea to test asynchronous throughput with JMH was to block one benchmark thread until a
 * specific amount of elements arrived at the buffer. The execution time of the benchmark invocation
 * could then be used to calculate the throughput.
 * <p>
 * Both benchmark methods are run 10 times in parallel (see
 * {@link JMHDifferentBatchSizesSingleShotTime} why we can't use different batch sizes for the
 * tests).<br>
 * 'incrementCounter' will increase each invocation a benchmark-global counter by 1 and checks if a
 * condition is reached ({@link #checkCondition()}.<br>
 * 'waitTilCounterReached' will be blocked and should finished its iteration after
 * 'incrementCounter' has been called 'batchSize' times.
 * 
 * <b>Desired behavior:</b><br>
 * 'incrementCounter' runs 'batchSize' times<br>
 * 'waitTilCounterReached' will be blocked in its first iteration until 'incrementCounter' has been
 * invoked 'batchSize'-times. It will only be called ones because measurement iteration is finished.
 * <p>
 * <b>Actual behavior:</b><br>
 * 'incrementCounter' runs 'batchSize' times<br>
 * 'waitTilCounterReached' will be blocked in its first iteration until 'incrementCounter' has been
 * invoked 'batchSize'-times. It is called 'batchSize'-1 times after we hoped that JMH will just
 * stop the test.
 * <p>
 * <b>We cannot use this approach because 'waitTilCounterReached' is invoked too many times. The
 * measurements are just garbage.</b>
 * 
 * @author Matthias Huber
 * 
 */
@State(Scope.Benchmark)
@Fork(1)
public class JMHLockVerification {

	private static final int batchSize = 100;

	private Lock lock = new ReentrantLock();
	private Condition occuringCondition;

	private int counter;

	@Setup(Level.Iteration)
	public void init() {
		occuringCondition = lock.newCondition();
		counter = 0;
	}

	private void checkCondition(Control control) {
		if (counter == batchSize) {
			control.stopMeasurement = true; // looks like JMH doesn't care about this
			System.out.println("Finally counter reached");
			lock.lock();
			System.out.println("Finally lock get");
			occuringCondition.signal();
			lock.unlock();
		}
	}

//	@Benchmark
	@Group("LockAndUnlock")
	@Warmup(iterations = 0)
	@Measurement(iterations = 1, batchSize = batchSize)
	@BenchmarkMode(Mode.SingleShotTime)
	public int incrementCounter(Control control) {
		if (!control.stopMeasurement) {
			counter++;
			System.out.println("Inced");
			checkCondition(control);
		}
		return counter;
	}

//	@Benchmark
	@Group("LockAndUnlock")
	@Warmup(iterations = 0)
	@Measurement(iterations = 1, batchSize = batchSize)
	@BenchmarkMode(Mode.SingleShotTime)
	public void waitTilCounterReached(Control control) {
		if (!control.stopMeasurement) {
			lock.lock();
			try {
				System.out.println("waitTilCounterReached: Waiting...");
				occuringCondition.await();
				System.out.println("waitTilCounterReached: ...finished. Try to stop the measurement");
				control.stopMeasurement = true;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("What the hell!! Test should be stopped");
		}
	}

}
