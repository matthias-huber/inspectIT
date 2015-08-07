package info.novatec.inspectit.agent.perf;

import info.novatec.inspectit.agent.analyzer.impl.ClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.impl.InheritanceAnalyzer;
import info.novatec.inspectit.agent.buffer.impl.SimpleBufferStrategy;
import info.novatec.inspectit.agent.config.impl.ConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.PropertyAccessor;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.impl.CoreService;
import info.novatec.inspectit.agent.core.impl.IdManager;
import info.novatec.inspectit.agent.sending.ISendingStrategy;
import info.novatec.inspectit.agent.sending.impl.TimeStrategy;
import info.novatec.inspectit.agent.sensor.method.timer.TimerHook;
import info.novatec.inspectit.util.Timer;
import info.novatec.inspectit.version.VersionService;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * JMH Test for {@link TimerHook} in combination with Coreservice {@link CoreService}.
 *
 * @author Matthias Huber
 *
 */
@State(Scope.Benchmark)
@Warmup(iterations = 10, batchSize = 200000)
@Measurement(iterations = 10, batchSize = 200000)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(3)
public class TimerHookTests {

	@Param({ "0" })
	public int tokens;

	private CoreService coreService;

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
		timerHook = new TimerHook(new Timer(), idManager, new PropertyAccessor(), new HashMap<String, Object>(), ManagementFactory.getThreadMXBean());
		timerHookNoThreadCPU = new TimerHook(new Timer(), idManager, new PropertyAccessor(), new HashMap<String, Object>(), null);

		// init CoreService -> CoreServiceJMH
		List<ISendingStrategy> sendingStrategies = new ArrayList<ISendingStrategy>();
		sendingStrategies.add(new TimeStrategy());
		coreService = new CoreService(configurationStorage, new NoConnection(), new SimpleBufferStrategy(), sendingStrategies, idManager);

		// we don't start the CoreService -> we don't want to start Platform, Sending and Preparing
		// Thread in order to just benchmark hooks
		// coreService.start();
	}

	// @Benchmark
	public void baselineConsumeCPU() {
		Blackhole.consumeCPU(tokens);
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
	 * ---- storage is created in the first invocation of the benchmark method. All other
	 * invocations add to the previously created storage. <br>
	 * What is not measured in the second-after-body:<br>
	 * - parameter extraction
	 *
	 */
	// @Benchmark
	public void measureMethod() {
		// Blackhole.consumeCPU(tokens);

		timerHook.beforeBody(methodId, sensorTypeId, null, null, null);
		timerHook.firstAfterBody(methodId, sensorTypeId, null, null, null, null);
		timerHook.secondAfterBody(coreService, methodId, sensorTypeId, null, null, null, registeredSensorConfig);
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
	 * ---- storage is created in the first invocation of the benchmark method. All other
	 * invocations add to the previously created storage. <br>
	 * What is not measured in the second-after-body:<br>
	 * - parameter extraction
	 *
	 */
	// @Benchmark
	public void measureMethodNoThreadCPU() {
		// Blackhole.consumeCPU(tokens);

		timerHookNoThreadCPU.beforeBody(methodId, sensorTypeId, null, null, null);
		timerHookNoThreadCPU.firstAfterBody(methodId, sensorTypeId, null, null, null, null);
		timerHookNoThreadCPU.secondAfterBody(coreService, methodId, sensorTypeId, null, null, null, registeredSensorConfig);
	}

	// TODO: tests with parameter extraction

	@TearDown(Level.Iteration)
	public void cleanUp() {
		coreService.printInformationObjectStorage();

		coreService = null;
		timerHook = null;
		timerHookNoThreadCPU = null;
	}
}
