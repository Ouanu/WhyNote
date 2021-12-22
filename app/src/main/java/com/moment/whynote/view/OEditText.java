package com.moment.whynote.view;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A View which can insert images
 */
public class OEditText extends androidx.appcompat.widget.AppCompatEditText {

    public OEditText(@NonNull @NotNull Context context) {
        super(context);
    }

    public OEditText(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OEditText(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * insert images into edittext
     * @param desc select uri from the description
     */
    public void insertImage(String desc) {
        SpannableStringBuilder builder = new SpannableStringBuilder(desc);
        String rex  = "<([^>]*)>";    //regular expression
        Pattern pattern = Pattern.compile(rex);// mount regular expression
        Matcher matcher = pattern.matcher(desc);
        ContentResolver contentResolver = getContext().getContentResolver();
        Bitmap bitmap;
        String uri;
        while (matcher.find()) {
            uri = matcher.group().replaceAll("[<>]","");
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(uri));
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                // 计算缩放比例.
                float k = ((float) 640) / width;
                // 取得想要缩放的matrix参数.
                Matrix matrix = new Matrix();
                matrix.postScale(k, k);
                // 得到新的图片.
                Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                builder.setSpan(
                        new ImageSpan(getContext(), newBitmap), matcher.start(), matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                this.setText(builder);
                this.setSelection(desc.length());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * save the image
     * @param uri of image
     * @return the uri which image we save
     */

    public Uri saveImage(Uri uri, String path) {
        Toast.makeText(getContext(), path, Toast.LENGTH_SHORT).show();
        String name = String.valueOf(System.currentTimeMillis());
        File saveFile = new File(path, name);
        FileOutputStream saveOutImage;
        try {
            saveOutImage = new FileOutputStream(saveFile);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, saveOutImage);
            saveOutImage.flush();
            saveOutImage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(saveFile);
    }

    /**
     * delete the Image File of item
     * @param path the file which we need to deal with
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void deleteImage(String path) {
        File file = new File(path);
        if(file.exists())
            file.delete();
    }

}
