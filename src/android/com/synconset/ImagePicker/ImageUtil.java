package com.synconset;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

public class ImageUtil {

    public static Bitmap decodeFile(File f, int width, int height){
        // decode image size
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
        options.inJustDecodeBounds = false;
        // Find the correct scale value. It should be the power of 2.
        int width_tmp = options.outWidth, height_tmp = options.outHeight;
        int scale = 1;

        int scaleWidth = width_tmp / width;
        int scaleHeight = height_tmp / height;
        scale = scaleHeight > scaleWidth ? scaleWidth : scaleHeight;
        if (scale <= 1) {
            return bitmap;
        }

        // decode with inSampleSize
        options.inSampleSize = scale;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inPurgeable = true;// 同时设置才会有效
        options.inInputShareable = true;//当系统内存不够时候图片自动被回收
        try {
            bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
            return null;
        }
        return bitmap;
    }

    //第一：我们先看下质量压缩方法：
    public static byte[] compressImage(Bitmap image, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, quality, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        long preLength = 0;
        byte[] bytes = baos.toByteArray();
        while (bytes.length / 1024 > 256 && bytes.length != preLength) {    //判断如果图片大于1M,进行压缩避免在生成图,大于继续压缩
            preLength = bytes.length;
            baos.reset();   //重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, quality, baos);//这里压缩options%，把压缩后的数据存放到baos中
            bytes = baos.toByteArray();
        }
        return bytes;
    }

    public static String imgToBase64(String path){
        BufferedInputStream inputStream = null;
        try {
            path = URLDecoder.decode(path, "UTF-8");
            StringBuilder sb = new StringBuilder();

            inputStream = new BufferedInputStream(new FileInputStream(path));
            byte[] buffer = new byte[3096];
            int len;
            while (-1 < (len = inputStream.read(buffer))) {
                sb.append(Base64.encodeToString(buffer, 0, len, Base64.NO_WRAP));
            }

            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return "";
    }

    public static String imgToMd5(File file) {
        String value = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }
}
