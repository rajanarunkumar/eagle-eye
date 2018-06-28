package com.atoz.aws.controller;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Profile("SQS")
@Slf4j
public class MessageController {

    @Autowired
    private SimpMessagingTemplate template;

    public String sendToUI(ByteOutputStream content) {
        this.template.convertAndSend("/topic/images", content);
        return "Test complete";
    }
}
