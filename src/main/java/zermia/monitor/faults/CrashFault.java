package zermia.monitor.faults;

public class CrashFault extends Fault {

	public void injectFault() {
		System.out.println("Crash");
		System.exit(-1);
	}
}
