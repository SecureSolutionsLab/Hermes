package zermia.common.schedule.arguments.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import zermia.common.schedule.FaultArguments;
import zermia.common.schedule.FaultDescription;
import zermia.common.schedule.arguments.DelayFaultArguments;
import zermia.common.schedule.arguments.DifferentRequestsToAllArguments;
import zermia.common.schedule.arguments.DropFaultArguments;
import zermia.common.schedule.arguments.DuplicateFaultArguments;

import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;

public class FaultArgumentsDeserializer extends StdDeserializer<FaultArguments> {

    public FaultArgumentsDeserializer() {
        this(null);
    }

    public FaultArgumentsDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public FaultArguments deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JacksonException {
        try {
            JsonStreamContext parsingContext = jp.getParsingContext();
            JsonStreamContext parent = parsingContext.getParent();
            FaultDescription currentValue = (FaultDescription)parent.getCurrentValue();
            JsonNode node = jp.getCodec().readTree(jp);
            int consecutive_rounds = (Integer) ((IntNode) node.get("consecutive_rounds")).numberValue();
            if(currentValue.getFault_type().equals("DelayFault")) {
                int delay_duration = (Integer) ((IntNode) node.get("delay_duration")).numberValue();
                return new DelayFaultArguments(consecutive_rounds, delay_duration);
            } else if (currentValue.getFault_type().equals("DropFault")) {
                int to = (Integer) ((IntNode) node.get("to")).numberValue();
                return new DropFaultArguments(consecutive_rounds, to);
            } else if (currentValue.getFault_type().equals("DuplicateFault")) {
                int to = (Integer) ((IntNode) node.get("to")).numberValue();
                return new DuplicateFaultArguments(consecutive_rounds, to);
            } else if(currentValue.getFault_type().equals("CrashFault")) { // Crash faults do not have arguments
                return null;
            } else if(currentValue.getFault_type().equals("DifferentRequestsToAll")) { // Crash faults do not have arguments
                System.out.println(node);
                int timestamp = (Integer) ((IntNode) node.get("timestamp")).numberValue();
                int size = ((ArrayNode) node.get("operation")).size();
                byte[] operation = new byte[size];
                for(int i = 0;i < size; i++) {
                    operation[i] = (byte)((ArrayNode) node.get("operation")).get(i).asInt();
                }
                System.out.println(Arrays.toString(operation));

                return new DifferentRequestsToAllArguments(consecutive_rounds, timestamp, operation);
            } else { // all faults have consecutive rounds argument
                return new FaultArguments(consecutive_rounds);
            }
        } catch ( Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
