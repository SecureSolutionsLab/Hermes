package zermia.monitor.faults;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class Leakv2Fault {
	static Integer leakSize = 1024 * 1024 * 10; //small leaks 10mbytes
	static final List<byte[]> leakList = new ArrayList<>();
	
	protected static SecureRandom randomGen = null;
	
    static {
        randomGen = new SecureRandom();
    }
	
	public void executeFault(Integer numberOfLeaks) {
		byte[] leak = new byte[leakSize];
		for(int i = 0; i<numberOfLeaks; i++) {
			randomGen.nextBytes(leak);
			leakList.add(leak);
		}
		long allocatedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		long presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;
		System.out.println("Memory left " + presumableFreeMemory);
		
		leakList.clear();
	}
	
	
}
