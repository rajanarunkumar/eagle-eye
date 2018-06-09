package com.hackathon.eagleeye.service;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.util.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recoknizer/image")
@Slf4j
public class ImageRecoknizerService {

  public static void main(String[] args) {
    ImageRecoknizerService irs = new ImageRecoknizerService();
    irs.process("blah");
  }

  @GetMapping("/process")
  public void process(String imageLocation) {
    // Explore Recoknition API
    String photo="/Users/arunrajan/Work/oss-learning/eagle-eye/src/main/resources/static/180608091639-anthony-bourdain-new-york-exlarge-169.jpg";

    ByteBuffer imageBytes = null;
    try (InputStream inputStream = new FileInputStream(new File(photo))) {
      imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
    } catch (IOException e) {
      e.printStackTrace();
    }

    AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();

    DetectLabelsRequest request = new DetectLabelsRequest()
        .withImage(new Image()
            .withBytes(imageBytes))
        .withMaxLabels(10)
        .withMinConfidence(77F);

    try {

      DetectLabelsResult result = rekognitionClient.detectLabels(request);
      List<Label> labels = result.getLabels();

      System.out.println("Detected labels for " + photo);
      for (Label label: labels) {
        System.out.println(label.getName() + ": " + label.getConfidence().toString());
      }

    } catch (AmazonRekognitionException e) {
      e.printStackTrace();
    }
  }
}
