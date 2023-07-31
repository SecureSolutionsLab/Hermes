package zermia.hooks.bftsmart;

import bftsmart.communication.SystemMessage;
import bftsmart.communication.client.netty.NettyClientServerCommunicationSystemClientSide;
import bftsmart.communication.client.netty.NettyClientServerCommunicationSystemServerSide;
import bftsmart.communication.server.ServerConnection;
import bftsmart.communication.server.ServersCommunicationLayer;
import bftsmart.consensus.messages.ConsensusMessage;
import bftsmart.statemanagement.SMMessage;
import bftsmart.tom.core.messages.ForwardedMessage;
import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.leaderchange.LCManager;
import bftsmart.tom.leaderchange.LCMessage;
import bftsmart.tom.util.TOMUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import zermia.common.schedule.FaultArguments;
import zermia.common.schedule.FaultDescription;
import zermia.common.schedule.arguments.DelayFaultArguments;
import zermia.common.schedule.arguments.DifferentRequestsToAllArguments;
import zermia.common.schedule.arguments.DropFaultArguments;
import zermia.common.schedule.arguments.DuplicateFaultArguments;
import zermia.monitor.state.bftsmart.*;
import zermia.monitor.state.ReplicaState;
import zermia.monitor.state.ClientState;
import zermia.monitor.state.NodeState;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

@Aspect
public class ZermiaMonitorHooks {
    class threadLoad extends Thread {  //needed for fault load, just throw anything to make some cpu waste
        public void run() {
            while (true) {
                int wasting = 2;
                int twaste = 1024 * 1024 * 1024;
                while (wasting < twaste) {
                    wasting = wasting * 2;
                }
            }
        }
    }

    private int monitorID;
    private zermia.monitor.runtime.bftsmart.BFTSmartMonitor monitor = null;
    private boolean isReplica; // is this monitor associated with a Replica or a BFTClient?
    private int crtLeader=-1;

    @Before("execution (* bftsmart.demo.counter.CounterClient.main*(..))")
    public void advice_clt_main(JoinPoint joinPoint) {
        String[] replicaArgs = (String[]) joinPoint.getArgs()[0]; //get arguments
        monitorID = Integer.parseInt(replicaArgs[0]); //get replica id
        monitor = new zermia.monitor.runtime.bftsmart.BFTSmartMonitor(monitorID);
        isReplica = false;
    }

    @Before("execution (* bftsmart.demo.counter.BFTCounterServer.main*(..))")
    public void advice_rep_main(JoinPoint joinPoint) {
        String[] replicaArgs = (String[]) joinPoint.getArgs()[0]; //get arguments
        monitorID = Integer.parseInt(replicaArgs[0]); //get replica id
        monitor = new zermia.monitor.runtime.bftsmart.BFTSmartMonitor(monitorID);
        isReplica = true;
    }

