package zermia.hooks.bftsmart;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class ZermiaAspectInstanceStartYCSB {

    int monitorID;
    zermia.monitor.runtime.bftsmart.BFTSmartMonitor monitor;

    @Before("execution (* bftsmart.demo.ycsb.YCSBServer.main*(..))")
    public void advice(JoinPoint joinPoint) {

        String[] ReplicaArgs = (String[]) joinPoint.getArgs()[0]; //get arguments
        String replicaID = ReplicaArgs[0]; //get replica id
        monitorID = Integer.parseInt(replicaID);
        monitor = new zermia.monitor.runtime.bftsmart.BFTSmartMonitor(monitorID);

    }
}
