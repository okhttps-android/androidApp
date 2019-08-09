package com.common.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.common.file.FileUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

/**
 * Created by Arisono on 2016/5/25.
 * 处理图片工具类
 */
public class ImageUtil {


    /**
     * 在部分Android手机（如MT788、Note2）上，
     * 使用Camera拍照以后，得到的照片会被自动旋转（90°、180°、270°）
     * ，这个情况很不符合预期。仔细分析了一下，
     * 因为照片属性中是存储了旋转信息的，
     * 所以要解决这个问题，可以在onActivityResult方法中，
     * 获取到照片数据后，读取它的旋转信息，如果不是0，
     * 说明这个照片已经被旋转过了，那么再使用android.graphics.Matrix将照片旋转回去即可。
     */
    public static Bitmap roateBitmapAndScale(Bitmap bitmap, int degree, int newWidth, int newHeight) {
        if (degree == 0) {
            return bitmap;
        }
        // 获得图片的宽高
        int width = bitmap.getWidth();
        // int height = bitmap.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        //float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree); //解决旋转问题
        matrix.postScale(scaleWidth, scaleWidth);//等比例缩放
        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return bmp;
    }


    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度 getBitmapDegree()
     * @return 旋转后的图片  无等比例缩放
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    /**
     * 读取图片的旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    private int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }


    /**
     * @功能:计算图片的缩放值
     * @param:
     * @author:Arisono
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (reqHeight == 0 || reqWidth == 0) return 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }


    /**
     * 压缩已存在的图片对象，并返回压缩后的图片(压缩质量，压缩尺寸函数)
     *
     * @param bitmap                   ：图片对象
     * @param quality:1-100;100表示不质量压缩
     * @param reqsW：压缩宽度
     * @param reqsH：压缩高度
     * @return
     */
    public final static Bitmap compressBitmap(Bitmap bitmap, int quality, int reqsW, int reqsH) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();//放入内存
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, baos);//压缩质量
            byte[] bts = baos.toByteArray();
            Bitmap res = compressBitmapWithByte(bts, reqsW, reqsH);//压缩尺寸
            baos.close();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }
    }


    /**
     * 压缩已存在的图片对象，并返回压缩后的图片(压缩质量，压缩尺寸函数)
     *
     * @param path                     ：图片对象
     * @param quality:1-100;100表示不质量压缩
     * @param reqsW：压缩宽度
     * @param reqsH：压缩高度
     * @return Bitmap
     */
    public final static Bitmap compressBitmap(String path, int quality, int reqsW, int reqsH) {
        Bitmap bitmap = compressBitmapWithFilePath(path, reqsW, reqsH);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();//放入内存
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);//压缩质量
            byte[] bts = baos.toByteArray();
            Bitmap res = bytes2Bimap(bts);
            baos.close();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }
    }


    /**
     * 压缩已存在的图片对象，并返回压缩后的图片文件(压缩质量，压缩尺寸函数)
     *
     * @param ：图片对象
     * @param quality:1-100;100表示不质量压缩
     * @param reqsW：压缩宽度
     * @param reqsH：压缩高度
     * @return Bitmap
     */
    public final static File compressBitmapToFile(String filePath, int quality, int reqsW, int reqsH) {
        Bitmap bitmap = compressBitmapWithFilePath(filePath, reqsW, reqsH);
        try {
            // ByteArrayOutputStream baos = new ByteArrayOutputStream();//放入内存
            File file = new File(filePath);
            BufferedOutputStream baos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);//压缩质量
            baos.flush();
            baos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return new File(filePath);
        }catch (Exception e){
            e.printStackTrace();
            return new File(filePath);
        }
    }


    /**
     * 压缩已存在的图片对象，并添加水印，并返回压缩后的图片文件(压缩质量，压缩尺寸函数)
     *
     * @author RaoMeng
     * @param ：图片对象
     * @param quality:1-100;100表示不质量压缩
     * @param reqsW：压缩宽度
     * @param reqsH：压缩高度
     * @param time:                    照片拍摄时间
     * @param address：照片拍摄地点
     * @param alpha：水印透明度
     * @return 添加水印的图片文件
     */
    public final static File compressWaterBitmapToFile(String filePath, int quality, int reqsW, int reqsH, String nickName, String time, String address, int alpha) {
        Bitmap bitmap = compressBitmapWithFilePath(filePath, reqsW, reqsH);
        try {
            TextPaint paint = new TextPaint();
            paint.setAlpha(alpha);
            paint.setAntiAlias(true);
            paint.setShadowLayer(2, 2, 2, Color.BLACK);
            paint.setColor(Color.WHITE);
            paint.setTypeface(Typeface.create("宋体", Typeface.NORMAL));
            //如果不用copy的方法，直接引用会对资源文件进行修改，而Android是不允许在代码里修改res文件里的图片
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            Rect bounds = new Rect();
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(bitmap, 0, 0, null);

            String nameTime = nickName + "    " + time;
            paint.setTextSize(16);
            paint.getTextBounds(nameTime, 0, nameTime.length(), bounds);
            canvas.drawText(nameTime, canvas.getWidth() - 10 - paint.measureText(nameTime), canvas.getHeight() - 60, paint);

            canvas.save();
            paint.setTextSize(16);
//            paint.getTextBounds(address, 0, address.length(), bounds);
            //文字过长时自动换行
            StaticLayout staticLayout = new StaticLayout(address, paint, canvas.getWidth() - 20, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
            float startX = 0;
            if (paint.measureText(address) > canvas.getWidth() - 10) {
                startX = 10;
            } else {
                startX = canvas.getWidth() - 10 - paint.measureText(address);
            }
            canvas.translate(startX, canvas.getHeight() - 50);
            staticLayout.draw(canvas);
            canvas.restore();
            /*if (paint.measureText(address) <= (float) (canvas.getWidth() - 20)) {
                canvas.drawText(address, canvas.getWidth() - 10 - paint.measureText(address), canvas.getHeight() - 30, paint);
            } else {
                String subAddress = address.substring(0, (int) (((canvas.getWidth() - 25) / paint.measureText(address)) * address.length())) + "...";
                paint.getTextBounds(subAddress, 0, subAddress.length(), bounds);
                canvas.drawText(subAddress,canvas.getWidth() - 10 - paint.measureText(subAddress), canvas.getHeight() - 30, paint);
            }*/

            File file = new File(filePath);
            BufferedOutputStream baos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);//压缩质量
            baos.flush();
            baos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 压缩已存在的图片对象，并返回压缩后的图片文件(压缩质量，压缩尺寸函数)
     * @param bitmap ：图片对象
     * @param quality:1-100;100表示不质量压缩
     * @param reqsW：压缩宽度
     * @param reqsH：压缩高度
     * @return Bitmap
     */
    /*public final static File compressBitmapToFile(String filePath, int quality) {
        Bitmap bitmap=compressBitmapWithFilePath(filePath,0,0);
        try {
            // ByteArrayOutputStream baos = new ByteArrayOutputStream();//放入内存
            File file=new File(filePath);
            BufferedOutputStream baos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, baos);//压缩质量
            baos.flush();
            baos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }*/

    /**
     * 压缩指定byte[]图片，并得到压缩后的图像
     *
     * @param bts
     * @param reqsW
     * @param reqsH
     * @return
     */
    public final static Bitmap compressBitmapWithByte(byte[] bts, int reqsW, int reqsH) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bts, 0, bts.length, options);
        options.inSampleSize = calculateInSampleSize(options, reqsW, reqsH);//计算尺寸比例
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bts, 0, bts.length, options);
    }

    /**
     * @功能:压缩指定filePath图片，并得到压缩后的图像
     * @author:Arisono
     * @param:filePath:图片路径
     * @return: Bitmap
     */
    public static Bitmap compressBitmapWithFilePath(String filePath, int reqsW, int reqsH) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, reqsW, reqsH);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * @功能:从资源清单中获取Bitmap
     * @author:Arisono
     * @param:resId:资源id
     * @return: Bitmap
     */
    public static Bitmap compressBitmapWithResources(Context ct, int resId, int reqsW, int reqsH) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(ct.getResources(), resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqsW, reqsH);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(ct.getResources(), resId, options);
    }


    /**
     * Drawable to Bitmap
     * @param drawable
     * @return Bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable){
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0,0,width,height);
        drawable.draw(canvas);
        return bitmap;

    }

    /** 
     * 获取圆角图片
     * @param bitmap
     * @param roundPx
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap,float roundPx){
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /** resId 获取圆角头像
     * @param ct
     * @param resId
     * @param roundPx
     * @return
     */
    public static Bitmap getRoundedCornerBitmapResId(Context ct,int  resId,float roundPx){
        Bitmap mBitmaps= compressBitmapWithResources(ct,resId,0,0);
        return  getRoundedCornerBitmap(mBitmaps,roundPx);
    }
    
    public static Bitmap bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }


    /**
     * 字节数组转换成bitmap
     * @param bytes
     * @return
     */
    public static Bitmap decodeImg(byte[] bytes) {
        Bitmap bitmap = null;

        byte[] imgByte = null;
        InputStream input = null;
        try {
            imgByte = bytes;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            input = new ByteArrayInputStream(imgByte);
            SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(input, null, options));
            bitmap = (Bitmap) softRef.get();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (imgByte != null) {
                imgByte = null;
            }

            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    /**	 * 把batmap 转file	 * @param bitmap	 * @param filepath	 */	
    public static File saveBitmapFile(Bitmap bitmap, String filepath) {
        File file = new File(FileUtils.getSDRoot()+"/uu/");//将要保存图片的路径
        if (!file.exists()){
            file.mkdir();
        }
        File filePng = new File(filepath);
     
         try {
             FileOutputStream fileOutputStream=  new FileOutputStream(filePng);
             BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
             bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos); 		
             bos.flush(); 		
             bos.close(); 
         } catch (IOException e) { 		
             e.printStackTrace(); 
         } 	
         return filePng;
    }
 
}
