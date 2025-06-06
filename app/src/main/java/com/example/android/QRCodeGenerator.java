package com.example.android;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {
    private static final String TAG = "QRCodeGenerator";
    private static final int DEFAULT_QR_SIZE = 512;
    private static final int MIN_QR_SIZE = 100;
    private static final int MAX_QR_SIZE = 1024;

    /**
     * Generate QR code bitmap from student ID
     * @param studentId The student ID to encode
     * @return Bitmap of the QR code or null if generation fails
     */
    public static Bitmap generateStudentQRCode(int studentId) {
        return generateStudentQRCode(studentId, DEFAULT_QR_SIZE);
    }

    /**
     * Generate QR code bitmap from student ID with custom size
     * @param studentId The student ID to encode
     * @param size The size of the QR code (width and height)
     * @return Bitmap of the QR code or null if generation fails
     */
    public static Bitmap generateStudentQRCode(int studentId, int size) {
        // Validate input parameters
        if (studentId <= 0) {
            Log.e(TAG, "Invalid student ID: " + studentId);
            return null;
        }

        if (size < MIN_QR_SIZE || size > MAX_QR_SIZE) {
            Log.w(TAG, "QR size out of range (" + MIN_QR_SIZE + "-" + MAX_QR_SIZE + "), using default");
            size = DEFAULT_QR_SIZE;
        }

        try {
            // Create the QR code content
            String qrContent = createQRContent(studentId);
            Log.d(TAG, "Generating QR code for student ID: " + studentId + ", size: " + size);

            return generateQRCodeBitmap(qrContent, size);

        } catch (Exception e) {
            Log.e(TAG, "Error generating QR code for student ID: " + studentId, e);
            return null;
        }
    }

    /**
     * Generate QR code from any string content
     * @param content The content to encode
     * @param size The size of the QR code
     * @return Bitmap of the QR code or null if generation fails
     */
    public static Bitmap generateQRCodeBitmap(String content, int size) {
        if (content == null || content.trim().isEmpty()) {
            Log.e(TAG, "QR content cannot be null or empty");
            return null;
        }

        if (size < MIN_QR_SIZE || size > MAX_QR_SIZE) {
            Log.w(TAG, "QR size out of range, using default");
            size = DEFAULT_QR_SIZE;
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            // Set up encoding hints for better quality and error correction
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 2);

            Log.d(TAG, "Encoding QR content: " + content);

            // Generate the QR code matrix
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            if (bitMatrix == null) {
                Log.e(TAG, "Failed to generate BitMatrix");
                return null;
            }

            // Create bitmap from the matrix
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);

            if (bitmap == null) {
                Log.e(TAG, "Failed to create bitmap");
                return null;
            }

            // Fill the bitmap with QR code pattern
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    // Set pixel color: black for QR code, white for background
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            Log.d(TAG, "QR code bitmap generated successfully");
            return bitmap;

        } catch (WriterException e) {
            Log.e(TAG, "WriterException while generating QR code", e);
            return null;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "OutOfMemoryError while generating QR code - size too large: " + size, e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error generating QR code bitmap", e);
            return null;
        }
    }

    /**
     * Create QR content string from student ID
     * You can customize this format based on your needs
     */
    private static String createQRContent(int studentId) {
        // Simple format - just the student ID with a prefix for validation
        return "STUDENT:" + studentId;

        // Alternative: JSON format with timestamp
        // return String.format("{\"student_id\":%d,\"timestamp\":%d,\"type\":\"attendance\"}",
        //                     studentId, System.currentTimeMillis());
    }

    /**
     * Extract student ID from QR code content
     * This should match the format used in createQRContent()
     */
    public static int extractStudentIdFromQR(String qrContent) {
        if (qrContent == null || qrContent.trim().isEmpty()) {
            Log.e(TAG, "QR content is null or empty");
            return -1;
        }

        try {
            // Handle the prefixed format
            if (qrContent.startsWith("STUDENT:")) {
                String idStr = qrContent.substring(8); // Remove "STUDENT:" prefix
                return Integer.parseInt(idStr.trim());
            }

            // Fallback: try to parse as direct number
            return Integer.parseInt(qrContent.trim());

            // If using JSON format, you would parse it here:
            // JSONObject json = new JSONObject(qrContent);
            // return json.getInt("student_id");

        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing student ID from QR content: " + qrContent, e);
            return -1; // Return -1 to indicate invalid ID
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error extracting student ID from QR content", e);
            return -1;
        }
    }

    /**
     * Validate if QR content contains a valid student ID
     */
    public static boolean isValidStudentQR(String qrContent) {
        int studentId = extractStudentIdFromQR(qrContent);
        return studentId > 0; // Assuming student IDs are positive integers
    }

    /**
     * Get a scaled down version of QR code for preview purposes
     */
    public static Bitmap generatePreviewQRCode(int studentId) {
        return generateStudentQRCode(studentId, 200); // Smaller size for previews
    }
}