    private void updateMonitorState(SystemMessage sysMsg, NodeState ms) {
        if (sysMsg instanceof SMMessage) { // State Management Message
            SMMessage smmessage = (SMMessage) sysMsg;
            BFTSmartReplicaState state = (BFTSmartReplicaState) ms;
//            System.out.printf("[ZermiaMonitorHooks] \t SMMessage: from, %d\n", smmessage.getSender());
            String msgProtocol = "SM";
            String msgProtocolPhase = "";
            switch (smmessage.getType()) {
                case TOMUtil.SM_REQUEST:
//                    System.out.printf("[ZermiaMonitorHooks] \t SMMessage: type, %s\n", "SM_REQUEST");
                    msgProtocolPhase = "SM_REQUEST";
                    break;
                case TOMUtil.SM_REPLY:
//                    System.out.printf("[ZermiaMonitorHooks] \t SMMessage: type, %s\n", "SM_REPLY");
                    msgProtocolPhase = "SM_REPLY";
                    break;
                case TOMUtil.SM_ASK_INITIAL:
//                    System.out.printf("[ZermiaMonitorHooks] \t SMMessage: type, %s\n", "SM_ASK_INITIAL");
                    msgProtocolPhase = "SM_ASK_INITIAL";
                    break;
                case TOMUtil.SM_REPLY_INITIAL:
//                    System.out.printf("[ZermiaMonitorHooks] \t SMMessage: type, %s\n", "SM_REPLY_INITIAL");
                    msgProtocolPhase = "SM_REPLY_INITIAL";
                    break;
                default:
                    System.out.printf("[ZermiaMonitorHooks] \t SMMessage: type, %d\n", smmessage.getType());
                    break;
            }

            // TODO: verify this logic (only update if message CID greater than current state CID)
            if (
                    state.getCrtProtocolPhase().equals("")
                            ||
                            ((state.getCrtProtocolPhase().equals("SM_REQUEST") || state.getCrtProtocolPhase().equals("SM_ASK_INITIAL"))
                                    &&
                                    (smmessage.getType() == TOMUtil.SM_REPLY || smmessage.getType() == TOMUtil.SM_REPLY_INITIAL))
            ) {
                state.setCrtConsensusID(smmessage.getCID());
                if (smmessage.getState() != null) {
//                System.out.printf("[ZermiaMonitorHooks] \t SMMessage: state.getLastCID, %d\n", smmessage.getState().getLastCID());
                    state.setLastConsensusID(smmessage.getState().getLastCID());
                } else {
//                System.out.printf("[ZermiaMonitorHooks] \t SMMessage: state.getLastCID, %d\n", -1);
                    state.setLastConsensusID(-1);
                }
                if (smmessage.getView() != null) {
//                System.out.printf("[ZermiaMonitorHooks] \t SMMessage: view.getID, %d\n", smmessage.getView().getId());
                    state.setCrtViewID(smmessage.getView().getId());
                } else {
//                System.out.printf("[ZermiaMonitorHooks] \t SMMessage: view.getID, %d\n", -1);
                    state.setCrtViewID(-1);
                }
//            System.out.printf("[ZermiaMonitorHooks] \t SMMessage: cid, %d\n", smmessage.getCID());
//            System.out.printf("[ZermiaMonitorHooks] \t SMMessage: regency, %d\n", smmessage.getRegency());
//            System.out.printf("[ZermiaMonitorHooks] \t SMMessage: leader, %d\n", smmessage.getLeader());
                state.setCrtProtocol(msgProtocol);
                state.setCrtProtocolPhase(msgProtocolPhase);
                state.setRegency(smmessage.getRegency());
                state.setLeader(smmessage.getLeader());
            }

//            private ApplicationState state; // State log
//            private View view;
//            private int cid; // Consensus ID up to which the sender needs to be updated
//            private int type; // Message type
//            private int regency; // Current regency
//            private int leader; // Current leader
//            public final boolean TRIGGER_SM_LOCALLY; // indicates that the replica should. initiate the SM protocol locally

        } else if (sysMsg instanceof TOMMessage) { // Total Order Multicast Message
            TOMMessage tomMessage = (TOMMessage) sysMsg;

            String protocolPhase = "";
            switch(tomMessage.getReqType()) {
                case ORDERED_REQUEST: {
                    protocolPhase = "ORDERED_REQUEST";
                    break;
                }
                case UNORDERED_REQUEST: {
                    protocolPhase = "ORDERED_REQUEST";
                    break;
                }
                case REPLY: {
                    protocolPhase = "REPLY";
                    break;
                }
                case RECONFIG: {
                    protocolPhase = "RECONFIG";
                    break;
                }
                case ASK_STATUS: {
                    protocolPhase = "ASK_STATUS";
                    break;
                }
                case STATUS_REPLY: {
                    protocolPhase = "STATUS_REPLY";
                    break;
                }
                case UNORDERED_HASHED_REQUEST: {
                    protocolPhase = "UNORDERED_HASHED_REQUEST";
                    break;
                }
                default: {

                }
            }

//            System.out.printf("[ZermiaMonitorHooks] \t TOMMessage: from, %d\n", tomMessage.getSender());
//            System.out.printf("[ZermiaMonitorHooks] \t TOMMessage: viewID, %d\n", tomMessage.getViewID());
//            System.out.printf("[ZermiaMonitorHooks] \t TOMMessage: session, %d\n", tomMessage.getSession());
//            System.out.printf("[ZermiaMonitorHooks] \t TOMMessage: sequence, %d\n", tomMessage.getSequence());
//            System.out.printf("[ZermiaMonitorHooks] \t TOMMessage: operationId, %d\n", tomMessage.getOperationId());
//            System.out.printf("[ZermiaMonitorHooks] \t TOMMessage: id, %d\n", tomMessage.getId());
//            if(tomMessage.reply != null) {
//                System.out.printf("[ZermiaMonitorHooks] \t TOMMessage: reply, %s\n", tomMessage.reply.toString());
//            }
//            else
//                System.out.printf("[ZermiaMonitorHooks] \t TOMMessage: reply, %s\n", "NULL");
            if (ms instanceof BFTSmartClientState) { // only executed by ReplicaMonitor
                BFTSmartClientState state = (BFTSmartClientState) ms;
                state.setViewID(tomMessage.getViewID());
                state.setSequenceID(tomMessage.getSequence());
                state.setOperationID(tomMessage.getOperationId());
                state.setLastMessageID(tomMessage.getId());
                state.setProtocolPhase(protocolPhase);
            } else if (ms instanceof BFTSmartReplicaState) { // only executed by ClientMonitor
                BFTSmartReplicaState state = (BFTSmartReplicaState) ms;
                if (tomMessage.getSequence() > state.getCrtConsensusID()) {
                    int cid = state.getCrtConsensusID();
                    state.setLastConsensusID(cid);
                    state.setCrtConsensusID(tomMessage.getSequence());
                    state.setCrtViewID(tomMessage.getViewID());
                    state.setCrtProtocol("CONSENSUS");
                    state.setCrtProtocolPhase(protocolPhase);
                }
            }

//            private int viewID; //current sender view
//            private TOMMessageType type; // request type: application or reconfiguration request
//            private int session; // Sequence number defined by the client
//            // Sequence number defined by the client.
//            // There is a sequence number for ordered and anothre for unordered messages
//            private int sequence;
//            private int operationId; // Sequence number defined by the client
//            private byte[] content = null; // Content of the message
//            //the fields bellow are not serialized!!!
//            private transient int id; // ID for this message. It should be unique

        } else if (sysMsg instanceof LCMessage) { // Leader Change Message
            // TODO: implement leader change monitoring
            LCMessage lcmessage = (LCMessage) sysMsg;
            String phase = null;
            switch (lcmessage.getType()) {
                case TOMUtil.STOP:
                    phase = "STOP";
                    break;
                case TOMUtil.STOPDATA:
                    phase = "STOPDATA";
                    break;
                case TOMUtil.SYNC:
                    phase = "SYNC";
                    ((BFTSmartReplicaState) ms).setLeader(lcmessage.getSender());
                    break;
                default:
                    phase = "LOCAL";
                    break;
            }
//            System.out.printf("[ZermiaMonitorHooks] \t LCMessage: from, %d\n", lcmessage.getSender());
//          System.out.printf("[ZermiaMonitorHooks] \t LCMessage: phase, %s\n", phase);
//            System.out.printf("[ZermiaMonitorHooks] \t LCMessage: reg, %d\n", lcmessage.getReg());
            ((BFTSmartReplicaState) ms).setRegency(lcmessage.getReg());
            ((BFTSmartReplicaState) ms).setCrtProtocol("LC");
            ((BFTSmartReplicaState) ms).setCrtProtocolPhase(phase);

//            System.out.printf("[ZermiaMonitorHooks] \t LCMessage: payload.length, %d\n", lcmessage.getPayload().length);
//            private int phase;
//            private int ts;
//            private byte[] payload;
//            public final boolean TRIGGER_LC_LOCALLY; // indicates that the replica should. initiate the LC protocol locally

        } else if (sysMsg instanceof ForwardedMessage) { // Message used to forward a client request to the current leader when the first timeout for this request is triggered
            // TODO: implement this
            ForwardedMessage fwmessage = (ForwardedMessage) sysMsg;
            System.out.printf("[ZermiaMonitorHooks] \t ForwardedMessage: from, %d\n", fwmessage.getSender());
            System.out.printf("[ZermiaMonitorHooks] \t ForwardedMessage: request, %s\n", fwmessage.getRequest());
//            private TOMMessage request;
        } else if (sysMsg instanceof ConsensusMessage) { // Message used in an epoch of a consensus instance
            ConsensusMessage cmessage = (ConsensusMessage) sysMsg;
//            System.out.printf("[ZermiaMonitorHooks] \t ConsensusMessage: from, %d\n", cmessage.getSender());
//            System.out.printf("[ZermiaMonitorHooks] \t ConsensusMessage: message number, %d\n", cmessage.getNumber());
//            System.out.printf("[ZermiaMonitorHooks] \t ConsensusMessage: crt epoch, %d\n", cmessage.getEpoch());
//            System.out.printf("[ZermiaMonitorHooks] \t ConsensusMessage: paxosType, %s\n", cmessage.getPaxosVerboseType());
//            System.out.printf("[ZermiaMonitorHooks] \t ConsensusMessage: value.length, %d\n", cmessage.getValue().length);
//            System.out.printf("[ZermiaMonitorHooks] \t ConsensusMessage: proof, %s\n", cmessage.getProof());

            BFTSmartReplicaState state = (BFTSmartReplicaState) ms;
            // TODO: verify this logic (only update if message CID greater than current state CID)
            if (
                    (!state.getCrtProtocol().equals("CONSENSUS"))
                            ||
                            (state.getCrtProtocol().equals("CONSENSUS") && (state.getCrtConsensusID() < cmessage.getNumber() || !state.getCrtProtocolPhase().equals(cmessage.getPaxosVerboseType())))

            ) {
                int cid = state.getCrtConsensusID();
                state.setLastConsensusID(cid);
                state.setCrtConsensusID(cmessage.getNumber());
                state.setCrtViewID(cmessage.getEpoch());
                state.setCrtProtocol("CONSENSUS");
                state.setCrtProtocolPhase(cmessage.getPaxosVerboseType());
            }
//            private int number; //consensus ID for this message
//            private int epoch; // Epoch to which this message belongs to
//            private int paxosType; // Message type
//            private byte[] value = null; // Value used when message type is PROPOSE
//            private Object proof; // Proof used when message type is COLLECT.- Can be either a MAC vector or a signature
        } else {
            System.out.printf("[ZermiaMonitorHooks] \t ?????Message: from %d\n", sysMsg.getSender());
            System.out.printf("[ZermiaMonitorHooks] \t ?????Message: %s\n", sysMsg.getClass().getCanonicalName());
        }

//        System.out.printf("[ZermiaMonitorHooks] \t State [node%d]: %s\n\n", ms.getId(), ms);
        //System.out.printf("[ZermiaMonitorHooks] \t AppState [node%d]: %s\n", monitorID, monitor.getCrtTargetAppState());

    }

