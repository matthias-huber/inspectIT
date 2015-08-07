package info.novatec.inspectit.agent.perf;

import info.novatec.inspectit.agent.analyzer.impl.ClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.impl.InheritanceAnalyzer;
import info.novatec.inspectit.agent.config.impl.ConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.PropertyAccessor;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.impl.IdManager;
import info.novatec.inspectit.agent.sensor.method.invocationsequence.InvocationSequenceHook;
import info.novatec.inspectit.agent.sensor.method.timer.TimerHook;
import info.novatec.inspectit.util.Timer;
import info.novatec.inspectit.version.VersionService;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

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
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.infra.ThreadParams;

/**
 * JMH Test for {@link TimerHook} in combination with Coreservice {@link InvocationSequenceHook}.
 *
 * @author Matthias Huber
 *
 */
@State(Scope.Benchmark)
@Warmup(iterations = 10, batchSize = 100000)
@Measurement(iterations = 10, batchSize = 100000)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(3)
public class TimerHookWithInvocationSequenceTests {

	@Param({ "0" })
	public int tokens;

	private InvocationSequenceHook invocationSequenceHook;

	private TimerHook timerHook;
	private TimerHook timerHookNoThreadCPU;

	private long methodId;
	private final long sensorTypeId = 1L;
	private RegisteredSensorConfig registeredSensorConfig;

	@Setup(Level.Trial)
	public void checkCondition() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		boolean validTest = bean.isCurrentThreadCpuTimeSupported();
		if (validTest) {
			validTest = bean.isThreadCpuTimeEnabled();
			if (!validTest) {
				// try if it can be set
				bean.setThreadCpuTimeEnabled(true);
				validTest = bean.isThreadCpuTimeEnabled();
			}
		}
		if (!validTest) {
			throw new IllegalStateException("Tests cannot differentiate between CPU Thread enabled or disabled!");
		}
	}

	@Setup(Level.Iteration)
	public void init(ThreadParams threadParams) {
		registeredSensorConfig = new RegisteredSensorConfig();

		ConfigurationStorage configurationStorage = new ConfigurationStorage(new ClassPoolAnalyzer(), new InheritanceAnalyzer(new ClassPoolAnalyzer()));
		IdManager idManager = new IdManager(configurationStorage, new NoConnection(), new VersionService());
		// set needed values on the idManager
		idManager.setPlatform();
		methodId = threadParams.getThreadIndex();
		idManager.addMethodId(methodId);
		idManager.addSensorTypeId(sensorTypeId);

		// init hooks
		invocationSequenceHook = new InvocationSequenceHook(new Timer(), idManager, new PropertyAccessor(), new HashMap<String, Object>(), false);
		timerHook = new TimerHook(new Timer(), idManager, new PropertyAccessor(), new HashMap<String, Object>(), ManagementFactory.getThreadMXBean());
		timerHookNoThreadCPU = new TimerHook(new Timer(), idManager, new PropertyAccessor(), new HashMap<String, Object>(), null);

		// start an invocation
		invocationSequenceHook.beforeBody(methodId, sensorTypeId, null, null, registeredSensorConfig);
	}

	// @Benchmark
	public void baselineConsumeCPU() {
		Blackhole.consumeCPU(tokens);
	}

	/**
	 * Benchmarks the overhead time of measuring a method invocation with inspectIT.
	 * <p>
	 * TODOs: We use only one sensor on one method! Is this valid for testing!!!
	 * <p>
	 * It measures the best case scenario:<br>
	 * - creation of a timestamp before the method is invoked.<br>
	 * - creation of a timestamp after the method invocation is finished.<br>
	 * - shortest second-after-body-scenario possible.<br>
	 * <br>
	 * What is not measured in the second-after-body:<br>
	 * - parameter extraction
	 *
	 */
	// @Benchmark
	public void measureMethodWithinInvocation() {
		// Blackhole.consumeCPU(tokens);

		timerHook.beforeBody(methodId, sensorTypeId, null, null, null);
		timerHook.firstAfterBody(methodId, sensorTypeId, null, null, null, null);
		timerHook.secondAfterBody(invocationSequenceHook, methodId, sensorTypeId, null, null, null, registeredSensorConfig);
	}

	/**
	 * Benchmarks the overhead time of measuring a method invocation + Thread CPU Time with
	 * inspectIT.
	 * <p>
	 * TODOs: We use only one sensor on one method! Is this valid for testing!!!
	 * <p>
	 * It measures the best case scenario:<br>
	 * - creation of a timestamp before the method is invoked.<br>
	 * - creation of a timestamp after the method invocation is finished.<br>
	 * - shortest second-after-body-scenario possible.<br>
	 * <br>
	 * What is not measured in the second-after-body:<br>
	 * - parameter extraction
	 *
	 */
	// @Benchmark
	public void measureMethodWithinInvocationNoThreadCPU() {
		// Blackhole.consumeCPU(tokens);

		timerHookNoThreadCPU.beforeBody(methodId, sensorTypeId, null, null, null);
		timerHookNoThreadCPU.firstAfterBody(methodId, sensorTypeId, null, null, null, null);
		timerHookNoThreadCPU.secondAfterBody(invocationSequenceHook, methodId, sensorTypeId, null, null, null, registeredSensorConfig);
	}

	@TearDown(Level.Iteration)
	public void cleanUp() {
		invocationSequenceHook = null;
		timerHook = null;
		timerHookNoThreadCPU = null;
	}

}
