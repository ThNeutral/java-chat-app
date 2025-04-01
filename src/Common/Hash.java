package Common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
    public static byte[] GetSHA256Bytes(String input, int size) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fullHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            if (size == fullHash.length) {
                return fullHash;
            } else if (size < fullHash.length) {
                byte[] trimmedHash = new byte[size];
                System.arraycopy(fullHash, 0, trimmedHash, 0, size);
                return trimmedHash;
            } else {
                throw new RuntimeException(size + " is bigger than SHA256 maximum length");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static byte[] GetSHA256Bytes(String input) {
        return GetSHA256Bytes(input, 32);
    }

    public static String GetSHA256String(String input, int size) {
        byte[] hashBytes = GetSHA256Bytes(input, size);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    public static String GetSHA256String(String input) {
        return GetSHA256String(input, 32);
    }
}