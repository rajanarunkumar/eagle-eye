package com.atoz.aws.service;

import com.amazonaws.util.IOUtils;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.AmazonServiceException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.sync.RequestBody;
import software.amazon.awssdk.sync.StreamingResponseHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Component
@Data
public class S3AccessService {
    private static final Logger log = LoggerFactory.getLogger(S3AccessService.class);

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.bucket.folder}")
    private String folderName;

    private S3Client s3;

    public S3AccessService() {
        s3 = S3Client.create();
    }

    public void uploadFile(String key, String filePath, Map<String, String> metaData) {

        File file = new File(filePath);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(buildKeyWithFolder(key))
                    .metadata(metaData).build();

            s3.putObject(request, RequestBody.of(file));
        } catch (AmazonServiceException e) {
            log.error("Amazeon service error: {}", e.getErrorMessage());
        }
    }

    public void uploadInputStreram(String key, InputStream inputStream, Map<String, String> metaData)
            throws IOException, AmazonServiceException {

        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(buildKeyWithFolder(key))
                    .metadata(metaData)
                    .build();

            RequestBody body = RequestBody.of(bytes);

            s3.putObject(request, body);
        } catch (IOException ioe) {
            log.error("Input stream cannot read to a byte stream: {}", ioe.getMessage());
            throw ioe;
        } catch (AmazonServiceException e) {
            log.error("Amazeon service error: {}", e.getErrorMessage());
            throw e;
        }
    }

    public void downLoadFile(String key, String destFilePath) {
        String keyWithFolder = buildKeyWithFolder(key);
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(keyWithFolder).build();

        String filePath = buildFilePath(destFilePath, key);
        log.info("Download file destination: {}", filePath);
        s3.getObject(request, StreamingResponseHandler.toFile(Paths.get(filePath)));
    }

    public void deleteFile(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(bucketName).key(buildKeyWithFolder(key)).build();
        s3.deleteObject(request);
    }

    public boolean isObjectExists(String key) {
        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .maxKeys(100)
                .build();
        ListObjectsV2Response listRes = s3.listObjectsV2(listReq);
        List<S3Object> objects = listRes.contents();
        objects.stream().forEach(s3Object -> System.out.println(s3Object.key()));
        if (objects.isEmpty()) {
            return false;
        } else {
            return objects.stream().filter(p -> p.key().equals(buildKeyWithFolder(key))).findAny().isPresent();
        }
    }



    private String buildFilePath(String destFilePathOrDir, String key) {
        if (destFilePathOrDir.endsWith("/")) {
            return destFilePathOrDir + key;
        } else {
            return destFilePathOrDir;
        }
    }

    private String buildKeyWithFolder(String key) {
        if (StringUtils.isEmpty(folderName)) {
            return key;
        } else {
            return folderName + "/" + key;
        }
    }

    public ByteOutputStream getObject(String key)throws Exception{
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(key).build();
        try(ByteOutputStream outputStream = new ByteOutputStream()){
            s3.getObject(GetObjectRequest.builder().bucket(bucketName).key(key).build(),StreamingResponseHandler.toOutputStream(outputStream));
            return outputStream;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
