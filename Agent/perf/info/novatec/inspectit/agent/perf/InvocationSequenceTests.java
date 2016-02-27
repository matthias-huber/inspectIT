package info.novatec.inspectit.agent.perf;

import info.novatec.inspectit.agent.analyzer.impl.ClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.impl.InheritanceAnalyzer;
import info.novatec.inspectit.agent.buffer.impl.SimpleBufferStrategy;
import info.novatec.inspectit.agent.config.impl.ConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.PropertyAccessor;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.impl.CoreService;
import info.novatec.inspectit.agent.core.impl.CoreServiceNoPut;
import info.novatec.inspectit.agent.core.impl.IdManager;
import info.novatec.inspectit.agent.sending.ISendingStrategy;
import info.novatec.inspectit.agent.sending.impl.TimeStrategy;
import info.novatec.inspectit.agent.sensor.method.invocationsequence.InvocationSequenceHook;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.util.Timer;
import info.novatec.inspectit.version.VersionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	public void init(ThreadParams threadParams) {
		registeredSensorConfig = new RegisteredSensorConfig();

		ConfigurationStorage configurationStorage = new ConfigurationStorage(new ClassPoolAnalyzer(), new InheritanceAnalyzer(new ClassPoolAnalyzer()));
		IdManager idManager = new IdManager(configurationStorage, new NoConnection(), new VersionService());
		// set needed values on the idManager
		idManager.setPlatform();
		methodId = threadParams.getThreadIndex();
		nestedMethodId = methodId + 10;
		idManager.addMethodId(methodId);
		idManager.addMethodId(nestedMethodId);
		idManager.addSensorTypeId(sensorTypeId);

		// init hooks
		invocationSequenceHook = new InvocationSequenceHook(new Timer(), idManager, new PropertyAccessor(), new HashMap<String, Object>(), false);

		// init CoreService -> CoreServiceJMH
		List<ISendingStrategy> sendingStrategies = new ArrayList<ISendingStrategy>();
		sendingStrategies.add(new TimeStrategy());
		coreService = new CoreService(configurationStorage, new NoConnection(), new SimpleBufferStrategy(), sendingStrategies, idManager);

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

		coreService.setSensorDataObjects(sensorDataObjects);

		coreServiceNoPut = new CoreServiceNoPut(configurationStorage, new NoConnection(), new SimpleBufferStrategy(), sendingStrategies, idManager);

		// we don't start the CoreService -> we don't want to start Platform, Sending and Preparing
		// Thread in order to just benchmark hooks
		// coreService.start();
		// coreServiceNoPut.start();
	}

	// @Benchmark
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

	// @Benchmark
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

	// @Benchmark
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
	public void cleanUp() throws InterruptedException {
		coreService.printSensorDataObjects();

		coreService = null;
		coreServiceNoPut = null;
		invocationSequenceHook = null;
	}

}
