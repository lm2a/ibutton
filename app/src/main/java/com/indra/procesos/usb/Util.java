package com.indra.procesos.usb;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mlamenza on 24/10/2017.
 */

public class Util {

    private static final int sizeOfIntInHalfBytes = 8;
    private static final int numberOfBitsInAHalfByte = 4;
    private static final int halfByte = 0x0F;
    private static final char[] hexDigits = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static String decToHex(int dec) {
        StringBuilder hexBuilder = new StringBuilder(sizeOfIntInHalfBytes);
        hexBuilder.setLength(sizeOfIntInHalfBytes);
        for (int i = sizeOfIntInHalfBytes - 1; i >= 0; --i)
        {
            int j = dec & halfByte;
            hexBuilder.setCharAt(i, hexDigits[j]);
            dec >>= numberOfBitsInAHalfByte;
        }
        return hexBuilder.toString();
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    public static String pageToStartingMemory0x(int page){
        int pageStart = page*32;
        return "0x" + Integer.toHexString(pageStart);
    }

    public static long pageToStartingMemory(int page){
        int pageStart = page*32;
        String pageStartHexa = Integer.toHexString(pageStart);
        return Long.parseLong(pageStartHexa, 16);
    }


    public static String pageToEndingMemory0x(int page){
        int pageEnd = page*32;
        return "0x" + Integer.toHexString(pageEnd);
    }

    public static long pageToEndingMemory(int page){
        int pageEnd = page*32+32;
        String pageEndHexa = Integer.toHexString(pageEnd);
        return  Long.parseLong(pageEndHexa, 16);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * este metodo muestra el contenido de la memoria (en java cada byte es signed) en hexa
     * @param bytes
     */
    public void printBytesAsHexa(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        System.out.println(sb.toString());
    }


    /**
     * este metodo muestra el contenido de la memoria (en java cada byte es signed) en hexa
     * @param bytes
     */
    public static String bytesAsHexa(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    public static List<String> splitEqually(String text, int size) {
        // Give the list the right capacity to start with. You could use an array
        // instead if you wanted.
        List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);

        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size))+" ");
        }
        return ret;
    }
}
