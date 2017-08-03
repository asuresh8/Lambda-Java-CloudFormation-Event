package com.amazonaws.lambda.runtime.events.samples;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.cloudformation.model.StackEvent;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * represents how to handle an ses event in lambda
 */
public class CloudFormationHandler implements RequestHandler<SNSEvent, StackEvent> {

    /**
     * handle lambda request
     * @param request SNS message
     * @param context Lambda context
     * @return deserialized SstackEvent
     */
    public StackEvent handleRequest(SNSEvent request, Context context) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            String cloudFormationMessage = request.getRecords().get(0).getSNS().getMessage();
            StringBuilder jsonStringBuilder = new StringBuilder();
            jsonStringBuilder.append("{");
            List<String> keyValuePairs = new ArrayList<String>(Arrays.asList(cloudFormationMessage.split("\n")));
            for (int i=0; i < keyValuePairs.size(); i++) {
                String keyValuePair = keyValuePairs.get(i);
                String key = keyValuePair.split("=")[0];
                String value = keyValuePair.split("=")[1].replace("\'", "");
                jsonStringBuilder.append("\"");
                jsonStringBuilder.append(key);
                jsonStringBuilder.append("\": \"");
                jsonStringBuilder.append(value);
                jsonStringBuilder.append("\"");
                // don't append comma if last element in list of pairs
                if (i != (keyValuePairs.size() - 1)) {
                    jsonStringBuilder.append(",");
                }
            }
            jsonStringBuilder.append("}");
            StackEvent stackEvent = mapper.readValue(jsonStringBuilder.toString(), StackEvent.class);
            System.out.println("Received stack event from stack: " + stackEvent.getStackName());
            return stackEvent;

        } catch (IOException e) {
            System.out.println("Unable to deserialize cloudformation event");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
