package info.novatec.inspectit.util;

public class RealMillisTimer implements ITimer {

	public double getCurrentTime() {
		return System.currentTimeMillis();
	}

}
