package com.app.notes.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Service
public class StorageService {

    @Value("${gcp.credentials.file}")
    private String credentialsFile;

    @Value("${gcp.bucket.name}")
    private String bucketName;

    private Storage storage;

    // Initialize GCP Storage client
    private Storage getStorage() throws IOException {
        if (storage == null) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                new FileInputStream(credentialsFile)
            );
            storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
        }
        return storage;
    }

    // Upload file to GCP Storage with custom path
    public String uploadFile(MultipartFile file, String objectPath) throws IOException {
        BlobId blobId = BlobId.of(bucketName, objectPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType(file.getContentType())
            .build();
        
        getStorage().create(blobInfo, file.getBytes());
        
        // Return public URL with properly encoded path
        String encodedPath = URLEncoder.encode(objectPath, StandardCharsets.UTF_8)
            .replace("+", "%20"); // Replace + with %20 for proper URL encoding
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, encodedPath);
    }

    // Upload file to GCP Storage (legacy - random filename)
    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        return uploadFile(file, fileName);
    }

    // Delete file from GCP Storage
    public boolean deleteFile(String fileName) throws IOException {
        BlobId blobId = BlobId.of(bucketName, fileName);
        return getStorage().delete(blobId);
    }

    // Generate signed URL (temporary access, expires in 1 hour)
    public String getSignedUrl(String fileName) throws IOException {
        BlobId blobId = BlobId.of(bucketName, fileName);
        Blob blob = getStorage().get(blobId);
        
        if (blob == null) {
            return null;
        }

        URL signedUrl = blob.signUrl(1, TimeUnit.HOURS);
        return signedUrl.toString();
    }
}
