package rocks.inspectit.agent.perf;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
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
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.infra.ThreadParams;

import rocks.inspectit.agent.java.config.impl.PropertyAccessor;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.impl.CoreService;
import rocks.inspectit.agent.java.core.impl.PlatformManager;
import rocks.inspectit.agent.java.sensor.method.timer.TimerHook;
import rocks.inspectit.agent.java.util.Timer;

/**
 * JMH Test for {@link TimerHook} in combination with Coreservice {@link CoreService}.
 *
 * @author Matthias Huber
 *
 */
@State(Scope.Benchmark)
@Warmup(iterations = 10, batchSize = 100000)
@Measurement(iterations = 10, batchSize = 10000)
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
	public void init(ThreadParams threadParams) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException {
		registeredSensorConfig = new RegisteredSensorConfig();
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("charting", true);
		registeredSensorConfig.setSettings(settings);

		methodId = threadParams.getThreadIndex();

		// init hooks
		PlatformManager platformManager = new PlatformManager();
		Field platformIdField = platformManager.getClass().getDeclaredField("platformId");
		platformIdField.setAccessible(true);
		platformIdField.setLong(platformManager, 42L);
		platformIdField.setAccessible(false);

		timerHook = new TimerHook(new Timer(), platformManager, new PropertyAccessor(), new HashMap<String, Object>(), ManagementFactory.getThreadMXBean());
		timerHookNoThreadCPU = new TimerHook(new Timer(), platformManager, new PropertyAccessor(), new HashMap<String, Object>(), ManagementFactory.getThreadMXBean());

		// disable CPU Threading
		Field cpuThreadEnabledField = timerHookNoThreadCPU.getClass().getDeclaredField("enabled");
		cpuThreadEnabledField.setAccessible(true);
		cpuThreadEnabledField.setBoolean(timerHookNoThreadCPU, false);
		cpuThreadEnabledField.setAccessible(false);

		// init CoreService
		coreService = new CoreService();

		// set connection
		Field connectionField = coreService.getClass().getDeclaredField("connection");
		connectionField.setAccessible(true);
		connectionField.set(coreService, new NoConnection());
		connectionField.setAccessible(false);

		// start disruptor
		Method startDisruptor = coreService.getClass().getDeclaredMethod("startDisruptor");
		startDisruptor.setAccessible(true);
		startDisruptor.invoke(coreService);
		startDisruptor.setAccessible(false);

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
	@Benchmark
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
	@Benchmark
	public void measureMethodNoThreadCPU() {
		// Blackhole.consumeCPU(tokens);

		timerHookNoThreadCPU.beforeBody(methodId, sensorTypeId, null, null, null);
		timerHookNoThreadCPU.firstAfterBody(methodId, sensorTypeId, null, null, null, null);
		timerHookNoThreadCPU.secondAfterBody(coreService, methodId, sensorTypeId, null, null, null, registeredSensorConfig);
	}

	// TODO: tests with parameter extraction

	@TearDown(Level.Iteration)
	public void cleanUp() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		Method stopDisruptor = coreService.getClass().getDeclaredMethod("stopDisruptor");
		stopDisruptor.setAccessible(true);
		stopDisruptor.invoke(coreService);
		stopDisruptor.setAccessible(false);

		coreService = null;
		timerHook = null;
		timerHookNoThreadCPU = null;
	}
}
