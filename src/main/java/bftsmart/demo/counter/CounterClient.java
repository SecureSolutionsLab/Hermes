/**
Copyright (c) 2007-2013 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and the authors indicated in the @author tags

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package bftsmart.demo.counter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import bftsmart.tom.ServiceProxy;
import sun.jvm.hotspot.runtime.Threads;

/**
 * Example client that updates a BFT replicated service (a counter).
 * 
 * @author alysson
 */
public class CounterClient implements Runnable {

    private int processID;
    private int increment;
    private int numberOfOperations;

    private ServiceProxy counterProxy;

    public CounterClient(int processID, int increment, int numberOfOperations) {
        this.processID = processID;
        this.increment = increment;
        this.numberOfOperations = numberOfOperations;
        this.counterProxy = new ServiceProxy(processID);
    }

    public void run() {
        try {
            for (int i = 0; i < numberOfOperations; i++) {
                ByteArrayOutputStream out = new ByteArrayOutputStream(4);
                new DataOutputStream(out).writeInt(increment);

                System.out.print("Invocation " + i);
                byte[] reply = (increment == 0)?
                        counterProxy.invokeUnordered(out.toByteArray()):
                        counterProxy.invokeOrdered(out.toByteArray()); //magic happens here

                if(reply != null) {
                    int newValue = new DataInputStream(new ByteArrayInputStream(reply)).readInt();
                    System.out.println(", returned value: " + newValue);
                } else {
                    System.out.println(", ERROR! Exiting.");
                    break;
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        counterProxy.close();
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java ... CounterClient <process id> <increment> [<number of operations>] [<number of clients>]");
            System.out.println("       if <increment> equals 0 the request will be read-only");
            System.out.println("       default <number of operations> equals 1000");
            System.out.println("       default <number of clients> equals 1");
            System.exit(-1);
        }

        int baseProcID = Integer.parseInt(args[0]);
        int inc = Integer.parseInt(args[1]);
        int numberOfOps = (args.length > 2) ? Integer.parseInt(args[2]) : 1000;
        int numberOfClts = (args.length > 3) ? Integer.parseInt(args[3]) : 1;

        Thread clts[] = new Thread[numberOfClts];

        for(int i = 0; i < numberOfClts; i++) {
            clts[i] = new Thread(new CounterClient(baseProcID+i, inc, numberOfOps));
        }
        for(int i = 0; i < numberOfClts; i++) {
            clts[i].start();
        }
        for(int i = 0; i < numberOfClts; i++) {
            try {
                clts[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
