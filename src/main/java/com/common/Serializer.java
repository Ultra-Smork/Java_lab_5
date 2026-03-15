package com.common;

import java.io.*;

/**
 * Utility class for serializing and deserializing objects.
 * 
 * This class converts Java objects to byte arrays (serialization)
 * and back (deserialization) using Java's built-in ObjectOutputStream.
 * This is used for sending objects over TCP sockets.
 * 
 * Note: All objects being serialized must implement Serializable interface.
 * This includes Request, Response, ServerStats, and MusicBand.
 */
public class Serializer {

    /**
     * Converts a Serializable object to a byte array.
     * 
     * This method takes any object that implements Serializable and
     * converts it to a byte array that can be sent over a network
     * or stored in a file.
     * 
     * @param obj The object to serialize (must implement Serializable)
     * @return Byte array representation of the object
     * @throws IOException If serialization fails
     */
    public static byte[] serialize(Serializable obj) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            oos.flush();
            return baos.toByteArray();
        }
    }

    /**
     * Converts a byte array back to a Java object.
     * 
     * This method takes a byte array (from serialize()) and converts
     * it back to the original object.
     * 
     * @param data The byte array to deserialize
     * @return The deserialized object
     * @throws IOException If deserialization fails
     * @throws ClassNotFoundException If the class of the object cannot be found
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (T) ois.readObject();
        }
    }

    /**
     * Serializes an object and prepends a 4-byte length header.
     * 
     * This is useful for protocols where you need to know how many
     * bytes to read before deserializing. The length is stored as
     * a 4-byte integer (big-endian).
     * 
     * @param obj The object to serialize
     * @return Byte array with length header + serialized data
     * @throws IOException If serialization fails
     */
    public static byte[] serializeWithLength(Serializable obj) throws IOException {
        byte[] data = serialize(obj);
        byte[] length = intToBytes(data.length);
        byte[] result = new byte[4 + data.length];
        System.arraycopy(length, 0, result, 0, 4);
        System.arraycopy(data, 0, result, 4, data.length);
        return result;
    }

    /**
     * Deserializes data that was serialized with serializeWithLength.
     * 
     * Reads the 4-byte length header first, then reads that many bytes
     * and deserializes the object.
     * 
     * @param data The byte array with length header + serialized data
     * @return The deserialized object
     * @throws IOException If deserialization fails or data is too short
     * @throws ClassNotFoundException If the class cannot be found
     */
    public static byte[] deserializeWithLength(byte[] data) throws IOException, ClassNotFoundException {
        if (data.length < 4) {
            throw new IOException("Invalid data: too short for length header");
        }
        int length = bytesToInt(data);
        if (data.length < 4 + length) {
            throw new IOException("Invalid data: insufficient data for declared length");
        }
        byte[] payload = new byte[length];
        System.arraycopy(data, 4, payload, 0, length);
        return deserialize(payload);
    }

    /**
     * Converts an integer to 4 bytes (big-endian).
     * Used for the length header in serializeWithLength.
     * 
     * @param value The integer to convert
     * @return 4-byte array representation
     */
    private static byte[] intToBytes(int value) {
        return new byte[] {
            (byte) (value >>> 24),
            (byte) (value >>> 16),
            (byte) (value >>> 8),
            (byte) value
        };
    }

    /**
     * Converts 4 bytes to an integer (big-endian).
     * Used for reading the length header in deserializeWithLength.
     * 
     * @param bytes The 4 bytes to convert
     * @return Integer representation
     */
    private static int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
               ((bytes[1] & 0xFF) << 16) |
               ((bytes[2] & 0xFF) << 8) |
               (bytes[3] & 0xFF);
    }
}
