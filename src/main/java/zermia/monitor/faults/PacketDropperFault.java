package zermia.monitor.faults;

import java.security.SecureRandom;

public class PacketDropperFault {
	protected static SecureRandom randomGen = null;
	
    static {
        randomGen = new SecureRandom();
    }
    
	public boolean executeFault(Integer faultChance) {
		Integer randVal = randomGen.nextInt(100);
	   	if(randVal > faultChance) {
    		return false;
    	} else return randVal <= faultChance;
    }
}

