package zermia.hooks.bftsmart;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect	
public class ZermiaAspectInstanceStart {

//	zermia.monitor.runtime.bftsmart.BFTSmartMonitor monitor;
//
//	@Before("execution (* bftsmart.demo.counter.BFTCounterServer.main*(..))")
//	 	public void advice(JoinPoint joinPoint) {
//		String[] replicaArgs = (String[]) joinPoint.getArgs()[0]; //get arguments
//		int replicaID =  Integer.parseInt(replicaArgs[0]); //get replica id
//		monitor = new zermia.monitor.runtime.bftsmart.BFTSmartMonitor(replicaID);
//		zermia.monitor.runtime.MonitorRuntime.bootstrapMonitor(monitor);
//	}
}

