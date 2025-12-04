package com.radyfy.common.service.filestore;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.radyfy.common.controller.FileController;
import io.sentry.Sentry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    @Value("${app.s3bucket.entity_assets}")
    private String bucketName;

    public final AmazonS3 s3client;

    // private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(
        // FileStorageProperties fileStorageProperties
        ) {
        // this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
        //         .toAbsolutePath().normalize();

        // try {
        //     Files.createDirectories(this.fileStorageLocation);
        // } catch (Exception ex) {
        //     throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        // }

        this.s3client = AmazonS3ClientBuilder.standard()
                .build();
    }

    public void storeFile(MultipartFile file, String fileName) {
        // Normalize file name
        // String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            uploadMultipartFile(file, fileName);

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    private void uploadMultipartFile(MultipartFile file, String fileName) throws IOException {

        // setting metadata
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        // create PutObjectRequest object with bucket name and file name
        PutObjectRequest putRequest = new PutObjectRequest(bucketName, fileName,
                new ByteArrayInputStream(file.getBytes()), metadata);

        // set public read permission
        // TODO remove public read of customer assets after implementing imagix
//        putRequest.withCannedAcl(CannedAccessControlList.PublicRead);

        // upload file to S3
        s3client.putObject(putRequest);
    }

    public byte[] downloadS3File(String fileName){
        try {
            // check if file name is null or empty
            if(fileName == null || fileName.isEmpty()){
                throw new RuntimeException("File name is required");
            }
            // remove leading slash if present
            if(fileName.startsWith("/")){
                fileName = fileName.substring(1);
            }
            // get file from S3
            S3Object s3Object = s3client.getObject(bucketName, fileName);
            

            // create byte array output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // write file content to byte array output stream
            try (InputStream inputStream = s3Object.getObjectContent()) {
                byte[] buffer = new byte[4096];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                logger.debug(e.getMessage(), e);
            }

            return outputStream.toByteArray();
        } catch (com.amazonaws.services.s3.model.AmazonS3Exception e) {
            Sentry.setExtra("fileName", fileName);
            Sentry.captureException(e);
            logger.warn("File not found in S3: {} - {}", fileName, e.getMessage());
            return new byte[0]; // Return empty byte array when file doesn't exist
        }
    }

    // public Resource loadFileAsResource(String fileName) {
    //     try {
    //         Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
    //         Resource resource = new UrlResource(filePath.toUri());
    //         if (resource.exists()) {
    //             return resource;
    //         } else {
    //             throw new MyFileNotFoundException("File not found " + fileName);
    //         }
    //     } catch (MalformedURLException ex) {
    //         throw new MyFileNotFoundException("File not found " + fileName, ex);
    //     }
    // }
}
