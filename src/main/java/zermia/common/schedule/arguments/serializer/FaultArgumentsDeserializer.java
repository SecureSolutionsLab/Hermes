package zermia.common.schedule.arguments.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;
import zermia.common.schedule.FaultArguments;
import zermia.common.schedule.FaultDescription;
import zermia.common.schedule.arguments.DelayFaultArguments;
import zermia.common.schedule.arguments.DropFaultArguments;
import zermia.common.schedule.arguments.DuplicateFaultArguments;

import java.io.IOException;

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
                return new DelayFaultArguments(delay_duration, consecutive_rounds);
            } else if (currentValue.getFault_type().equals("DropFault")) {
                int to = (Integer) ((IntNode) node.get("to")).numberValue();
                return new DropFaultArguments(to, consecutive_rounds);
            } else if (currentValue.getFault_type().equals("DuplicateFault")) {
                int to = (Integer) ((IntNode) node.get("to")).numberValue();
                return new DuplicateFaultArguments(to, consecutive_rounds);
            } else if(currentValue.getFault_type().equals("CrashFault")) { // Crash faults do not have arguments
                return null;
            } else { // all faults have consecutive rounds argument
                return new FaultArguments(consecutive_rounds);
            }
        } catch ( Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
