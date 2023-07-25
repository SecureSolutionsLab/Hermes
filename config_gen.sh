#!/bin/bash
# Basic while loop

##Modify
#shadow.yaml
#coordinator.config
#config/hosts.config
#config/system.config
#build.gradle

#case "PNRS":{	//Primary no request sent
#case "DMTA":{ //send three different messages to different replicas
#case "DMTA2":{ //send three different messages to different replicas
#case "DMTAEP":{ //different messages to all except primary does receive anything
#case "DMTAEP2":{ //different messages to all except primary does receive anything and the operation numbers are all different

counter=0
start_time=1
f=5
limit=$((3*f))

echo "general:"
echo "  log_level: error #error warning info debug trace"
echo "  stop_time: $((100*f))s"
echo "  parallelism: 64"
echo "  progress: false"
echo "  model_unblocked_syscall_latency: true # Isto tem de estar a false NOT"
echo "network:"
echo "  graph:"
echo "    type: 1_gbit_switch"
echo "hosts:"
echo "  coordinator:"
echo "    network_node_id: 0"
echo "    options:"
echo "      log_level: error"
echo "    processes:"
echo "      - path: java"

#counter=0
#str="Replica 0 TdelayAll 50 2000 50 TdelayAll 50 4000 50 TdelayAll 50 6000 50 TdelayAll 50 8000 50"

counter=0
str=""
while [ $counter -lt $f ]
do
    str+="Replica ${counter} Dropper 100 $((counter*2000 + 2000)) 4 "
    ((counter++))
done

str="Client 200 PNRS 1 100 100 Client 200 PNRS 2 200 200"

echo "        args: -Duser.dir=\"/home/joaosoares/bft_smart-library-master/build/local/cli0/\" -Djava.security.properties=\"/home/joaosoares/bft_smart-library-master/build/local/cli0/config/java.security\" -Dlogback.configurationFile=\"/home/joaosoares/bft_smart-library-master/build/local/cli0/config/logback.xml\" -cp \".:/home/joaosoares/bft_smart-library-master/build/local/cli0/:/home/joaosoares/bft_smart-library-master/build/local/cli0/lib/*\" coordinator.ZermiaCoordinatorMain ${str}"
echo "        start_time: 0s"
counter=0
while [ $counter -le $limit ]
do
    echo "  replica${counter}:"
    echo "    network_node_id: 0"
    echo "    options:"
    echo "      log_level: error"
    echo "    processes:"
    echo "      - path: java"
    echo "        args: -Duser.dir=\"/home/joaosoares/bft_smart-library-master/build/local/rep${counter}/\" -Djava.security.properties=\"/home/joaosoares/bft_smart-library-master/build/local/rep${counter}/config/java.security\" -Dlogback.configurationFile=\"/home/joaosoares/bft_smart-library-master/build/local/rep${counter}/config/logback.xml\" -cp \".:/home/joaosoares/bft_smart-library-master/build/local/rep${counter}/:/home/joaosoares/bft_smart-library-master/build/local/rep${counter}/lib/*\" bftsmart.demo.ycsb.YCSBServer ${counter}"
    echo "        start_time: ${start_time}s"
    ((counter++))
#    ((start_time+=1))

    if ((counter == 1))
    then
        ((start_time+=1))
    fi

#    if ((counter%4 == 0))
#    then
#        ((start_time+=1))
#    fi
done
echo "  client:"
echo "    network_node_id: 0"
echo "    options:"
echo "      log_level: error"
echo "    processes:"
echo "      - path: java"
echo "        args: -Duser.dir=\"/home/joaosoares/bft_smart-library-master/build/local/cli0/\" -Djava.security.properties=\"/home/joaosoares/bft_smart-library-master/build/local/cli0/config/java.security\" -Dlogback.configurationFile=\"/home/joaosoares/bft_smart-library-master/build/local/cli0/config/logback.xml\" -cp \".:/home/joaosoares/bft_smart-library-master/build/local/cli0/:/home/joaosoares/bft_smart-library-master/build/local/cli0/lib/*\" com.yahoo.ycsb.Client -threads 200 -P /home/joaosoares/bft_smart-library-master/build/local/cli0/config/workloads/workloada -p measurementtype=timeseries -p timeseries.granularity=1000 -db bftsmart.demo.ycsb.YCSBClient -s"
echo "        start_time: $((10*f))s"


