package com.xzjmyk.pm.activity.util.imageloader;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.common.LogUtil;
import com.xzjmyk.pm.activity.ui.groupchat.SelectContactsActivity;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class BitmapUtil {

    public static boolean saveBitmapToSDCard(Bitmap bmp, String strPath) {
        if (bmp == null) {
            return false;
        }
        if (TextUtils.isEmpty(strPath)) {
            return false;
        }
        try {
            File file = new File(strPath.substring(0, strPath.lastIndexOf("/")));
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(strPath);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = BitmapUtil.bitampToByte(bmp);
            fos.write(buffer);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static byte[] bitampToByte(Bitmap bitmap) {
        byte[] array = null;
        try {
            if (null != bitmap) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                array = os.toByteArray();
                os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return array;
    }


    public static Drawable getScaleDraw(String imgPath, Context mContext) {
        Bitmap bitmap = null;
        try {
            Log.d("BitmapUtil",
                    "[getScaleDraw]imgPath is " + imgPath.toString());
            File imageFile = new File(imgPath);
            if (!imageFile.exists()) {
                Log.d("BitmapUtil", "[getScaleDraw]file not  exists");
                return null;
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imgPath, opts);

            opts.inSampleSize = computeSampleSize(opts, -1, 800 * 480);
            // Log.d("BitmapUtil","inSampleSize===>"+opts.inSampleSize);
            opts.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(imgPath, opts);

        } catch (OutOfMemoryError err) {
            Log.d("BitmapUtil", "[getScaleDraw] out of memory");

        }
        if (bitmap == null) {
            return null;
        }
        Drawable resizeDrawable = new BitmapDrawable(mContext.getResources(),
                bitmap);
        return resizeDrawable;
    }

    public static void saveMyBitmap(Context mContext, Bitmap bitmap,
                                    String desName) throws IOException {
        FileOutputStream fOut = null;
        Log.i("Arison", "Environment.getExternalStorageState()=" + Environment.getExternalStorageState());
        Log.i("Arison", "Environment.MEDIA_MOUNTED=" + Environment.MEDIA_MOUNTED);
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            LogUtil.d("sdcard doesn't exit, save png to app dir");
            fOut = mContext.openFileOutput(desName + ".png",
                    Context.MODE_PRIVATE);
        } else {
            Log.i("Arison", "path=" + Environment.getExternalStorageDirectory()
                    .getPath());
            File dir = new File(Environment.getExternalStorageDirectory()
                    .getPath() + "/demo/");
            dir.mkdir();
            File f = new File(Environment.getExternalStorageDirectory()
                    .getPath() + "/demo/" + desName + ".png");
            f.createNewFile();
            fOut = new FileOutputStream(f);
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public Bitmap getScaleBitmap(Resources res, int id) {

        Bitmap bitmap = null;
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, id, opts);

            opts.inSampleSize = computeSampleSize(opts, -1, 800 * 480);
            // Log.d("BitmapUtil","inSampleSize===>"+opts.inSampleSize);
            opts.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeResource(res, id, opts);
        } catch (OutOfMemoryError err) {
            Log.d("BitmapUtil", "[getScaleBitmap] out of memory");

        }
        return bitmap;
    }

    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {

        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;

    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {

        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
                Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }

    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap decodeBitmap(Resources res, int id) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 通过这个bitmap获取图片的宽和高
        Bitmap bitmap = BitmapFactory.decodeResource(res, id, options);
        if (bitmap == null) {
            LogUtil.d("bitmap为空");
        }
        float realWidth = options.outWidth;
        float realHeight = options.outHeight;
        LogUtil.d("真实图片高度：" + realHeight + "宽度:" + realWidth);
        // 计算缩放比
        int scale = (int) ((realHeight > realWidth ? realHeight : realWidth) / 100);
        if (scale <= 0) {
            scale = 1;
        }
        LogUtil.d("scale=>" + scale);
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        // 注意这次要把options.inJustDecodeBounds 设为 false,这次图片是要读取出来的。
        bitmap = BitmapFactory.decodeResource(res, id, options);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        LogUtil.d("缩略图高度：" + h + "宽度:" + w);
        return bitmap;
    }

    public static Bitmap getCombineBitmaps(List<SelectContactsActivity.MyBitmapEntity> mEntityList,
                                           Bitmap... bitmaps) {
        LogUtil.d("count=>" + mEntityList.size());
        Bitmap newBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        LogUtil.d("newBitmap=>" + newBitmap.getWidth() + ","
                + newBitmap.getHeight());
        for (int i = 0; i < mEntityList.size(); i++) {
            LogUtil.d("i==>" + i);
            newBitmap = mixtureBitmap(newBitmap, bitmaps[i], new PointF(
                    mEntityList.get(i).x, mEntityList.get(i).y));
        }
        return newBitmap;
    }

    /**
     * 将多个Bitmap合并成一个图片。
     *
     * @param columns 将多个图合成多少列
     * @param bitmaps ... 要合成的图片
     * @return
     */
    public static Bitmap combineBitmaps(int columns, Bitmap... bitmaps) {
        if (columns <= 0 || bitmaps == null || bitmaps.length == 0) {
            throw new IllegalArgumentException(
                    "Wrong parameters: columns must > 0 and bitmaps.length must > 0.");
        }
        int maxWidthPerImage = 20;
        int maxHeightPerImage = 20;
        for (Bitmap b : bitmaps) {
            maxWidthPerImage = maxWidthPerImage > b.getWidth() ? maxWidthPerImage
                    : b.getWidth();
            maxHeightPerImage = maxHeightPerImage > b.getHeight() ? maxHeightPerImage
                    : b.getHeight();
        }
        LogUtil.d("maxWidthPerImage=>" + maxWidthPerImage
                + ";maxHeightPerImage=>" + maxHeightPerImage);
        int rows = 0;
        if (columns >= bitmaps.length) {
            rows = 1;
            columns = bitmaps.length;
        } else {
            rows = bitmaps.length % columns == 0 ? bitmaps.length / columns
                    : bitmaps.length / columns + 1;
        }
        Bitmap newBitmap = Bitmap.createBitmap(columns * maxWidthPerImage, rows
                * maxHeightPerImage, Bitmap.Config.ARGB_8888);
        LogUtil.d("newBitmap=>" + newBitmap.getWidth() + ","
                + newBitmap.getHeight());
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < columns; y++) {
                int index = x * columns + y;
                if (index >= bitmaps.length)
                    break;
                LogUtil.d("y=>" + y + " * maxWidthPerImage=>"
                        + maxWidthPerImage + " = " + (y * maxWidthPerImage));
                LogUtil.d("x=>" + x + " * maxHeightPerImage=>"
                        + maxHeightPerImage + " = " + (x * maxHeightPerImage));
                newBitmap = mixtureBitmap(newBitmap, bitmaps[index],
                        new PointF(y * maxWidthPerImage, x * maxHeightPerImage));
            }
        }
        return newBitmap;
    }


    public static Bitmap mixtureBitmap(Bitmap first, Bitmap second,
                                       PointF fromPoint) {
        if (first == null || second == null || fromPoint == null) {
            return null;
        }
        Bitmap newBitmap = Bitmap.createBitmap(first.getWidth(),
                first.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newBitmap);
        cv.drawBitmap(first, 0, 0, null);
        cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);
        cv.save();//Canvas.ALL_SAVE_FLAG
        cv.restore();
        return newBitmap;
    }

    public static void getScreenWidthAndHeight(Activity mContext) {
        DisplayMetrics metric = new DisplayMetrics();
        mContext.getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels; // 屏幕宽度（像素）
        int height = metric.heightPixels; // 屏幕高度（像素）
        LogUtil.d("screen width=>" + width + ",height=>" + height);
    }


    /**
     * 保存文件
     *
     * @param bm
     * @param fileName
     * @throws IOException
     */
    public static File saveFile(Bitmap bm, String fileName) throws IOException {
        String path = com.common.file.FileUtils.getSDRoot() + "/uu/chat/";
//		Log.i("Arison","BitmapUtil:saveFile:306:path:"+path);
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File myCaptureFile = new File(path + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
        bos.flush();
        bos.close();

        return myCaptureFile;
    }

}
