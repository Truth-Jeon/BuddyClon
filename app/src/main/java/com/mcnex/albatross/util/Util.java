package com.mcnex.albatross.util;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by kyrie on 2017-07-17.
 */

public class Util {

    /**
     * @param bytes
     * @param order
     * @return
     */
    public static int byteToInt(byte[] bytes, ByteOrder order) {

        ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE/8);
        buff.order(order);

        buff.put(bytes);
        buff.flip();

        return buff.getInt();
    }


    public static byte[] intToByteArray(int integer) {
        ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8);
        buff.putInt(integer);
        buff.order(ByteOrder.BIG_ENDIAN);
        //buff.order(ByteOrder.LITTLE_ENDIAN);
        return buff.array();
    }

    public static String printHEX(byte c)
    {
        String str = String.format("%02X", c);
        return str;
//        }

    }

    public static String formatNumber2(int num) {
        String str = String.format("%02d", num);
        return str;
    }

    public static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }

        for(byte aa :data){
            Log.d("hexStringToByteArray", "hexStringToByteArray:  " +  printHEX(aa));
        }

        return data;
    }

    public static short toInt16(byte[] bArr, int i) {
        return (short) (((bArr[i + 1] & 255) << 8) | (bArr[i] & 255));
    }

}


