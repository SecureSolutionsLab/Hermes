package zermia.monitor.faults;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class StackLeakFault {
	protected static SecureRandom randomGen = null;
	
    static {
        randomGen = new SecureRandom();
    }
	
	static Integer leakSize = 1024 * 1024 * 30; //small leaks 30mbytes
	static final List<byte[]> leakList = new ArrayList<>();
	
	public void executeFault(Integer numberOfLeaks) {
		byte[] leak = new byte[leakSize];
		for(int i = 0; i<numberOfLeaks; i++) {
			randomGen.nextBytes(leak);
			leakList.add(leak);
		}
		long allocatedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		long presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;
		System.out.println("Memory left " + presumableFreeMemory);
	}
}
