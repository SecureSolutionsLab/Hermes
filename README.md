
# Hermes Fault Injector framework

This is a fault injection framework designed to inject faults into Byzantine Fault Tolerant (BFT) protocols.

It allows users to define a set of faults (referred to as fault schedule) that are injected into the target application at runtime.

## Design

The Hermes fault injection framework is designed to test and validate BFT protocols/applications.
It monitors the state of the protocol's replicas and clients and manages fault injection based on their state.

State monitoring is accomplished by Monitors. These attach to replicas and clients using Aspect Oriented Programming (AOP).

A central Coordinator reads a user defined global fault schedule and is responsible for managing the monitors.
Monitors register themselves with the Coordinator and retrieve their fault schedule.


## Defining fault schedule

Users can define a global fault schedule using either a `.json` on `.yaml` file.
A global fault schedule is a set of monitor configurations that include:
- the monitor identifier;
- the target bft replica/client it attaches to;
- if it should notify/update the coordinator after each fault; and,
- the monitor's fault schedule

The structure of a fault schedule is presented next:

```
schedule:                         # a list of monitor information and respective fault schedules
 - monitor_id: <id>
   target: <relica/client id>
   fault_schedule:
     - fault_type: <fault type>
       fault_trigger_conditions:  # the conditions required for injectig this fault
         consensus_id: <consensus id>
         view_id: <view id>
         protocol: <protocol>
         protocol_phase: <protocol_phase>
       fault_arguments:           # additional arguments needed for each specific fault
         ...
         ... 


```

An example of a Fault Schedule presented next:

```
schedule:
  - monitor_id: 0
    target: replica_0
    fault_schedule:
      - fault_type: DelayFault      # delay message fault
        fault_trigger_conditions:
          consensus_id: 80
          view_id: -1
          protocol: 'CONSENSUS'
          protocol_phase: 'PROPOSE'
        fault_arguments:
          delay_duration: 1000      # amount of time to delay the replica
          consecutive_rounds: 20    # number of consecutive rounds 
      - fault_type: DropFault       # drop message fault
        fault_trigger_conditions:
          consensus_id: 100
          view_id: -1
          protocol: 'CONSENSUS'
          protocol_phase: 'PROPOSE'
        fault_arguments:
          to: 2
          consecutive_rounds: 20
      - fault_type: DropFault       # drop message fault
        fault_trigger_conditions:
          consensus_id: 100
          view_id: -1
          protocol: 'CONSENSUS'
          protocol_phase: 'WRITE'
        fault_arguments:
          to: 2
          consecutive_rounds: 20
      - fault_type: DropFault       # drop message fault
        fault_trigger_conditions:
          consensus_id: 100
          view_id: -1
          protocol: 'CONSENSUS'
          protocol_phase: 'ACCEPT'
        fault_arguments:
          to: 2
          consecutive_rounds: 20
      - fault_type: DuplicateFault  # duplicate message fault
        fault_trigger_conditions:
          consensus_id: 101
          view_id: -1
          protocol: 'CONSENSUS'
          protocol_phase: 'PROPOSE'
        fault_arguments:
          to: 3
          consecutive_rounds: 20
      - fault_type: CrashFault      # crash node fault
        fault_trigger_conditions:
          consensus_id: 105
  - monitor_id: 1
    target: replica_1
    fault_schedule:
      - fault_type: DelayFault
        fault_trigger_conditions:
          consensus_id: 80
          view_id: -1
          protocol: 'CONSENSUS'
          protocol_phase: 'WRITE'
        fault_arguments:
          delay_duration: 1000
          consecutive_rounds: 20
  - monitor_id: 2
    target: replica_2
    fault_schedule: []              # empty fault schedule
  - monitor_id: 3
    target: replica_3
    fault_schedule: []              # empty fault schedule
  - monitor_id: 1001
    target: client_1001
    fault_schedule:
      - fault_type: Only2Primary
        fault_trigger_conditions:
          consensus_id: 80
          view_id: -1
          protocol: 'CONSENSUS'
          protocol_phase: 'REQUEST'
      - fault_type: Only2Secondaries
        fault_trigger_conditions:
          consensus_id: 81
          view_id: -1
          protocol: 'CONSENSUS'
          protocol_phase: 'REQUEST'

```


## Code

`src/proto/` - Coordinator services definitions

`src/java/zermia/` - fault injection framework

`src/java/bftsmart/` - target BFT protocol/application (in this case BFT-SMaRt) 

## Compiling

Type `./gradlew build` in the main directory to build the project. The required jar files and default configuration files will be available in the `build/install/library` directory.

Type `./gradlew localDeploy` in the main directory to locally deploy the project.

Type `./gradlew installDist` in the main directory to locally install Hermes.


## Running 

- __Use bft_home environment variable for handling bft_smart path__
> `export bft_home="path/to/project"`

- __Coordinator__

> `java -Duser.dir="$bft_home/build/install/library/" -Djava.security.properties="$bft_home/build/install/library/config/java.security" -Dlogback.configurationFile="$bft_home/build/install/library/config/logback.xml" -cp ".:$bft_home/build/install/library/:$bft_home/build/install/library/lib/*" zermia.coordinator.ZermiaCoordinatorMain schedule.yaml`

- __Running a modified version of BFTCounterClient test (original -> single threaded; new -> multithreaded)__

- __Server 0__
> `java -Duser.dir="$bft_home/build/local/rep0/" -Djava.security.properties="$bft_home/build/local/rep0/config/java.security" -Dlogback.configurationFile="$bft_home/build/local/rep0/config/logback.xml" -cp ".:$bft_home/build/local/rep0/:$bft_home/build/local/rep0/lib/*" bftsmart.demo.counter.BFTCounterServer 0`

- __Server 1__
> `java -Duser.dir="$bft_home/build/local/rep1/" -Djava.security.properties="$bft_home/build/local/rep1/config/java.security" -Dlogback.configurationFile="$bft_home/build/local/rep1/config/logback.xml" -cp ".:$bft_home/build/local/rep1/:$bft_home/build/local/rep1/lib/*" bftsmart.demo.counter.BFTCounterServer 1`

- __Server 2__
> `java -Duser.dir="$bft_home/build/local/rep2/" -Djava.security.properties="$bft_home/build/local/rep2/config/java.security" -Dlogback.configurationFile="$bft_home/build/local/rep2/config/logback.xml" -cp ".:$bft_home/build/local/rep2/:$bft_home/build/local/rep2/lib/*" bftsmart.demo.counter.BFTCounterServer 2`

- __Server 1__
> `java -Duser.dir="$bft_home/build/local/rep3/" -Djava.security.properties="$bft_home/build/local/rep3/config/java.security" -Dlogback.configurationFile="$bft_home/build/local/rep3/config/logback.xml" -cp ".:$bft_home/build/local/rep3/:$bft_home/build/local/rep3/lib/*" bftsmart.demo.counter.BFTCounterServer 3`

- __Client__
> `CounterClient <client id> <increment value> [<number of operations>] [<number of clients>]`


> `java -Duser.dir="$bft_home/build/local/cli0/" -Djava.security.properties="$bft_home/build/local/cli0/config/java.security" -Dlogback.configurationFile="$bft_home/build/local/cli0/config/logback.xml" -cp ".:$bft_home/build/local/cli0/:$bft_home/build/local/cli0/lib/*" bftsmart.demo.counter.CounterClient 1001 1 100 3`





## Additional information and publications

***Feel free to contact us if you have any questions!***
