package rocks.inspectit.agent.perf;

import java.lang.reflect.Field;
import java.util.HashMap;
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
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.infra.ThreadParams;

import rocks.inspectit.agent.java.config.impl.PropertyAccessor;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.IObjectStorage;
import rocks.inspectit.agent.java.core.impl.CoreService;
import rocks.inspectit.agent.java.core.impl.PlatformManager;
import rocks.inspectit.agent.java.sensor.method.invocationsequence.InvocationSequenceHook;
import rocks.inspectit.agent.java.util.Timer;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

@State(Scope.Benchmark)
@Warmup(iterations = 10, batchSize = 100000)
@Measurement(iterations = 10, batchSize = 10000)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(3)
public class InvocationSequenceTests {

	@Param({ "0" })
	private int initialSize;

	@Param({ "0" })
	public int tokens;

	private InvocationSequenceHook invocationSequenceHook;

	private CoreService coreService;
	private CoreServiceNoPut coreServiceNoPut;

	private long methodId;
	private long nestedMethodId;
	private final long sensorTypeId = 1L;
	private RegisteredSensorConfig registeredSensorConfig;

	@Setup(Level.Iteration)
	public void init(ThreadParams threadParams) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		registeredSensorConfig = new RegisteredSensorConfig();
		Map<String, Object> settings = new HashMap<String, Object>();
		registeredSensorConfig.setSettings(settings);

		methodId = threadParams.getThreadIndex();
		nestedMethodId = methodId + 10;

		// init hooks
		PlatformManager platformManager = new PlatformManager();
		Field platformIdField = platformManager.getClass().getDeclaredField("platformId");
		platformIdField.setAccessible(true);
		platformIdField.setLong(platformManager, 42L);
		platformIdField.setAccessible(false);

		invocationSequenceHook = new InvocationSequenceHook(new Timer(), platformManager, new PropertyAccessor(), new HashMap<String, Object>(), false);

		// init CoreService
		coreService = new CoreService();

		// set the pre-initialized map
		Map<String, DefaultData> sensorDataObjects = new ConcurrentHashMap<String, DefaultData>();

		while (sensorDataObjects.size() != initialSize) {
			StringBuilder sb = new StringBuilder();
			sb.append(System.nanoTime() / 1000000.0d);
			sb.append('.');
			sb.append(methodId);
			sb.append('.');
			sb.append(sensorTypeId);
			sensorDataObjects.put(sb.toString(), new InvocationSequenceData());
		}

		// System.out.println("Size: " + initialSize);

		Field sensorDataObjectsField = coreService.getClass().getDeclaredField("sensorDataObjects");
		sensorDataObjectsField.setAccessible(true);
		sensorDataObjectsField.set(coreService, sensorDataObjects);
		sensorDataObjectsField.setAccessible(false);

		// init CoreServiceNoPut
		coreServiceNoPut = new CoreServiceNoPut();

		// we don't start the CoreService -> we don't want to start Platform, Sending and Preparing
		// Thread in order to just benchmark hooks
		// coreService.start();
		// coreServiceNoPut.start();
	}

	@Benchmark
	public void baseline_consumeCPU() {
		Blackhole.consumeCPU(tokens);
	}

	@Benchmark
	public void startAnInvocation() {
		// Blackhole.consumeCPU(tokens);

		invocationSequenceHook.beforeBody(methodId, sensorTypeId, null, null, registeredSensorConfig);
		invocationSequenceHook.firstAfterBody(methodId, sensorTypeId, null, null, null, registeredSensorConfig);
		invocationSequenceHook.secondAfterBody(coreService, methodId, sensorTypeId, null, null, null, registeredSensorConfig);
		// resetting the invocation data is done in the secondAfterBody
	}

	@Benchmark
	public void startAnInvocationNoPut() {
		// Blackhole.consumeCPU(tokens);
		invocationSequenceHook.beforeBody(methodId, sensorTypeId, null, null, registeredSensorConfig);
		invocationSequenceHook.firstAfterBody(methodId, sensorTypeId, null, null, null, registeredSensorConfig);
		invocationSequenceHook.secondAfterBody(coreServiceNoPut, methodId, sensorTypeId, null, null, null, registeredSensorConfig);
		// resetting the invocation data is done in the secondAfterBody
	}

	@Benchmark
	public void startAnInvocationPlusMethodSameId() {
		// Blackhole.consumeCPU(tokens);

		// invocation sequence
		invocationSequenceHook.beforeBody(methodId, sensorTypeId, null, null, registeredSensorConfig);

		// nested method
		invocationSequenceHook.beforeBody(methodId, sensorTypeId, null, null, registeredSensorConfig);
		invocationSequenceHook.firstAfterBody(methodId, sensorTypeId, null, null, null, registeredSensorConfig);
		invocationSequenceHook.secondAfterBody(coreService, methodId, sensorTypeId, null, null, null, registeredSensorConfig);

		// stop the invocation sequence
		invocationSequenceHook.firstAfterBody(methodId, sensorTypeId, null, null, null, registeredSensorConfig);
		invocationSequenceHook.secondAfterBody(coreService, methodId, sensorTypeId, null, null, null, registeredSensorConfig);
	}

	@Benchmark
	public void startAnInvocationPlusMethodOtherId() {
		// Blackhole.consumeCPU(tokens);

		// invocation sequence
		invocationSequenceHook.beforeBody(methodId, sensorTypeId, null, null, registeredSensorConfig);

		// nested method
		invocationSequenceHook.beforeBody(nestedMethodId, sensorTypeId, null, null, registeredSensorConfig);
		invocationSequenceHook.firstAfterBody(nestedMethodId, sensorTypeId, null, null, null, registeredSensorConfig);
		invocationSequenceHook.secondAfterBody(coreService, nestedMethodId, sensorTypeId, null, null, null, registeredSensorConfig);

		// stop the invocation sequence
		invocationSequenceHook.firstAfterBody(methodId, sensorTypeId, null, null, null, registeredSensorConfig);
		invocationSequenceHook.secondAfterBody(coreService, methodId, sensorTypeId, null, null, null, registeredSensorConfig);
	}

	@TearDown(Level.Iteration)
	public void cleanUp() throws InterruptedException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field sensorDataObjectsField = coreService.getClass().getDeclaredField("sensorDataObjects");
		sensorDataObjectsField.setAccessible(true);
		Map<String, IObjectStorage> sensorDataObjects = (Map<String, IObjectStorage>) sensorDataObjectsField.get(coreService);
		// System.out.println(sensorDataObjects.size());

		coreService = null;
		coreServiceNoPut = null;
		invocationSequenceHook = null;
	}

}
