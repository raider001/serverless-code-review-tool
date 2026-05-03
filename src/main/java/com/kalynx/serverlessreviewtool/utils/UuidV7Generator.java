package com.kalynx.serverlessreviewtool.utils;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Generator for UUID v7 (Time-Ordered UUIDs).
 * <p>
 * UUID v7 combines a 48-bit timestamp with random bits to create globally unique,
 * time-ordered identifiers suitable for distributed systems without coordination.
 * <p>
 * Structure:
 * <ul>
 *   <li>48 bits: Unix timestamp in milliseconds</li>
 *   <li>4 bits: Version (0111 = v7)</li>
 *   <li>12 bits: Random data</li>
 *   <li>2 bits: Variant (10 = RFC 4122)</li>
 *   <li>62 bits: Random data</li>
 * </ul>
 */
public class UuidV7Generator {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a new UUID v7.
     *
     * @return UUID v7 as a string in standard format (8-4-4-4-12 hexadecimal)
     */
    public static String generate() {
        long timestamp = System.currentTimeMillis();

        long mostSigBits = (timestamp << 16) | (randomBits(12) & 0xFFF);
        mostSigBits = (mostSigBits & 0xFFFFFFFFFFFF0FFFL) | 0x7000L;

        long leastSigBits = randomBits(62);
        leastSigBits = (leastSigBits & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;

        UUID uuid = new UUID(mostSigBits, leastSigBits);
        return uuid.toString();
    }

    private static long randomBits(int bits) {
        if (bits <= 0 || bits > 64) {
            throw new IllegalArgumentException("bits must be between 1 and 64");
        }

        long value = RANDOM.nextLong();

        if (bits < 64) {
            long mask = (1L << bits) - 1;
            value = value & mask;
        }

        return value;
    }

    public static void main(String[] args) {
        System.out.println("UUID v7 Examples:");
        for (int i = 0; i < 10; i++) {
            System.out.println(generate());
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

