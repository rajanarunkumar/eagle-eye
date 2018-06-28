package com.hackathon.eagleeye.service;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

@RestController
@RequestMapping("/api/recoknizer/image")
@Slf4j
public class ImageRecoknizerService {

  @GetMapping("/getFacialLabel")
  public DetectLabelsResult detectLabels(@RequestParam String imageLocation) {
    ByteBuffer imageBytes = getImageBytesFromFileLocation(imageLocation);
    AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();
    DetectLabelsRequest request = new DetectLabelsRequest()
        .withImage(new Image()
            .withBytes(imageBytes))
        .withMaxLabels(10)
        .withMinConfidence(77F);
    DetectLabelsResult result = null;

    try {
      result = rekognitionClient.detectLabels(request);
      List<Label> labels = result.getLabels();
      log.info("Detected labels for " + imageLocation);
      for (Label label : labels) {
        log.info(label.getName() + ": " + label.getConfidence().toString());
      }
    } catch (AmazonRekognitionException e) {
      e.printStackTrace();
    }

    return result;
  }

  @GetMapping("/detectFaces")
  public DetectFacesResult detectFaces(@RequestParam String imageLocation) {
    ByteBuffer imageBytes = getImageBytesFromFileLocation(imageLocation);
    AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();
    DetectFacesRequest request = new DetectFacesRequest()
        .withImage(new Image()
            .withBytes(imageBytes));
    DetectFacesResult result = null;

    try {
      result = rekognitionClient.detectFaces(request);
      List<FaceDetail> faceDetails = result.getFaceDetails();
      log.info("Detected facial details for " + imageLocation);
      for (FaceDetail faceDetail : faceDetails) {
        log.info(
            faceDetail.getLandmarks().get(0).getType() + " - X: " + faceDetail.getLandmarks().get(0)
                .getX() + " Y:" + faceDetail.getLandmarks().get(0).getY());
        log.info(
            faceDetail.getLandmarks().get(1).getType() + " - X: " + faceDetail.getLandmarks().get(1)
                .getX() + " Y:" + faceDetail.getLandmarks().get(1).getY());
      }
    } catch (AmazonRekognitionException e) {
      e.printStackTrace();
    }
    return result;
  }

  private ByteBuffer getImageBytesFromFileLocation(String imagePath) {
    ByteBuffer imageBytes = null;
    try (InputStream inputStream = new FileInputStream(new File(imagePath))) {
      imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
    } catch (IOException e) {
      log.error("File not found");
      e.printStackTrace();
    }
    return imageBytes;
  }
}
