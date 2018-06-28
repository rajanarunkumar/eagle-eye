package com.atoz.aws.service;

import com.atoz.aws.controller.MessageController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import java.io.StringReader;

@Service
@Profile("SQS")
@Slf4j
public class SQSListenerService {

    private final MessageController messageController;

    private final S3AccessService s3AccessService;

    private final DynamoDbAccessService dynamoDbAccessService;

    @Autowired
    public SQSListenerService(MessageController messageController, S3AccessService s3AccessService, DynamoDbAccessService dynamoDbAccessService) {
        this.messageController = messageController;
        this.s3AccessService = s3AccessService;
        this.dynamoDbAccessService = dynamoDbAccessService;
    }

    @JmsListener(destination = "eagle-eye-live-feed-hit-queue")
    public void receiveMessage(String requestJSON) throws JMSException {
        log.info("Received Message ");
        try {
            processImage(requestJSON);
        } catch (Exception ex) {
            log.error("Encountered error while parsing message.", ex);
            throw new JMSException("Encountered error while parsing message.");
        }
    }

    private void processImage(String content) {
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(new StringReader(content)).get("Message");
            JsonNode faceId = new ObjectMapper().readTree(new StringReader(jsonNode.asText())).get("FaceSearchResponse").get(0).get("MatchedFaces").get(0).get("Face").get("FaceId");
            messageController.sendToUI(s3AccessService.getObject(dynamoDbAccessService.getItem(faceId.asText()).get("S3Key").s()));
        } catch (Exception e) {
            log.error("Error processing Message : {}", e.getMessage());
        }
    }
}


