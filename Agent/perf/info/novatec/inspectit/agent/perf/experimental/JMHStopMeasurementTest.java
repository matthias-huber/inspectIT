package info.novatec.inspectit.agent.perf.experimental;

import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Control;

/**
 * The intention of this class was to verify if asynchronous throughput can be measured with JMH
 * benchmarks with a "invocation-counter-approach". Within the agent elements are stored in a
 * HashMap by several threads, a single threads takes these elements and prepares it for sending.
 * What we want to measure is the time it takes to insert an element into the hashmap until it can
 * be send to the CMR. As this is done by several threads, we cannot measure this within a single
 * JMH benchmark.
 * <p>
 * 
 * <b>Test setup:</b><br>
 * Two benchmarks run parallel (-> @Group). One benchmark increases a counter which should be used
 * by the second as "number of invocations"<br>
 * incrementCounter: simulates the insertions into the map. It increases a counter to keep track of
 * the numbers insert into the hashmap. <br>
 * realMeasurement: The benchmark spins until the measurement is stopped (-> only one invocation of
 * the method). At the end the counter incremented by benchmark "incrementCounter" will be set as
 * the number of invocations of "realMeasurement".
 * <p>
 * 
 * <b>Expected behavior:</b><br>
 * - We get a throughput for "incrementCounter"<br>
 * - "realMeasurement" will be invoked once. We assume that the throughput is nearly the same than
 * the one from "incrementCounter" as we use the "counter" as operations per iteration (->
 * @AuxCounters).
 * <p>
 * 
 * <b>Actual behaviour:</b><br>
 * - "realMeasurement" is invoked twice<br>
 * - throughputs of the two benchmarks are not nearly the same. They diverge by factor > 40.000<br>
 * - e.g.<br>
 * 
 * <code>
 * Benchmark                                                            Mode  Cnt              Score              Error  Units
 * StopMeasurementTest.TheScoreAlwaysSettlesAtTheEnd:incrementCounter  thrpt   20      303344894.519 ±      6047416.513  ops/s
 * StopMeasurementTest.TheScoreAlwaysSettlesAtTheEnd:realMeasurement   thrpt   20           7050.256 ±         1980.342  ops/s
 * </code>
 * <p>
 * 
 * We don't have an explanation why the figures diverge so much.
 * 
 * Result: "invocation-counter-approach" is not usable.
 * 
 * @author Matthias Huber
 * 
 */
@State(Scope.Benchmark)
@Fork(1)
public class JMHStopMeasurementTest {

	private long counter;
	private int executionCounter;

	@Setup(Level.Iteration)
	public void init() {
		counter = 0;
		executionCounter = 0;
	}

	@AuxCounters
	@State(Scope.Thread)
	public static class AdditionalCounters {
		public long count;

		@Setup(Level.Iteration)
		public void clean() {
			count = 0;
		}

	}

//	@Benchmark
	@Group("TheScoreAlwaysSettlesAtTheEnd")
	public long incrementCounter(Control control) {
		// only increase the counter during the measurement phase.
		// Since JMH cannot stop threads immediately. This method will be called hundreds or even
		// thousand times before the thread stops invoking the method.
		if (!control.stopMeasurement) {
			counter++;
		}
		return counter;
	}

//	@Benchmark
	@Group("TheScoreAlwaysSettlesAtTheEnd")
	public void realMeasurement(Control control, AdditionalCounters additionalCounters) {
		executionCounter++;
		System.out.println("Execution Counter: " + executionCounter);

		// unfortunately JMH cannot stop threads immediately. It can just tell the threads to stop.
		// In our tests this method has been called twice
		if (executionCounter == 1) {
			// loop until JMH stops the measurement
			while (!control.stopMeasurement) {
			}
			additionalCounters.count = counter; // tell JMH that this method was performed "counter"
												// times.

			// Uncomment these lines to verify that the "counter" will not increase
			// after JMH stops the measurement iteration (-> stopMeasurement)
			System.out.println("Counter: " + counter);
			System.out.println("Additional: " + additionalCounters.count);
		}
	}

}
