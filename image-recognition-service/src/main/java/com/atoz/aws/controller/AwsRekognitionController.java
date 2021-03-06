package com.atoz.aws.controller;

import com.atoz.aws.service.AtoZImageRekognitionService;
import com.atoz.aws.service.DynamoDbAccessService;
import com.atoz.aws.service.S3AccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AwsRekognitionController {
    private static Logger log = LoggerFactory.getLogger(AwsRekognitionController.class);

    @Value("${aws.dynamoDb.table.attribute.fullname}")
    private String attributeName;

    @Autowired
    private S3AccessService s3Service;

    @Autowired
    private AtoZImageRekognitionService imageService;

    @Autowired
    private DynamoDbAccessService dynamoDbAccessService;

    @CrossOrigin
    @RequestMapping(value="/image/s3upload", method=RequestMethod.POST)
    public ResponseEntity<String> s3UploadFile(@RequestParam("name") String name,
                               @RequestParam("file") MultipartFile uploadfile) {
        Map<String, String> metaData = new HashMap<>();
        metaData.put(attributeName, name);
        String content;
        HttpStatus status = HttpStatus.OK;

        try {
            s3Service.uploadInputStreram(name.replaceAll("\\s","")+".jpg", uploadfile.getInputStream(), metaData);
            content = name + " photo uploaded successfully!";
            log.info(content);
        } catch (IOException ioe) {
            content = name + " upload failed.";
            log.error(content);
            status = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(content, status);
    }

    @CrossOrigin
    @RequestMapping(value="/image/match", method=RequestMethod.POST)
    public ResponseEntity<Map<String, Float>> matchImage(@RequestParam("file") MultipartFile uploadfile) {
        try {
            Map<String, Float> matchedNames = imageService.matchImage(uploadfile.getInputStream());
            return new ResponseEntity<>(matchedNames, HttpStatus.OK);
        } catch (Exception ioe) {
            log.error("Error to match image: {}", ioe.getMessage());
            HashMap<String,Float> noMatch = new HashMap<>();
            noMatch.put(ioe.getMessage(),100f);
            return new ResponseEntity<Map<String, Float>>(noMatch, HttpStatus.BAD_REQUEST);
        }
    }
}
