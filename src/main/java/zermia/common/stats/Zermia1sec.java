package zermia.common.stats;

import java.time.Duration;
import java.time.Instant;

public class Zermia1sec {
	static Instant start1sec;
	static Instant end1sec;
	static Integer time1sec;
	static boolean firstTime = true;
	
	public void startTimer() {
		start1sec = Instant.now();
	}
	
	public boolean time1Sec() {
		if(firstTime) {
			firstTime = false;
			startTimer();
		}	
		end1sec = Instant.now();
		time1sec = (int) Duration.between(start1sec, end1sec).toMillis();	
		if(time1sec >= 1000) {
			startTimer();
			return true; //envia mensages/sec para server
		} else return false;
	}
	
}
