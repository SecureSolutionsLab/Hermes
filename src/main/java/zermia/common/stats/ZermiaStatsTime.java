package zermia.common.stats;

import java.time.Duration;
import java.time.Instant;

public class ZermiaStatsTime {
	static Instant start;
	static Instant end;
	
	public void startTimer() {
		start = Instant.now();
	}

	public double endTimer() {
		end = Instant.now();
		//System.out.println("Test end at " + Duration.between(start, end).toMillis() + " milliseconds");	
		return Duration.between(start, end).toMillis();
		
	}	
}
