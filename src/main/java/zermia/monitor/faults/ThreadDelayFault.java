package zermia.monitor.faults;

public class ThreadDelayFault extends Fault {
	private int delay;

	public ThreadDelayFault (int delay) {
		this.delay = delay;
	}

	public void injectFault() {
		try {
			Thread.currentThread().sleep(delay);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
