package zermia.monitor.faults;

public class BigPacketFault {

	public byte[] executeFault(byte[] data, Integer timesLarger) {
		Integer lenghtD = data.length * timesLarger;
		Integer iterator = data.length;
		byte[] dataR = new byte[lenghtD]; 
		for(int i=0; i<lenghtD; i=i+iterator) {
			System.arraycopy(data, 0, dataR, i, data.length);
		}
		return dataR;
		
	}
	
	
}
