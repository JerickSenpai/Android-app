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

    public static Bitmap generateStudentQRCode(int studentId) {
        return generateStudentQRCode(studentId, DEFAULT_QR_SIZE);
    }

    public static Bitmap generateStudentQRCode(int studentId, int size) {
        if (studentId <= 0) {
            Log.e(TAG, "Invalid student ID: " + studentId);
            return null;
        }

        if (size < MIN_QR_SIZE || size > MAX_QR_SIZE) {
            Log.w(TAG, "QR size out of range, using default");
            size = DEFAULT_QR_SIZE;
        }

        try {
            String qrContent = createQRContent(studentId);
            return generateQRCodeBitmap(qrContent, size);
        } catch (Exception e) {
            Log.e(TAG, "Error generating QR code for student ID: " + studentId, e);
            return null;
        }
    }

    public static Bitmap generateQRCodeBitmap(String content, int size) {
        if (content == null || content.trim().isEmpty()) {
            Log.e(TAG, "QR content is null or empty");
            return null;
        }

        if (size < MIN_QR_SIZE || size > MAX_QR_SIZE) {
            size = DEFAULT_QR_SIZE;
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 2);

            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;

        } catch (WriterException | OutOfMemoryError e) {
            Log.e(TAG, "Error generating QR code", e);
            return null;
        }
    }

    private static String createQRContent(int studentId) {
        return "STUDENT:" + studentId;
    }

    public static int extractStudentIdFromQR(String qrContent) {
        if (qrContent == null || qrContent.trim().isEmpty()) {
            return -1;
        }

        try {
            if (qrContent.startsWith("STUDENT:")) {
                return Integer.parseInt(qrContent.substring(8).trim());
            }
            return Integer.parseInt(qrContent.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static boolean isValidStudentQR(String qrContent) {
        return extractStudentIdFromQR(qrContent) > 0;
    }

    public static Bitmap generatePreviewQRCode(int studentId) {
        return generateStudentQRCode(studentId, 200);
    }
}
