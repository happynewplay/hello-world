package io.plugin.httpDemo;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpUtilDemo {

    private static final String BASE_URL = "http://localhost:8080/api"; // Assuming default Spring Boot port

    public static void main(String[] args) {
        System.out.println("Starting HttpUtilDemo...");

        // Demo: File Upload
        demoFileUpload();

        // Demo: JSON Data Submission
        demoJsonDataSubmission();

        // Demo: Form Data Submission
        demoFormDataSubmission();

        System.out.println("HttpUtilDemo finished.");
    }

    private static void demoFileUpload() {
        System.out.println("\n--- File Upload Demo ---");
        File tempFile = null;
        try {
            // 1. Create a sample file
            tempFile = FileUtil.createTempFile("sample", ".txt", FileUtil.getTmpDir(), true);
            FileUtil.writeString("This is a test file for HttpUtil upload demo.", tempFile, StandardCharsets.UTF_8);
            System.out.println("Created temporary file: " + tempFile.getAbsolutePath());

            // 2. Upload the file
            String uploadUrl = BASE_URL + "/files/upload";
            System.out.println("Uploading to: " + uploadUrl);

            HttpResponse response = HttpRequest.post(uploadUrl)
                    .form("file", tempFile) // "file" must match @RequestParam name in Controller
                    .timeout(20000) // Optional: set timeout
                    .execute();

            System.out.println("Upload response status: " + response.getStatus());
            System.out.println("Upload response body: " + response.body());

            if (response.isOk()) {
                String responseBody = response.body();
                // Assuming server responds with "File uploaded successfully: <filename>"
                if (responseBody.startsWith("File uploaded successfully: ")) {
                    String uploadedFileName = responseBody.substring("File uploaded successfully: ".length());
                    System.out.println("Uploaded filename from server: " + uploadedFileName);
                    // 3. Demo: File Download
                    demoFileDownload(uploadedFileName, tempFile.length());
                } else {
                    System.err.println("Upload successful, but response format unexpected: " + responseBody);
                }
            } else {
                System.err.println("File upload failed.");
            }

        } catch (HttpException e) {
            System.err.println("HTTP Exception during file upload/download: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Exception during file upload/download demo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (tempFile != null && tempFile.exists()) {
                FileUtil.del(tempFile);
                System.out.println("Deleted temporary file: " + tempFile.getAbsolutePath());
            }
        }
    }

    private static void demoFileDownload(String filename, long originalFileSize) {
        System.out.println("\n--- File Download Demo (called from upload) ---");
        File downloadedFile = null;
        try {
            String downloadUrl = BASE_URL + "/files/download/" + filename;
            System.out.println("Downloading from: " + downloadUrl);

            downloadedFile = FileUtil.createTempFile("downloaded_", "_" + filename, FileUtil.getTmpDir(), true);

            long size = HttpUtil.downloadFile(downloadUrl, downloadedFile);

            System.out.println("File downloaded to: " + downloadedFile.getAbsolutePath());
            System.out.println("Downloaded file size: " + size + " bytes");

            if (size == originalFileSize) {
                System.out.println("File download successful and size matches original.");
                // Optionally, compare content for small files
                // String originalContent = FileUtil.readString(tempFile, StandardCharsets.UTF_8);
                // String downloadedContent = FileUtil.readString(downloadedFile, StandardCharsets.UTF_8);
                // if (originalContent.equals(downloadedContent)) {
                //     System.out.println("File content verified.");
                // } else {
                //     System.err.println("File content mismatch!");
                // }
            } else {
                System.err.println("File download size mismatch! Original: " + originalFileSize + ", Downloaded: " + size);
            }

        } catch (HttpException e) {
            System.err.println("HTTP Exception during file download: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Exception during file download demo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (downloadedFile != null && downloadedFile.exists()) {
                 FileUtil.del(downloadedFile);
                 System.out.println("Deleted temporary downloaded file: " + downloadedFile.getAbsolutePath());
            }
        }
    }

    private static void demoJsonDataSubmission() {
        System.out.println("\n--- JSON Data Submission Demo ---");
        try {
            String jsonUrl = BASE_URL + "/data/submitJson";
            System.out.println("Submitting JSON to: " + jsonUrl);

            Map<String, Object> jsonData = new HashMap<>();
            jsonData.put("name", "Test User");
            jsonData.put("value", 123);
            jsonData.put("active", true);
            String jsonString = JSONUtil.toJsonStr(jsonData);
            System.out.println("Sending JSON: " + jsonString);

            HttpResponse response = HttpRequest.post(jsonUrl)
                    .body(jsonString, "application/json") // Set content type for JSON
                    .timeout(5000)
                    .execute();

            System.out.println("JSON submission response status: " + response.getStatus());
            System.out.println("JSON submission response body: " + response.body());

            if (!response.isOk()) {
                System.err.println("JSON data submission failed.");
            } else {
                System.out.println("JSON data submission successful.");
            }

        } catch (HttpException e) {
            System.err.println("HTTP Exception during JSON submission: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Exception during JSON submission demo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void demoFormDataSubmission() {
        System.out.println("\n--- Form Data Submission Demo ---");
        try {
            String formUrl = BASE_URL + "/data/submitForm";
            System.out.println("Submitting Form data to: " + formUrl);

            Map<String, Object> formData = MapUtil.builder(new HashMap<String, Object>())
                    .put("username", "johndoe")
                    .put("email", "johndoe@example.com")
                    .put("id", "789")
                    .build();
            
            System.out.println("Sending Form data: " + formData);

            // HttpUtil.post automatically sets Content-Type to application/x-www-form-urlencoded for maps
            HttpResponse response = HttpRequest.post(formUrl)
                    .form(formData)
                    .timeout(5000)
                    .execute();
            
            System.out.println("Form data submission response status: " + response.getStatus());
            System.out.println("Form data submission response body: " + response.body());

            if (!response.isOk()) {
                System.err.println("Form data submission failed.");
            } else {
                System.out.println("Form data submission successful.");
            }

        } catch (HttpException e) {
            System.err.println("HTTP Exception during Form data submission: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Exception during Form data submission demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
