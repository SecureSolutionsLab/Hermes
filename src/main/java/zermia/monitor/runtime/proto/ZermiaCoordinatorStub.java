package zermia.monitor.runtime.proto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import zermia.common.schedule.FaultArguments;
import zermia.common.schedule.FaultDescription;
import zermia.common.schedule.TriggerConditions;
import zermia.common.schedule.arguments.DelayFaultArguments;
import zermia.common.schedule.arguments.DropFaultArguments;
import zermia.common.schedule.arguments.DuplicateFaultArguments;
import zermia.coordinator.config.CoordinatorConfiguration;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import zermia.monitor.runtime.config.MonitorConfiguration;
import zermia.coordinator.ZermiaCoordinatorService.Proto_MonitorRequest;
import zermia.coordinator.ZermiaCoordinatorService.Proto_RegistrationReply;
import zermia.coordinator.ZermiaCoordinatorServicesGrpc;

import java.util.LinkedList;

/**
 * ClientSide implementation of CoordinationService
 */
public class ZermiaCoordinatorStub {
	private static ZermiaCoordinatorServicesGrpc.ZermiaCoordinatorServicesBlockingStub runtimeBlockStub = null;
	private static CoordinatorConfiguration props = null;
	private static ManagedChannel mgChannel = null;

	public ZermiaCoordinatorStub(CoordinatorConfiguration props) {
		if(runtimeBlockStub == null) {
			zermia.monitor.runtime.proto.ZermiaCoordinatorStub.props = props;
			zermia.monitor.runtime.proto.ZermiaCoordinatorStub.init();
		}
	}

	private static void init() {
		props.loadProperties(CoordinatorConfiguration.defaultPropertiesFilePath);
		mgChannel = ManagedChannelBuilder.forAddress(props.getCoordinatorIP(), props.getCoordinatorPort())
				.usePlaintext()
				.build();
		runtimeBlockStub = ZermiaCoordinatorServicesGrpc.newBlockingStub(mgChannel);
	}
	public MonitorConfiguration registerMonitor(int monitorID) {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		mapper.findAndRegisterModules();
		Proto_MonitorRequest request = Proto_MonitorRequest.newBuilder()
				.setMonitorID(monitorID)
				.build();
		Proto_RegistrationReply reply = runtimeBlockStub.registerMonitor(request);

		MonitorConfiguration ret = new MonitorConfiguration();
		ret.monitorID = monitorID;
		ret.f = reply.getF();
		ret.N = reply.getN();
		ret.isFaulty = reply.getReplicaStatus();
		ret.schedule = new LinkedList<>();
		int faultScheduleSize = reply.getFaultScheduleSize();
		System.out.printf("%d, %d, %d, %d", monitorID, reply.getF(), reply.getN(), faultScheduleSize);
		for(int i = 0; i < faultScheduleSize; i++) {
			try {
				Proto_RegistrationReply.Proto_FaultDescription proto_faultDescription = reply.getFaultSchedule(i);
				Proto_RegistrationReply.Proto_FaultDescription.Proto_TriggerConditions proto_triggerConditions = proto_faultDescription.getTriggerConditions();
				Proto_RegistrationReply.Proto_FaultDescription.Proto_FaultArguments proto_faultArguments = proto_faultDescription.getFaultArguments();

				// Rebuild TriggerConditions for current Fault
				TriggerConditions crt_triggerConditions = new TriggerConditions();
				crt_triggerConditions.setConsensus_id(proto_triggerConditions.getConsensusId());
				crt_triggerConditions.setView_id(proto_triggerConditions.getViewId());
				crt_triggerConditions.setProtocol(proto_triggerConditions.getProtocol());
				crt_triggerConditions.setProtocol_phase(proto_triggerConditions.getProtocolPhase());

				FaultArguments crt_faultArguments = null;
				if(proto_faultDescription.getFaultType().equals("DelayFault")) {
					crt_faultArguments = new DelayFaultArguments(proto_faultArguments.getFirstArg(), proto_faultArguments.getConsecutiveRounds());
				} else if (proto_faultDescription.getFaultType().equals("DropFault")) {
					crt_faultArguments = new DropFaultArguments(proto_faultArguments.getFirstArg(), proto_faultArguments.getConsecutiveRounds());
				} else if (proto_faultDescription.getFaultType().equals("DuplicateFault")) {
					crt_faultArguments = new DuplicateFaultArguments(proto_faultArguments.getFirstArg(), proto_faultArguments.getConsecutiveRounds());
				} else if (proto_faultDescription.getFaultType().equals("CrashFault")) {

				} else {
					crt_faultArguments = new FaultArguments(proto_faultArguments.getConsecutiveRounds());
				}

				//Rebuild FaultDescription for current Fault
				FaultDescription crt_faultDescription = new FaultDescription();
				crt_faultDescription.setFault_type(proto_faultDescription.getFaultType());
				crt_faultDescription.setFault_trigger_conditions(crt_triggerConditions);
				crt_faultDescription.setFault_arguments(crt_faultArguments);
				//Add FaultDescription to this monitor Schedule
				ret.schedule.add(crt_faultDescription);
			} catch (Exception e) {
				System.err.println("Error parsing Fault Description : \n" + reply.getFaultSchedule(i));
				System.err.println(e);
			}
		}
		return ret;
	}


	public static void main (String[] args) {
		try {
			CoordinatorConfiguration properties = new CoordinatorConfiguration();
			zermia.monitor.runtime.proto.ZermiaCoordinatorStub test_stub = new zermia.monitor.runtime.proto.ZermiaCoordinatorStub(properties);
			MonitorConfiguration conf0 = test_stub.registerMonitor(0);
			System.out.println(conf0);
			MonitorConfiguration conf1 = test_stub.registerMonitor(1);
			System.out.println(conf1);
			MonitorConfiguration conf2 = test_stub.registerMonitor(2);
			System.out.println(conf2);
			MonitorConfiguration conf3 = test_stub.registerMonitor(3);
			System.out.println(conf3);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
