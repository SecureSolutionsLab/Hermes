package zermia.monitor.faults;

import java.security.SecureRandom;

public class ZeroPacketFault {
	protected static SecureRandom randomGen = null;
	
    static {
        randomGen = new SecureRandom();
    }
	
	
	public byte[] executeFault(Integer faultChance, byte[] data ) {
		Integer randVal = randomGen.nextInt(100);
	   	if(randVal > faultChance) {
    	} else if(randVal <= faultChance){
	   		for (int i = 0; i < data.length; i++) {
                data[i] = 0;
             }
    	}
	   	return data; 
    }
}
