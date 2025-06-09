package com.example.ANONIMUS.controller;

import com.example.ANONIMUS.dto.BulkQrRequest;
import com.example.ANONIMUS.dto.BulkQrResult;
import com.example.ANONIMUS.dto.QrGenerationRequest;
import com.example.ANONIMUS.dto.QrResponse;
import com.example.ANONIMUS.model.QrEntity;
import com.example.ANONIMUS.service.QrService;
import com.example.ANONIMUS.service.RequestCounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/qr")
public class QrController {

    private final QrService qrService;

    public QrController(QrService qrService) {
        this.qrService = qrService;
    }

    @GetMapping
    @Operation(summary = "Generate QR code", description = "Creates QR code from text")
    @ApiResponse(responseCode = "200", description = "QR generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    public ResponseEntity<QrResponse> generateQrCode(
            @RequestParam String text,
            @RequestParam String username) {

        if (text == null || text.trim().isEmpty() || username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(qrService.generateAndSaveQrCode(text, username));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Bulk generate QR codes", description = "Creates multiple QR codes from a list of text/username pairs")
    @ApiResponse(responseCode = "200", description = "QR codes generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input payload")
    public ResponseEntity<List<BulkQrResult>> generateBulkQrCodes(@RequestBody BulkQrRequest bulkRequest) {
        if (bulkRequest == null || bulkRequest.getRequests() == null || bulkRequest.getRequests().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonList(
                    BulkQrResult.failure(null, null, "Request body or list of requests is empty")
            ));
        }

        // Basic validation for each item
        boolean hasInvalidItem = bulkRequest.getRequests().stream()
                .anyMatch(req -> req.getText() == null || req.getText().trim().isEmpty() ||
                        req.getUsername() == null || req.getUsername().trim().isEmpty());

        if (hasInvalidItem) {
            return ResponseEntity.badRequest().body(
                    bulkRequest.getRequests().stream()
                            .map(req -> {
                                if (req.getText() == null || req.getText().trim().isEmpty() ||
                                        req.getUsername() == null || req.getUsername().trim().isEmpty()) {
                                    return BulkQrResult.failure(req.getText(), req.getUsername(), "Invalid text or username");
                                }
                                return null; // Placeholder for valid items, will be processed by service
                            })
                            .filter(result -> result != null) // Only keep the failure markers for the response
                            .collect(Collectors.toList()) // Or handle differently depending on desired error reporting
            );
            // Consider a more granular error response if needed
        }


        List<BulkQrResult> results = qrService.generateBulkQrCodes(bulkRequest.getRequests());
        return ResponseEntity.ok(results);
    }


    @GetMapping("/by-user")
    @Operation(summary = "Get QR codes by username", description = "Retrieves all QR codes associated with a specific username")
    @ApiResponse(responseCode = "200", description = "QR codes retrieved successfully")
    @ApiResponse(responseCode = "404", description = "User not found or no QR codes associated") // Assuming service handles this
    public ResponseEntity<List<QrEntity>> getQrCodesByUser(
            @RequestParam String username) {
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<QrEntity> qrCodes = qrService.getQrCodesByUsername(username);
        // Decide if an empty list warrants a 404 or just an empty 200
        // if (qrCodes.isEmpty()) {
        //     return ResponseEntity.notFound().build();
        // }
        return ResponseEntity.ok(qrCodes);
    }
    @Autowired
    private RequestCounterService requestCounterService;

    @GetMapping("/request-count")
    @Operation(summary = "Get request count", description = "Returns total number of requests handled")
    public ResponseEntity<Long> getRequestCount() {
        return ResponseEntity.ok(requestCounterService.getCount());
    }

    @PostMapping("/reset-count")
    @Operation(summary = "Reset request count", description = "Resets the request counter to zero")
    public ResponseEntity<Void> resetRequestCount() {
        requestCounterService.reset();
        return ResponseEntity.noContent().build();
    }
}