    /**
     * Client Sent Request to replicas
     * Only Called on a Client Monitor
     * @param joinPoint - join point for this advice
     * @throws Throwable
     */
    // bftsmart.communication.client.netty.NettyClientServerCommunicationSystemClientSide.send(boolean sign, int[] targets, TOMMessage sm)
    @Around("execution (* bftsmart.communication.client.netty.NettyClientServerCommunicationSystemClientSide.send*(..))")
    public void advice_c0(ProceedingJoinPoint joinPoint) throws Throwable {
        NettyClientServerCommunicationSystemClientSide _this = (NettyClientServerCommunicationSystemClientSide) joinPoint.getThis();
        Object[] args = joinPoint.getArgs();
        boolean sign = (boolean) args[0];
        int[] targets = (int[]) args[1];
        TOMMessage tommessage = (TOMMessage) args[2];

        String to = "";
        for (int i = 0; i < targets.length; i++) {
            to += targets[i];
            if (i < (targets.length - 1))
                to += ", ";
        }
        SystemMessage sysMsg = (SystemMessage) args[2];
        System.out.println("[ZermiaMonitorHooks] advice_c0 bftsmart.communication.client.netty.NettyClientServerCommunicationSystemClientSide.send - from: " + _this.getClientId() + "/" + sysMsg.getSender() + " to: " + to);

        ClientState state = monitor.getClientState(sysMsg.getSender());
        if (state == null) {
            state = new BFTSmartClientState(sysMsg.getSender());
            monitor.addClientState(state);
        }
        updateMonitorState(sysMsg, state);

        FaultDescription fd = this.monitor.getNextFault2Inject();
        if(fd != null) {
            FaultArguments fa = (FaultArguments) fd.getFault_arguments();
            ((BFTSmartClientState)state).setCrtPrimary(0);
            System.out.printf("\t [ZermiaMonitorHooks] Triggering %s:\n", fd.getFault_type());
            System.out.printf("\t [ZermiaMonitorHooks] Triggering Fault: %s \n", fd);
            System.out.printf("\t [ZermiaMonitorHooks] Current State: %s \n", state);
            switch (fd.getFault_type()) {
                case "RequestPrimaryOnly": {
                    try {
                        args[1] = new int[]{((BFTSmartClientState)state).getCrtPrimary()};
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "RequestSecondariesOnly": {
                    try {
                        int crtPrimary = ((BFTSmartClientState)state).getCrtPrimary();
                        int[] newTarget = new int[targets.length-1];
                        int j = 0;
                        for (int i = 0; i < targets.length; i++) {
                            if (targets[i] == crtPrimary)
                                continue;
                            newTarget[j] = targets[i];
                            j++;
                        }

                        args[1] = newTarget;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "DifferentRequestsToAll": {
                    DifferentRequestsToAllArguments drtafa = (DifferentRequestsToAllArguments) fd.getFault_arguments();

                    int quorum;

                    Integer[] targetArray = Arrays.stream(targets).boxed().toArray(Integer[]::new);
                    Collections.shuffle(Arrays.asList(targetArray), new Random());

                    if (_this.controller.getStaticConf().isBFT()) {
                        quorum = (int) Math.ceil((_this.controller.getCurrentViewN() + _this.controller.getCurrentViewF()) / 2) + 1;
                    } else {
                        quorum = (int) Math.ceil((_this.controller.getCurrentViewN()) / 2) + 1;
                    }
                    _this.listener.waitForChannels(quorum); // wait for the previous transmission to complete
                    _this.logger.debug("Sending request from " + tommessage.getSender() + " with sequence number " + tommessage.getSequence() + " to "
                            + Arrays.toString(targetArray));
                    int sent = 0;

                    for (int target : targetArray) {
                        TOMMessage sm_c = (TOMMessage) tommessage.clone();
                        int length = drtafa.getOperation().length;
                        byte[] newContents = drtafa.getOperation();
                        sm_c.content[length-1] += newContents[length-1] + target;
                        //sm_c.sequence -= target; //makes no sense changing as it will lead to dropping message
                        //sm_c.operationId -= target; //makes no sense changing as it will lead to dropping message
                        sm_c.session += target;
                        if (sm_c.serializedMessage == null) {
                            // serialize message
                            DataOutputStream dos = null;
                            try {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                dos = new DataOutputStream(baos);
                                sm_c.wExternal(dos);
                                dos.flush();
                                sm_c.serializedMessage = baos.toByteArray();
                            } catch (IOException ex) {
                                _this.logger.debug("Impossible to serialize message: " + tommessage);
                            }
                        }

                        // Logger.println("Sending message with "+sm.serializedMessage.length+" bytes of
                        // content.");

                        // produce signature
                        if (sign && sm_c.serializedMessageSignature == null) {
                            sm_c.serializedMessageSignature = _this.signMessage(_this.privKey, sm_c.serializedMessage);
                        }

                        // This is done to avoid a race condition with the writeAndFush method. Since
                        // the method is asynchronous,
                        // each iteration of this loop could overwrite the destination of the previous
                        // one
                        try {
                            TOMMessage sm = (TOMMessage) sm_c.clone();
                        //Modified JSoares (targets[target] it does not make sense since targetArray was already shuffled)
    //			sm.destination = targets[target];
                            sm.destination = target;

                            _this.rl.readLock().lock();
                            //Modified JSoares (targets[target] it does not make sense since targetArray was already shuffled)
    //			Channel channel = sessionClientToReplica.get(targets[target]).getChannel();
                            Channel channel = _this.sessionClientToReplica.get(target).getChannel();
                            _this.rl.readLock().unlock();
                            if (channel.isActive()) {
                                sm.signed = sign;
                                ChannelFuture f = channel.writeAndFlush(sm);

                                f.addListener(_this.listener);

                                sent++;
                            } else {
                                //Modified JSoares (targets[target] it does not make sense since targetArray was already shuffled)
    //				logger.debug("Channel to " + targets[target] + " is not connected");
                                _this.logger.debug("Channel to " + target + " is not connected");
                            }
                        } catch (CloneNotSupportedException e) {
                            _this.logger.error("Failed to clone TOMMessage", e);
                            continue;
                        }

                    }

                    if (targets.length > _this.controller.getCurrentViewF() && sent < _this.controller.getCurrentViewF() + 1) {
                        // if less than f+1 servers are connected send an exception to the client
                        throw new RuntimeException("Impossible to connect to servers!");
                    }
                    if (targets.length == 1 && sent == 0)
                        throw new RuntimeException("Server not connected");
                }
                return;
            }
        }

        joinPoint.proceed(args);
        //int nodeID = sysMsg.getSender();
        //zermia.monitor.runtime.MonitorRuntime monitor = zermia.monitor.runtime.MonitorRuntime.getInstance(nodeID);
    }

    /**
     * Replica Received Request from client
     * Only Called on a Replica Monitor
     * @param joinPoint - join point for this advice
     * @throws Throwable
     */
    //bftsmart.tom.core.TOMLayer.requestReceived(TOMMessage msg, boolean fromClient)
    @Before("execution (* bftsmart.tom.core.TOMLayer.requestReceived*(..))")
    public void advice_r1(JoinPoint joinPoint) throws Throwable {
        SystemMessage sysMsg = (SystemMessage) joinPoint.getArgs()[0];
        TOMMessage msg = (TOMMessage)sysMsg;
        System.out.printf("[ZermiaMonitorHooks] advice_r1 bftsmart.tom.core.TOMLayer.requestReceived (%d, %d, %d, %s): from: %d\n", msg.getId(), msg.getSequence(), msg.getOperationId(), Arrays.toString(msg.content), sysMsg.getSender());

        ClientState state = monitor.getClientState(sysMsg.getSender());
        if (state == null) {
            state = new BFTSmartClientState(sysMsg.getSender());
            monitor.addClientState(state);
        }
        updateMonitorState(sysMsg, state);
        //int nodeID = sysMsg.getSender();
        //zermia.monitor.runtime.MonitorRuntime monitor = zermia.monitor.runtime.MonitorRuntime.getInstance(nodeID);
    }

    /**
     * Replica Sent Reply to Client
     * Only Called on a Replica Monitor
     * @param joinPoint - join point for this advice
     * @throws Throwable
     */
    //bftsmart.communication.client.netty.NettyClientServerCommunicationSystemServerSide.send(int[] targets, TOMMessage sm, boolean serializeClassHeaders)
    @Before("execution (* bftsmart.communication.client.netty.NettyClientServerCommunicationSystemServerSide.send*(..))")
    public void advice_r8(JoinPoint joinPoint) throws Throwable {
        NettyClientServerCommunicationSystemServerSide _this = (NettyClientServerCommunicationSystemServerSide) joinPoint.getThis();
        int[] targets = (int[]) joinPoint.getArgs()[0];
        String to = "";
        for (int i = 0; i < targets.length; i++) {
            to += targets[i];
            if (i < (targets.length - 1))
                to += ", ";
        }
        SystemMessage sysMsg = (SystemMessage) joinPoint.getArgs()[1];
        //System.out.println("[ZermiaMonitorHooks] advice_r8 bftsmart.communication.client.netty.NettyClientServerCommunicationSystemServerSide.send: from: " + sysMsg.getSender() + " to: " + to);

        int monitor_id = Integer.parseInt(to);
        ClientState state = monitor.getClientState(monitor_id);
        if (state == null) {
            state = new BFTSmartClientState(monitor_id);
            monitor.addClientState(state);
        }
        updateMonitorState(sysMsg, state);
        //System.out.println("[ZermiaMonitorHooks] advice_r8 - " + monitor.getCrtTargetAppState());

        //int nodeID = sysMsg.getSender();
        //zermia.monitor.runtime.MonitorRuntime monitor = zermia.monitor.runtime.MonitorRuntime.getInstance(nodeID);
    }

    /**
     * Client Received Reply from replica
     * Only Called on a Client Monitor
     * @param joinPoint - join point for this advice
     * @throws Throwable
     */
    // bftsmart.communication.client.netty.NettyClientServerCommunicationSystemClientSide.channelRead0(ChannelHandlerContext ctx, TOMMessage sm)
    @Before("execution (* bftsmart.communication.client.netty.NettyClientServerCommunicationSystemClientSide.channelRead0*(..))")
    public void advice_c9(JoinPoint joinPoint) throws Throwable {
        NettyClientServerCommunicationSystemClientSide _this = (NettyClientServerCommunicationSystemClientSide) joinPoint.getThis();
        _this.getClientId();
        SystemMessage sysMsg = (SystemMessage) joinPoint.getArgs()[1];
        //System.out.println("[ZermiaMonitorHooks] advice_c9 bftsmart.communication.client.netty.NettyClientServerCommunicationSystemClientSide.channelRead0: (this.ClientId: " + _this.getClientId() + ") from: " + sysMsg.getSender());

        ReplicaState state = monitor.getReplicaState(sysMsg.getSender());
        if (state == null) {
            state = new BFTSmartReplicaState(sysMsg.getSender());
            monitor.addReplicaState(state);
        }
        updateMonitorState(sysMsg, state);
//        System.out.println("[ZermiaMonitorHooks] advice_c9 bftsmart.communication.client.netty.NettyClientServerCommunicationSystemClientSide.channelRead0: " + monitor.getCrtTargetAppState());
        //int nodeID = sysMsg.getSender();
        //zermia.monitor.runtime.MonitorRuntime monitor = zermia.monitor.runtime.MonitorRuntime.getInstance(nodeID);
    }


    /**
     * Replica Received Message from other Replica
     * Only Called by a Replica Monitor
     * @param joinPoint - join point for this advice
     * @throws Throwable
     */
    //bftsmart.communication.MessageHandler.processData(SystemMessage sm)
    @Before("execution (* bftsmart.communication.MessageHandler.processData*(..))")
    public void advice_r3_r5_r7(JoinPoint joinPoint) throws Throwable {
        SystemMessage sysMsg = (SystemMessage) joinPoint.getArgs()[0];
        System.out.printf("[ZermiaMonitorHooks] advice_r3_r5_r7 bftsmart.communication.MessageHandler.processData: %s(%s)\n", sysMsg.getClass().getSimpleName(), sysMsg);

        ReplicaState state = monitor.getReplicaState(sysMsg.getSender());
        if (state == null) {
            state = new BFTSmartReplicaState(sysMsg.getSender());
            monitor.addReplicaState(state);
        }
        updateMonitorState(sysMsg, state);
        //int nodeID = sysMsg.getSender();
        //zermia.monitor.runtime.MonitorRuntime monitor = zermia.monitor.runtime.MonitorRuntime.getInstance(nodeID);
    }

    /**
     * Target Application state update bases on Message to be sent
     * @param joinPoint - join point for this advice
     * @throws Throwable
     */
    // TODO: verify if this is used in other protocols
    // bftsmart.communication.ServerCommunicationSystem.send(int[] targets, SystemMessage sm)
//    @Before("execution (* bftsmart.communication.ServerCommunicationSystem.send*(..))")
//    public void advice1(JoinPoint joinPoint) throws Throwable {
//        int[] targets = (int[]) joinPoint.getArgs()[0];
//        String to = "";
//        for (int i = 0; i < targets.length; i++) {
//            to += targets[i];
//            if(i < (targets.length-1))
//                to += ", ";
//        }
//        System.out.println("[ZermiaMonitorHooks] advice1 bftsmart.communication.ServerCommunicationSystem.send: to: " + to);
//        SystemMessage sysMsg = (SystemMessage) joinPoint.getArgs()[1];
//        int nodeID = sysMsg.getSender();
//
//        log(sysMsg);
//    }

    /**
     * Replica sent message to other Replicas to inject fault based on current state of this node's state
     * Only Called on a Replica Monitor
     * @param joinPoint - join point for this advice
     * @throws Throwable
     */
    //bftsmart.communication.server.ServersCommunicationLayer.send(int[] targets, SystemMessage sm, boolean useMAC)
    @Around("execution (* bftsmart.communication.server.ServersCommunicationLayer.send*(..))")
    public void advice_r2_r4_r6(ProceedingJoinPoint joinPoint) throws Throwable {
        ServersCommunicationLayer _this = (ServersCommunicationLayer) joinPoint.getThis();
        int[] targets = (int[]) joinPoint.getArgs()[0];
        String to = "";
        for (int i = 0; i < targets.length; i++) {
            to += targets[i];
            if (i < (targets.length - 1))
                to += ", ";
        }
        //System.out.println("[ZermiaMonitorHooks] advice_r2_r4_r6 bftsmart.communication.server.ServersCommunicationLayer.send: from: " + _this.getReplicaId()+"/"+this.monitorID + " to: " + to);
        //zermia.monitor.runtime.MonitorRuntime zR = monitor;
        Object[] args = joinPoint.getArgs();
        SystemMessage sysMsg = (SystemMessage) joinPoint.getArgs()[1];

        ReplicaState state = monitor.getReplicaState(sysMsg.getSender());
        if (state == null) {
            state = new BFTSmartReplicaState(sysMsg.getSender());
            monitor.addReplicaState(state);
        }
        updateMonitorState(sysMsg, state);

        FaultDescription fd = this.monitor.getNextFault2Inject();
        if(fd != null) {
            switch (fd.getFault_type()) {
                case "DelayFault": {
                    try {
                        DelayFaultArguments dfa = (DelayFaultArguments) fd.getFault_arguments();
                        System.out.printf("\t [ZermiaMonitorHooks] Triggering %s: %d \n", fd.getFault_type(), dfa.getDelayDuration());
                        System.out.printf("\t [ZermiaMonitorHooks] Triggering Fault: %s \n", fd.toString());
                        Thread.currentThread().sleep(dfa.getDelayDuration());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "DropFault": {
                    DropFaultArguments dfa = (DropFaultArguments) fd.getFault_arguments();
                    int f_to = dfa.getTo();

                    System.out.printf("\t [ZermiaMonitorHooks] Triggering %s: for replica %d \n", fd.getFault_type(), f_to);
                    System.out.printf("\t [ZermiaMonitorHooks] Triggering Fault: %s \n", fd.toString());

                    int[] crt_targets = (int[]) joinPoint.getArgs()[0];
                    int[] new_target = new int[crt_targets.length-1];
                    int j = 0;
                    for( int i = 0; i < crt_targets.length; i++) {
                        if (crt_targets[i] == f_to)
                            continue;
                        new_target[j] = crt_targets[i];
                        j += 1;
                    }
                    args[0] = new_target;
                    break;
                }
                case "DuplicateFault": {
                    DuplicateFaultArguments dfa = (DuplicateFaultArguments) fd.getFault_arguments();
                    int f_to = dfa.getTo();

                    System.out.printf("\t [ZermiaMonitorHooks] Triggering %s: for replica %d \n", fd.getFault_type(), f_to);
                    System.out.printf("\t [ZermiaMonitorHooks] Triggering Fault: %s \n", fd.toString());

                    int[] crt_targets = (int[]) joinPoint.getArgs()[0];
                    int[] new_target = new int[crt_targets.length + 1];
                    int j = 0;
                    for( int i = 0; i < crt_targets.length; i++) {
                        if (crt_targets[i] == f_to) {
                            new_target[j] = crt_targets[i];
                            j += 1;
                        }
                        new_target[j] = crt_targets[i];
                        j += 1;
                    }
                    args[0] = new_target;
                    break;
                }
                case "CrashFault": {
                    System.out.printf("\t [ZermiaMonitorHooks] Triggering %s:\n", fd.getFault_type());
                    System.out.printf("\t [ZermiaMonitorHooks] Triggering Fault: %s \n", fd.toString());
                    System.exit(-1);
                    break;
                }
                default: {
                    break;
                }
            }
        }

        //TODO determine if any fault should be injected

        joinPoint.proceed(args);

    }


    // bftsmart.communication.server.ServerConnection.send(byte[] data)
    @Around("execution (* bftsmart.communication.server.ServerConnection.send*(..))")
    public void advice3(ProceedingJoinPoint joinPoint) throws Throwable {
        ServerConnection _this = (ServerConnection) joinPoint.getThis();
        byte[] data = (byte[]) joinPoint.getArgs()[0];


//        System.out.println("[ZermiaMonitorHooks] advice3 bftsmart.communication.server.ServerConnection.send - " + data.length + " bytes");
//        zermia.monitor.runtime.MonitorRuntime zR = zermia.monitor.runtime.MonitorRuntime.getInstance();
        Object[] arg = joinPoint.getArgs();

        //if (_this.getRemoteId() == faultyReplica)
        // TODO : implement modify messages
        // TODO : need to know which server is sending message

        joinPoint.proceed(arg);
    }


    //---------------------------------------------------------------------------------//
    // bftsmart.tom.leaderchange.LCManager.setNewLeader(int leader)
    @Before("execution (* bftsmart.tom.leaderchange.LCManager.setNewLeader*(..))")
    public void advice4(JoinPoint jpoint) {
        LCManager _this = (LCManager) jpoint.getThis();
        crtLeader = (int) jpoint.getArgs()[0];
        //System.out.println("[ZermiaMonitorHooks] advice4 bftsmart.tom.leaderchange.LCManager.setNewLeader");

    }

    //---------------------------------------------------------------------------------//
    @AfterReturning(pointcut = "execution (* bftsmart.tom.core.ExecutionManager.getCurrentLeader*(..))", returning = "currentLeader")
    public void advice5(Object currentLeader) {
        crtLeader = (int) currentLeader;
        //System.out.println("[ZermiaMonitorHooks] advice5 bftsmart.tom.core.ExecutionManager.getCurrentLeader");

    }

//---------------------------------------------------------------------------------//		
    //@Around("execution (* bftsmart.tom.core.Synchronizer.triggerTimeout*(..))")
/*	@Around("execution (* bftsmart.tom.core.Synchronizer.startSynchronization*(..))")
    public void stopBombard(ProceedingJoinPoint joinPoint) throws Throwable {
        ZermiaRuntime zR = ZermiaRuntime.getInstance();
        Object[] arg = joinPoint.getArgs();
        Integer getArg1 = (Integer) joinPoint.getArgs()[0];
        getArg1 = getArg1 +1;
        arg[0] = getArg1;

        //System.out.println("Called" + run);
        if (zR.getReplicaFaultState().equals(false) || run == null) {
            //if not faulty do nothing
        }else {
             if (zR.getRunStart() <= run && zR.getRunTrigSum()> run) {
                 zR.setBoolIterator(true);
                 for (int i = 0; i < zR.getFaultPamList().get(zR.getListIterator()).size(); i = i + 2) {
                     Integer faultArg = Integer.parseInt(zR.getFaultPamList().get(zR.getListIterator()).get(i+1));
                     String faultN = zR.getFaultPamList().get(zR.getListIterator()).get(i);

                     switch(faultN) {
                        case "FloodStop" : {
                            for(int jk=0;jk<faultArg;jk++) {
                                joinPoint.proceed(arg);
                                //System.out.println("teste " + jk);
                            }
                        break;}
                        default : break;
                     }
                 }
             }
        }
        joinPoint.proceed();
    }

    @Around("execution (* bftsmart.tom.leaderchange.LCManager.setNextReg*(..))")
    public void changeRegency(ProceedingJoinPoint joinPoint) throws Throwable {
        ZermiaRuntime zR = ZermiaRuntime.getInstance();
        if (zR.getReplicaFaultState().equals(false) || run == null) {
            joinPoint.proceed();
        }else {
            Object[] arg = joinPoint.getArgs();
            Integer getArg1 = (Integer) joinPoint.getArgs()[0];
            getArg1 = getArg1 +1;
            arg[0] = getArg1;
            joinPoint.proceed(arg);
        }
    }
*/
//---------------------------------------------------------------------------------//

    @Before("execution (* bftsmart.tom.leaderchange.LCManager.setNextReg*(..))")
    public void checkRegencyNext(JoinPoint joinPoint) {
//        zermia.monitor.runtime.MonitorRuntime zR = zermia.monitor.runtime.MonitorRuntime.getInstance();
//        Integer arg1 = (Integer) joinPoint.getArgs()[0];
//        zR.setNextRegency(arg1);
    }

    @Before("execution (* bftsmart.tom.leaderchange.LCManager.setLastReg*(..))")
    public void checkRegencyOld(JoinPoint joinPoint) {
//        zermia.monitor.runtime.MonitorRuntime zR = zermia.monitor.runtime.MonitorRuntime.getInstance();
//        Integer arg1 = (Integer) joinPoint.getArgs()[0];
//        zR.setOldRegency(arg1);
    }


    //----------------------------------------------------------------------------------//
    public byte[] messageVert(SystemMessage sm) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream(); //248
        try {
            new ObjectOutputStream(bo).writeObject(sm);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] smk = bo.toByteArray();
        return smk;
    }

    public SystemMessage messageVert2(byte[] sm, SystemMessage msk) {
        SystemMessage smk = msk;
        try {
            smk = (SystemMessage) (new ObjectInputStream(new ByteArrayInputStream(sm)).readObject());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return smk;
    }

    //if old is different than next, means a view change
    public Boolean verifyRegency() {
//        zermia.monitor.runtime.MonitorRuntime zR = zermia.monitor.runtime.MonitorRuntime.getInstance();
//        if (zR.getOldRegency() == zR.getNextRegency()) {
//            changeR = true;
//            return false; // no view change
//        } else {
//            return true; //view change
//        }
        return false;
    }

    String byteArrayToString(byte[] in) {
        char[] out = new char[in.length * 2];
        for (int i = 0; i < in.length; i++) {
            out[i * 2] = "0123456789ABCDEF".charAt((in[i] >> 4) & 15);
            out[i * 2 + 1] = "0123456789ABCDEF".charAt(in[i] & 15);
        }
        return new String(out);
    }

//--------------------------------------------------------------------------------//	

    public byte[] readStopPayload() { //reads payload from stop file
        FileInputStream fin = null;
        ObjectInputStream obIS = null;
        byte[] retornaB = null;

        try {
            fin = new FileInputStream("savedStopPay.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            obIS = new ObjectInputStream(fin);
            retornaB = (byte[]) obIS.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return retornaB;

    }

    public byte[] readStopDataPayload() { //reads payload from stopdata file
        FileInputStream fin = null;
        ObjectInputStream obIS = null;
        byte[] retornaB = null;

        try {
            fin = new FileInputStream("savedStopDataPay.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            obIS = new ObjectInputStream(fin);
            retornaB = (byte[]) obIS.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return retornaB;

    }

    public byte[] readSyncPayload() { //reads payload from sync file
        FileInputStream fin = null;
        ObjectInputStream obIS = null;
        byte[] retornaB = null;

        try {
            fin = new FileInputStream("savedSyncPay.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            obIS = new ObjectInputStream(fin);
            retornaB = (byte[]) obIS.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return retornaB;

    }

//------------------------------------------------------------------------------//	
    //code to save payloads for later tests

    public void writeStopPayload(byte[] bs) {
        try {
            FileOutputStream fout = new FileOutputStream("savedStopPay.txt");
            ObjectOutputStream outputo = new ObjectOutputStream(fout);
            outputo.writeObject(bs);
            outputo.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void writeStopDataPayload(byte[] bs) {
        try {
            FileOutputStream fout = new FileOutputStream("savedStopDataPay.txt");
            ObjectOutputStream outputo = new ObjectOutputStream(fout);
            outputo.writeObject(bs);
            outputo.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void writeSyncPayload(byte[] bs) {
        try {
            FileOutputStream fout = new FileOutputStream("savedSyncPay.txt");
            ObjectOutputStream outputo = new ObjectOutputStream(fout);
            outputo.writeObject(bs);
            outputo.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


//----------------------------------------------------------------------//

}

