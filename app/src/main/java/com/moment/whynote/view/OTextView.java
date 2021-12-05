package com.moment.whynote.view;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A View which can insert images
 */
public class OTextView extends androidx.appcompat.widget.AppCompatTextView {

    public OTextView(@NonNull @NotNull Context context) {
        super(context);
    }

    public OTextView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OTextView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
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
                float k = ((float) 180) / width;
                // 取得想要缩放的matrix参数.
                Matrix matrix = new Matrix();
                matrix.postScale(k, k);
                // 得到新的图片.
                Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                builder.setSpan(
                        new ImageSpan(getContext(), newBitmap), matcher.start(), matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                this.setText(builder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
