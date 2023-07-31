package zermia.coordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.protobuf.ByteString;
import zermia.common.schedule.*;
import zermia.common.schedule.arguments.DelayFaultArguments;
import zermia.common.schedule.arguments.DifferentRequestsToAllArguments;
import zermia.common.schedule.arguments.DropFaultArguments;
import zermia.common.schedule.arguments.DuplicateFaultArguments;
import zermia.common.schedule.arguments.serializer.FaultArgumentsDeserializer;
import zermia.coordinator.clients.Client;
import zermia.coordinator.config.CoordinatorConfiguration;
import zermia.coordinator.proto.services.ZermiaCoordinatorServices_todelete;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import zermia.coordinator.ZermiaCoordinatorService.Proto_MonitorRequest;
import zermia.coordinator.ZermiaCoordinatorService.Proto_RegistrationReply;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZermiaCoordinatorMain extends ZermiaCoordinatorServicesGrpc.ZermiaCoordinatorServicesImplBase {
    public static final CoordinatorConfiguration props = new CoordinatorConfiguration();
    private Server zermiaServer;
    private Integer closeServer;
    private GlobalFaultSchedule globalFaultSchedule;
    private HashMap<String, Client> zermiaClients = new HashMap<>(); // Monitors

    private ZermiaCoordinatorMain() {
        props.loadProperties(CoordinatorConfiguration.defaultPropertiesFilePath);    //properties file loader
        closeServer = props.getServerUptime();                                       //server uptime in secs
    }

    public ZermiaCoordinatorMain(GlobalFaultSchedule schedule) {
        this();
        init(schedule);
        initServer();
    }

    private void init(GlobalFaultSchedule schedule) {
        for(MonitorFaultSchedule md : schedule.getSchedule()) {
            if(zermiaClients.containsKey("Monitor"+md.getMonitor_id())) {
                System.out.println("Fault Schedule Error:");
                System.out.println("\t Multiple monitors with ID: " + md.getMonitor_id());
                System.out.println("\t Monitors must have distinct IDs!");
                System.out.println("Exiting!");
                System.exit(-11);
            }
            Client clt = new Client(md.getMonitor_id());
            clt.setFaultSchedule(md);
            zermiaClients.put("Monitor"+md.getMonitor_id(), clt);
        }
    }

    private void initServer() {
        zermiaServer = ServerBuilder.forPort(props.getCoordinatorPort())
            .addService(this)
            .build();
    }

    public Server getZermiaServer() {
        return zermiaServer;
    }

    @Override
    public void registerMonitor(Proto_MonitorRequest request, StreamObserver<Proto_RegistrationReply> responseObserver) {
        Logger.getLogger(ZermiaCoordinatorServices_todelete.class.getName()).log(Level.INFO, "Connection Request from Replica number " + request.getMonitorID());

        int monitorID = request.getMonitorID();
        Client c = zermiaClients.get("Monitor"+monitorID);
        List<FaultDescription> schedule = c.getFaultSchedule().getFault_schedule();

        Proto_RegistrationReply.Builder replyBuilder = Proto_RegistrationReply.newBuilder()
                .setF(props.getF())
                .setN(props.getN())
                .setReplicaStatus(true)
                .setFaultScheduleSize(schedule.size());
        System.out.printf("%d, %d, %d, %d\n", monitorID, props.getF(), props.getN(), schedule.size());
        for (int i = 0;i < schedule.size(); i++) {
            FaultDescription crt = schedule.get(i);
            TriggerConditions crt_triggerConditions = crt.getFault_trigger_conditions();
            Proto_RegistrationReply.Proto_FaultDescription.Proto_TriggerConditions.Builder protoTriggerConditions = Proto_RegistrationReply.Proto_FaultDescription.Proto_TriggerConditions.newBuilder()
                    .setConsensusId(crt_triggerConditions.getConsensus_id())
                    .setViewId(crt_triggerConditions.getView_id());

            if (crt_triggerConditions.getProtocol() == null)
                protoTriggerConditions.setProtocol("null");
            else
                protoTriggerConditions.setProtocol(crt_triggerConditions.getProtocol());

            if (crt_triggerConditions.getProtocol_phase() == null)
                protoTriggerConditions.setProtocolPhase("null");
            else
                protoTriggerConditions.setProtocolPhase(crt_triggerConditions.getProtocol_phase());


            if (crt.getFault_type().equals("DelayFault")) {
                DelayFaultArguments dfa = (DelayFaultArguments) crt.getFault_arguments();
                Proto_RegistrationReply.Proto_FaultDescription.Proto_FaultArguments.Builder protoFaultArguments = Proto_RegistrationReply.Proto_FaultDescription.Proto_FaultArguments.newBuilder()
                        .setConsecutiveRounds(dfa.getConsecutive_rounds())
                        .setSecondArg(dfa.getDelayDuration());
                Proto_RegistrationReply.Proto_FaultDescription.Builder faultDecription_builder = Proto_RegistrationReply.Proto_FaultDescription.newBuilder()
                        .setFaultType(crt.getFault_type())
                        .setTriggerConditions(protoTriggerConditions)
                        .setFaultArguments(protoFaultArguments);

                replyBuilder.addFaultSchedule(faultDecription_builder);
            } else if (crt.getFault_type().equals("DropFault")) {
                DropFaultArguments dfa = (DropFaultArguments) crt.getFault_arguments();
                Proto_RegistrationReply.Proto_FaultDescription.Proto_FaultArguments.Builder protoFaultArguments = Proto_RegistrationReply.Proto_FaultDescription.Proto_FaultArguments.newBuilder()
                        .setConsecutiveRounds(dfa.getConsecutive_rounds())
                        .setSecondArg(dfa.getTo());
                Proto_RegistrationReply.Proto_FaultDescription.Builder faultDecription_builder = Proto_RegistrationReply.Proto_FaultDescription.newBuilder()
                        .setFaultType(crt.getFault_type())
                        .setTriggerConditions(protoTriggerConditions)
                        .setFaultArguments(protoFaultArguments);

                replyBuilder.addFaultSchedule(faultDecription_builder);
            } else if (crt.getFault_type().equals("DuplicateFault")) {
                DuplicateFaultArguments dfa = (DuplicateFaultArguments) crt.getFault_arguments();
                Proto_RegistrationReply.Proto_FaultDescription.Proto_FaultArguments.Builder protoFaultArguments = Proto_RegistrationReply.Proto_FaultDescription.Proto_FaultArguments.newBuilder()
                        .setConsecutiveRounds(dfa.getConsecutive_rounds())
                        .setSecondArg(dfa.getTo());
                Proto_RegistrationReply.Proto_FaultDescription.Builder faultDecription_builder = Proto_RegistrationReply.Proto_FaultDescription.newBuilder()
                        .setFaultType(crt.getFault_type())
                        .setTriggerConditions(protoTriggerConditions)
                        .setFaultArguments(protoFaultArguments);

                replyBuilder.addFaultSchedule(faultDecription_builder);
            } else if (crt.getFault_type().equals("CrashFault")) {
                Proto_RegistrationReply.Proto_FaultDescription.Builder faultDecription_builder = Proto_RegistrationReply.Proto_FaultDescription.newBuilder()
                        .setFaultType(crt.getFault_type())
                        .setTriggerConditions(protoTriggerConditions);

                replyBuilder.addFaultSchedule(faultDecription_builder);
            } else if (crt.getFault_type().equals("DifferentRequestsToAll")) {
                DifferentRequestsToAllArguments drtafa = (DifferentRequestsToAllArguments) crt.getFault_arguments();
                Proto_RegistrationReply.Proto_FaultDescription.Proto_FaultArguments.Builder protoFaultArguments = Proto_RegistrationReply.Proto_FaultDescription.Proto_FaultArguments.newBuilder()
                        .setConsecutiveRounds(drtafa.getConsecutive_rounds())
                        .setSecondArg(drtafa.getTimestamp())
                        .setThirdArg(ByteString.copyFrom(drtafa.getOperation()));
                Proto_RegistrationReply.Proto_FaultDescription.Builder faultDecription_builder = Proto_RegistrationReply.Proto_FaultDescription.newBuilder()
                        .setFaultType(crt.getFault_type())
                        .setTriggerConditions(protoTriggerConditions)
                        .setFaultArguments(protoFaultArguments);

                replyBuilder.addFaultSchedule(faultDecription_builder);
            } else {
                FaultArguments fa = crt.getFault_arguments();
                Proto_RegistrationReply.Proto_FaultDescription.Proto_FaultArguments.Builder protoFaultArguments = Proto_RegistrationReply.Proto_FaultDescription.Proto_FaultArguments.newBuilder()
                        .setConsecutiveRounds(fa.getConsecutive_rounds());
                Proto_RegistrationReply.Proto_FaultDescription.Builder faultDecription_builder = Proto_RegistrationReply.Proto_FaultDescription.newBuilder()
                        .setFaultType(crt.getFault_type())
                        .setTriggerConditions(protoTriggerConditions)
                        .setFaultArguments(protoFaultArguments);
                replyBuilder.addFaultSchedule(faultDecription_builder);
            }
        }

        Proto_RegistrationReply reply = replyBuilder.build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    public static GlobalFaultSchedule parseSchedule(File scheduleFile) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(FaultArguments.class, new FaultArgumentsDeserializer());
        mapper.registerModule(module);
        GlobalFaultSchedule schedule = null;
        try {
            schedule = mapper.readValue(scheduleFile, GlobalFaultSchedule.class);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error reading schedule file : " + scheduleFile.getName());
            System.err.println(e);
        }
        return schedule;
    }

    //----------------------------------------------------------------------------------//
    //main
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Invalid number of arguments");
            System.out.println();
            System.out.println("Usage: java ZermiaCoordinatorMain schedule_file_path [config_file_path]");
            System.out.println();
            System.exit(-1);
        }

        File scheduleFile = new File(System.getProperty("user.dir"), args[0]);
        System.out.printf("[ZermiaCoordinatorMain] schedule file: %s\n", scheduleFile);

        if(!scheduleFile.exists()) {
            System.out.println("Unable to open schedule file: " + args[0]);
            System.out.println();
            System.out.println("Usage: java ZermiaCoordinatorMain schedule_file_path [config_file_path]");
            System.out.println();
            System.exit(-1);
        }

        GlobalFaultSchedule schedule = parseSchedule(scheduleFile);
        System.out.printf("[ZermiaCoordinatorMain] global schedule: \n \t %s \n", schedule);
        ZermiaCoordinatorMain coordinator = new ZermiaCoordinatorMain(schedule);

        try {
            coordinator.getZermiaServer().start(); //zermia server start
            Logger.getLogger(ZermiaCoordinatorMain.class.getName()).log(Level.INFO, "Starting Coordinator Services in port : " + coordinator.getZermiaServer().getPort());
            coordinator.getZermiaServer().awaitTermination(coordinator.closeServer, TimeUnit.SECONDS);
            Logger.getLogger(ZermiaCoordinatorMain.class.getName()).log(Level.INFO, "Coordinator Services shutting down after " + coordinator.closeServer + " seconds");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Logger.getLogger(ZermiaCoordinatorMain.class.getName()).log(Level.INFO, "Coordinator exiting");
    }
}

