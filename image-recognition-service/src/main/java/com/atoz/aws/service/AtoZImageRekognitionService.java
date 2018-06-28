package com.atoz.aws.service;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.util.IOUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Service
public class AtoZImageRekognitionService {
    private static final Logger log = LoggerFactory.getLogger(AtoZImageRekognitionService.class);

    @Autowired
    private DynamoDbAccessService dynamoService;

    @Value("${aws.rekognition.image.collection}")
    private String imageCollection;

    private final AmazonRekognition client;

    public AtoZImageRekognitionService() {
        client = AmazonRekognitionClientBuilder.defaultClient();
    }

    public AtoZImageRekognitionService(String imageCollection) {
        this.imageCollection = imageCollection;
        this.client = AmazonRekognitionClientBuilder.defaultClient();
    }

    public void imageIndex(File file, String name) throws Exception {
        Image image = getImageFromFile(file);

        IndexFacesRequest indexRequest = new IndexFacesRequest().withImage(image).withCollectionId(imageCollection);
        IndexFacesResult indexResults = client.indexFaces(indexRequest);
        List<FaceRecord> faceRecs = indexResults.getFaceRecords();
        if (faceRecs.isEmpty()) {
            log.info("No image indexed from image file: {}", file.getAbsolutePath());
        } else {
            // only index the largest face in the image
            for (FaceRecord face: faceRecs) {
                dynamoService.putItem(face.getFace().getFaceId(),
                        buildExtraDbItemAttributes(dynamoService.getAttrFullName(), name));
            }
            log.info("{} FaceIds are indexed from the image file {}", faceRecs.size(), file.getAbsolutePath());
        }
    }

    /**
     * @param inputStream
     * @param name
     * @throws Exception
     */
    public void imageIndex(InputStream inputStream, String name) throws Exception {
        ByteBuffer imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
        Image image = new Image().withBytes(imageBytes);

        IndexFacesRequest indexRequest = new IndexFacesRequest().withImage(image).withCollectionId(imageCollection);
        IndexFacesResult indexResults = client.indexFaces(indexRequest);
        List<FaceRecord> faceRecs = indexResults.getFaceRecords();
        if (faceRecs.isEmpty()) {
            log.info("No image indexed");
        } else {
            for (FaceRecord face: faceRecs) {
                dynamoService.putItem(face.getFace().getFaceId(),
                        buildExtraDbItemAttributes(dynamoService.getAttrFullName(), name));
            }
            log.info("{} Face Ids are indexed for {}", faceRecs.size(), name);
        }
    }

    /**
     * The Image file to be matched has to be on the server file system.
     *
     * @param file Image file path of the application host file system.
     * @return Matched face name and confidence percentage.
     * @throws Exception
     */
    public Map<String, Float> matchImage(File file) throws Exception {
        boolean matched = false;
        SearchFacesByImageRequest searchRequest = new SearchFacesByImageRequest()
                .withCollectionId(imageCollection)
                .withImage(getImageFromFile(file));

        SearchFacesByImageResult searchResult = client.searchFacesByImage(searchRequest);
        Map<String, Float> matchResult = new HashMap<>();
        for (FaceMatch match: searchResult.getFaceMatches()) {
            log.info("Number of faces matched: {}", searchResult.getFaceMatches().size());
            log.info("Face ID: " + match.getFace().getFaceId());
            String faceId = match.getFace().getFaceId();

            Map<String, AttributeValue> matchedInfo = dynamoService.getItem(faceId);
            String fullName = matchedInfo.get(dynamoService.getAttrFullName()).s();
            float confidence = match.getFace().getConfidence();
            matchResult.put(fullName, confidence);
        }

        return matchResult;
    }

    public Map<String, Float> matchImage(InputStream inputStream) throws Exception {
        ByteBuffer imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
        Image image = new Image().withBytes(imageBytes);

        SearchFacesByImageRequest searchRequest = new SearchFacesByImageRequest()
                .withCollectionId(imageCollection)
                .withImage(image);

        SearchFacesByImageResult searchResult = client.searchFacesByImage(searchRequest);

        Map<String, Float> matchedFaces = new HashMap<>();

        for (FaceMatch match: searchResult.getFaceMatches()) {
            log.info("Number of faces matched: " + searchResult.getFaceMatches().size());
            String faceId = match.getFace().getFaceId();
            Map<String, AttributeValue> map;
            String fullName;
            try {
                map = dynamoService.getItem(faceId);
                fullName = map.get(dynamoService.getAttrFullName()).s();
            } catch (Exception e) {
                continue;
            }
            matchedFaces.put(fullName, match.getFace().getConfidence());
        }

        return matchedFaces;
    }


    private Image getImageFromFile(File file) throws Exception {
        ByteBuffer imageBytes;
        try (InputStream inputStream = new FileInputStream(file)) {
            imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
        }

        return new Image().withBytes(imageBytes);
    }

    private Map<String, AttributeValue> buildExtraDbItemAttributes(String name, String value) {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put(name, AttributeValue.builder().s(value).build());

        return map;
    }

}
