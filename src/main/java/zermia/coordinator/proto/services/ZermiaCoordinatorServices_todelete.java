package zermia.coordinator.proto.services;

import java.util.concurrent.locks.ReentrantLock;

/*
import zermia.coordinator.ZermiaCoordinatorService.ConnectionRequest;
import zermia.coordinator.ZermiaCoordinatorService.ConnectionSecondReply;
import zermia.coordinator.ZermiaCoordinatorService.ConnectionSecondRequest;
import zermia.coordinator.ZermiaCoordinatorService.FaultActivationReply;
import zermia.coordinator.ZermiaCoordinatorService.FaultActivationRequest;
import zermia.coordinator.ZermiaCoordinatorService.Stat1SecReply;
import zermia.coordinator.ZermiaCoordinatorService.Stat1SecRequest;
import zermia.coordinator.ZermiaCoordinatorService.StatsReply;
import zermia.coordinator.ZermiaCoordinatorService.StatsRequest;
import zermia.coordinator.ZermiaCoordinatorService.clientFaultActivationReply;
import zermia.coordinator.ZermiaCoordinatorService.clientFaultActivationRequest;
import zermia.coordinator.ZermiaCoordinatorService.clientInitialReply;
import zermia.coordinator.ZermiaCoordinatorService.clientInitialRequest;
 */
import zermia.coordinator.ZermiaCoordinatorServicesGrpc.ZermiaCoordinatorServicesImplBase;
import zermia.coordinator.ZermiaCoordinatorMain;

/**
 * ServerSide implementation of ZermiaCoordinatorServices
 */
public class ZermiaCoordinatorServices_todelete extends ZermiaCoordinatorServicesImplBase{
	protected ReentrantLock z_lock = new ReentrantLock();

	public ZermiaCoordinatorServices_todelete(ZermiaCoordinatorMain coordinator) {

	}

