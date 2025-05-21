package io.plugin.httpDemo;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/data")
public class DataController {

    @PostMapping(value = "/submitJson", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> submitJson(@RequestBody Map<String, Object> jsonData) {
        // For demonstration, we'll just print the received data and return it.
        System.out.println("Received JSON data: " + jsonData);
        // You can process the jsonData here as needed.
        return ResponseEntity.ok(jsonData);
    }

    @PostMapping(value = "/submitForm", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MultiValueMap<String, String>> submitForm(@RequestBody MultiValueMap<String, String> formData) {
        // For demonstration, we'll just print the received data and return it.
        System.out.println("Received Form data: " + formData);
        // You can process the formData here as needed.
        // Spring automatically parses x-www-form-urlencoded data into a MultiValueMap
        // when @RequestBody is used with a compatible type.
        return ResponseEntity.ok(formData);
    }
}
