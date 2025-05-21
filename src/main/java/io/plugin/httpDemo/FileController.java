package io.plugin.httpDemo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.net.MalformedURLException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final Path uploadDir;

    public FileController() {
        // Define the upload directory path
        String tempDir = System.getProperty("java.io.tmpdir");
        this.uploadDir = Paths.get(tempDir, "uploads");
        try {
            // Create the directory if it doesn't exist
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            // Handle directory creation failure - for now, print stack trace
            // In a real app, you'd want more robust error handling or logging
            e.printStackTrace();
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty. Please select a file to upload.");
        }

        try {
            // Generate a unique filename to avoid collisions
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + extension;

            Path destinationFile = this.uploadDir.resolve(uniqueFileName).normalize().toAbsolutePath();

            // Ensure the destination is within the uploadDir (security measure)
            if (!destinationFile.getParent().equals(this.uploadDir.toAbsolutePath())) {
                return ResponseEntity.status(500).body("Cannot store file outside of the designated directory.");
            }
            
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            // Construct file download URI (optional, depends on requirements)
            // String fileDownloadUri = "/api/files/download/" + uniqueFileName;

            return ResponseEntity.ok("File uploaded successfully: " + uniqueFileName);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage());
        }
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Path filePath = this.uploadDir.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Try to determine content type
                String contentType = null;
                try {
                    contentType = Files.probeContentType(filePath);
                } catch (IOException e) {
                    // log error or handle
                    System.err.println("Could not determine file type for " + filename);
                }

                // Default content type if determination fails
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            // This usually means the filename resulted in a malformed URI
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null); // Or a more specific error
        }
    }
}