echo " "
echo " "

echo "zermia.numberOfClients=$((limit+2))"
counter=0
while [ $counter -le $((limit+1)) ]
do
    echo "zermia.client.ID.${counter}=${counter}"
    ((counter++))
done


echo " "
echo " "

counter=0
port=11000
port1=11001
while [ $counter -le $limit ]
do
    echo "${counter} replica$((counter)) ${port} ${port1}"
#    echo "${counter} 127.0.0.$((2+counter)) ${port} ${port1}"
    ((counter++))
    ((port+=10))
    ((port1+=10))
done


echo " "
echo " "

counter=0
str=""
while [ $counter -le $limit ]
do
    if ((counter < limit))
    then
        str+="${counter},"
    else
        str+="${counter}"
    fi
    ((counter++))
done

echo "system.servers.num = $((3*f+1))"
echo "system.servers.f = ${f}"
echo "system.initial.view = ${str}"
echo "system.totalordermulticast.timeout = 2000"

echo " "
echo " "
echo " "
echo " "


str="Replica 0 Crash 1 2000 1 Replica 1 Crash 1 4000 1 Replica 2 Crash 1 6000 1 Replica 3 Crash 1 8000 1 Replica 4 Crash 1 10000 1 Replica 5 Crash 1 12000 1 Replica 6 Crash 1 14000 1 Replica 7 Crash 1 16000 1 Replica 8 Crash 1 18000 1 Replica 9 Crash 1 20000 1"
echo "echo \"Starting Coordinator\";"
echo "java -Duser.dir=\"/home/joaosoares/bft_smart-library-master/build/local/cli0/\" -Djava.security.properties=\"/home/joaosoares/bft_smart-library-master/build/local/cli0/config/java.security\" -Dlogback.configurationFile=\"/home/joaosoares/bft_smart-library-master/build/local/cli0/config/logback.xml\" -cp \".:/home/joaosoares/bft_smart-library-master/build/local/cli0/:/home/joaosoares/bft_smart-library-master/build/local/cli0/lib/*\" coordinator.ZermiaCoordinatorMain ${str} > coordinator.stdout 2> coordinator.stderr &"
echo "sleep 2;"
counter=0
while [ $counter -le $limit ]
do
    echo "echo \"Starting Replica ${counter}\";"
    echo "java -Duser.dir=\"/home/joaosoares/bft_smart-library-master/build/local/rep${counter}/\" -Djava.security.properties=\"/home/joaosoares/bft_smart-library-master/build/local/rep${counter}/config/java.security\" -Dlogback.configurationFile=\"/home/joaosoares/bft_smart-library-master/build/local/rep${counter}/config/logback.xml\" -cp \".:/home/joaosoares/bft_smart-library-master/build/local/rep${counter}/:/home/joaosoares/bft_smart-library-master/build/local/rep${counter}/lib/*\" bftsmart.demo.ycsb.YCSBServer ${counter} > replica${counter}.stdout 2> replica${counter}.stderr &"
    echo "sleep 1;"
    ((counter++))
done
echo "sleep 25;"
echo "echo \"Starting Client\";"
echo "java -Duser.dir=\"/home/joaosoares/bft_smart-library-master/build/local/cli0/\" -Djava.security.properties=\"/home/joaosoares/bft_smart-library-master/build/local/cli0/config/java.security\" -Dlogback.configurationFile=\"/home/joaosoares/bft_smart-library-master/build/local/cli0/config/logback.xml\" -cp \".:/home/joaosoares/bft_smart-library-master/build/local/cli0/:/home/joaosoares/bft_smart-library-master/build/local/cli0/lib/*\" com.yahoo.ycsb.Client -threads 200 -P /home/joaosoares/bft_smart-library-master/build/local/cli0/config/workloads/workloada -p measurementtype=timeseries -p timeseries.granularity=1000 -db bftsmart.demo.ycsb.YCSBClient -s > client0.stdout 2> client0.stderr &"
echo "sleep $((50*f));"
echo "echo \"Terminating Experiment\";"
echo "killall java"