	/*
	@Override
	public void firstConnection(ConnectionRequest request, StreamObserver<ConnectionReply> responseObserver) {
		props.loadProperties(ZermiaProperties.defautlPropertiesFilePath);
		Logger.getLogger(ZermiaCoordinatorServices.class.getName()).log(Level.INFO, "Connection Request from Replica number " + request.getReplicaID());
		
		clientList.getClient(request.getReplicaID()).setNewFileTest();
		
		if(clientList.checkClientExistence(request.getReplicaID())){
			if(clientList.getClient(request.getReplicaID()).getFaultness()) {
				ConnectionReply rep = ConnectionReply.newBuilder()
						.setReplicaStatus(true)
						.setFaultScheduleSize(clientList.getClient(request.getReplicaID()).getFaultPamList().size())
						.addAllFaultRepList(clientList.returnFaultyClientList())
						.setGroupSize(props.getNumberOfReplicas())
						.build();
				
				responseObserver.onNext(rep);
				responseObserver.onCompleted();
				Logger.getLogger(ZermiaCoordinatorServices.class.getName()).log(Level.INFO, "Connection successful to Faulty Replica number " + request.getReplicaID());
			} else {
				ConnectionReply rep = ConnectionReply.newBuilder()
						.setReplicaStatus(false) //correct replica
						.setFaultScheduleSize(0)
						.addAllFaultRepList(clientList.returnFaultyClientList())
						.setGroupSize(props.getNumberOfReplicas())
						.build();
				
				responseObserver.onNext(rep);
				responseObserver.onCompleted();
				Logger.getLogger(ZermiaCoordinatorServices.class.getName()).log(Level.INFO, "Connection successful to non-faulty Replica number " + request.getReplicaID());
			}
		}
		try {
			clientList.getClient(request.getReplicaID()).checkForExcelFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//----------------------------------------------------------------------------------//	

	@Override
	public void secondConnection(ConnectionSecondRequest request, StreamObserver<ConnectionSecondReply> responseObserver) {
	props.loadProperties(ZermiaProperties.defautlPropertiesFilePath);
	ArrayList<Boolean> arrayReply = new ArrayList<Boolean>();
	arrayReply.add(clientList.getClient(request.getReplicaID()).getVc());	//viewchange
	arrayReply.add(clientList.getClient(request.getReplicaID()).getCk());	//checkpoint
	//arrayReply.add(props.getCs());	//consensus
	
	arrayReply.add(clientList.getClient(request.getReplicaID()).getFw());	//forwarded
	
	arrayReply.add(clientList.getClient(request.getReplicaID()).getFocusPrimary()); //primary focused attack
	
	arrayReply.add(clientList.getClient(request.getReplicaID()).getClientReply()); //Client Reply
	
	arrayReply.add(clientList.getClient(request.getReplicaID()).getCsPrP()); //consensus pre-prepare
	arrayReply.add(clientList.getClient(request.getReplicaID()).getCsPr()); //consensus prepare
	arrayReply.add(clientList.getClient(request.getReplicaID()).getCsCm()); //consensus commit
	
	arrayReply.add(clientList.getClient(request.getReplicaID()).getVcStop()); //first part view
	arrayReply.add(clientList.getClient(request.getReplicaID()).getVcStopD()); //second part view
	arrayReply.add(clientList.getClient(request.getReplicaID()).getVcSync()); //third part view
	
	System.out.println(arrayReply);
	
	ConnectionSecondReply rep = ConnectionSecondReply.newBuilder()
			.addAllMessageTypes(arrayReply)
			.build();
	
	responseObserver.onNext(rep);
	responseObserver.onCompleted();
	}
	
	
//----------------------------------------------------------------------------------//
	
	@Override
	public void faultService(FaultActivationRequest request, StreamObserver<FaultActivationReply> responseObserver) {
		Logger.getLogger(ZermiaCoordinatorServices.class.getName()).log(Level.INFO, "Fault ACTIVATION Request from runtime Replica : " + request.getReplicaID());

		ArrayList<String> fault_PamList = new ArrayList<String>();
		ArrayList<String> run_TriggerList = new ArrayList<String>();
		
		fault_PamList = clientList.getClient(request.getReplicaID()).getFaultPamList().get(request.getFaultScheduleIterator());
		run_TriggerList = clientList.getClient(request.getReplicaID()).getRunsTriggerList().get(request.getFaultScheduleIterator());
				
		FaultActivationReply runFaultAct = FaultActivationReply.newBuilder()
				.addAllFaultPam(fault_PamList)
				.addAllRunTrigger(run_TriggerList)
				.build();
				
		responseObserver.onNext(runFaultAct);
		responseObserver.onCompleted();	
		}
	
//----------------------------------------------------------------------------------//
	
	@Override
	public void statsService(StatsRequest request, StreamObserver<StatsReply> responseObserver) {
		props.loadProperties(ZermiaProperties.defautlPropertiesFilePath);
		repListSize = props.getNumberOfReplicas();
		clientList.getClient(request.getReplicaID()).setMessagesSentTotal(request.getMessageTotal());
		clientList.getClient(request.getReplicaID()).setTimeFinish(request.getTimeFinal());
		
		StatsReply sReply = StatsReply.newBuilder()
				.build();
		
		responseObserver.onNext(sReply);
		responseObserver.onCompleted();
		
		z_lock.lock();	
		zStats = new ZermiaStats();
		
		try {
			zStats.checkForExcelFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		replicaStats.put(Integer.parseInt(request.getReplicaID()), zStats.calculateAll2(request.getReplicaID()));
		
//		try {
//			replicaList.getReplica(request.getReplicaID()).fillExcelFile();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		if(repListSize <= replicaStats.size() ) {
			for(int k = 0; k<replicaStats.size();k++) {
				Logger.getLogger(ZermiaCoordinatorMain.class.getName()).log(Level.INFO, "------------- Replica " + k + " END OF TEST STATS --------------");
				
				System.out.println("Replica " + k + " END TIME : " + replicaStats.get(k).get(0) + " seconds");
				System.out.println("Replica " + k + " THROUGHPUT : " + replicaStats.get(k).get(1) + " messages per second");
				System.out.println("Replica " + k + " total messages : " + replicaStats.get(k).get(2) + " messages");
				System.out.println("Replica " + k + " average latency: " + replicaStats.get(k).get(3) + " millisenconds");
				
				try {
					zStats.fillExcelFile(k,replicaStats.get(k).get(0), replicaStats.get(k).get(2), replicaStats.get(k).get(1),replicaStats.get(k).get(3));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			try {
				zStats.fillExcelTestSeparator();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			replicaStats.clear();
		}
		z_lock.unlock();
	}
	
	
//----------------------------------------------------------------------------------//	
	@Override
	public void stat1SecService(Stat1SecRequest request, StreamObserver<Stat1SecReply> responseObserver) {
		
		clientList.getClient(request.getReplicaID()).getTP1secList().put(request.getMessage1Sec(), request.getMessage1Throughput());

		try {
			clientList.getClient(request.getReplicaID()).fillExcelFile2(request.getMessage1Sec(), request.getMessage1Throughput());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Stat1SecReply rep = Stat1SecReply.newBuilder()
				.setTeste("0")
				.build();
		
		responseObserver.onNext(rep);
		responseObserver.onCompleted();	
	}

	
	
//----------------------------------------------------------------------------------//	
	@Override
	public void clientFirstRequest(clientInitialRequest request, StreamObserver<clientInitialReply> responseObserver) {
		Logger.getLogger(ZermiaCoordinatorServices.class.getName()).log(Level.INFO, "Faulty Client Request received");
		
		clientInitialReply rep = clientInitialReply.newBuilder()
					.setFaultyClientGroupSize(clientList.highestNumber()/100000)
					.setFaultScheduleSize(clientList.getClient(String.valueOf(clientList.highestNumber())).getFaultPamList().size())
					.addAllFaultRepList(clientList.returnFaultyClientList())
					.setGroupSizeReplicas(props.getNumberOfReplicas())
					.addAllRepList(clientList.returnAllClientsList())
					.build();
			
			responseObserver.onNext(rep);
			responseObserver.onCompleted();
			Logger.getLogger(ZermiaCoordinatorServices.class.getName()).log(Level.INFO, "Connection successful to Client");
		
		
	}
	
//----------------------------------------------------------------------------------//	

	@Override
	public void clientFaultService(clientFaultActivationRequest request, StreamObserver<clientFaultActivationReply> responseObserver) {

		Logger.getLogger(ZermiaCoordinatorServices.class.getName()).log(Level.INFO, "Fault ACTIVATION Requested from Clients");

		ArrayList<String> fault_PamList1 = new ArrayList<String>();
		ArrayList<String> run_TriggerList1 = new ArrayList<String>();
		
		fault_PamList1 = clientList.getClient(String.valueOf(clientList.highestNumber())).getFaultPamList().get(request.getFaultScheduleIterator());
		run_TriggerList1 = clientList.getClient(String.valueOf(clientList.highestNumber())).getRunsTriggerList().get(request.getFaultScheduleIterator());
				
		clientFaultActivationReply runFaultAct = clientFaultActivationReply.newBuilder()
				.addAllFaultPam(fault_PamList1)
				.addAllRunTrigger(run_TriggerList1)
				.build();
				
		responseObserver.onNext(runFaultAct);
		responseObserver.onCompleted();	
		
	}
	*/
//----------------------------------------------------------------------------------//
	

}
	

