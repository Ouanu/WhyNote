package com.moment.whynote.view;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.annotation.Nullable;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OTextEditor extends LinearLayout {
    private EditText editText;
    private ScrollView scrollView;
    private StringBuffer stringBuffer = new StringBuffer();
    private static ContentResolver resolver;

    public OTextEditor(Context context) {
        super(context);
        initView();
    }

    public OTextEditor(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public OTextEditor(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public OTextEditor(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    @SuppressLint("ResourceAsColor")
    public void initView() {
        resolver = getContext().getContentResolver();
        scrollView = new ScrollView(getContext());
        editText = new EditText(getContext());
        scrollView.addView(editText, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        this.setOrientation(VERTICAL);
        this.addView(scrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }


    public EditText getEditText() {
        return editText;
    }

    public void setEditText(EditText editText) {
        this.editText = editText;
    }

    public void insertImage(StringBuffer desc, int start, int end, ContentResolver resolver) {
        SpannableStringBuilder builder = new SpannableStringBuilder(desc);
        String rexgString  = "<([^>]*)>";                                   //正则表达式
        Pattern pattern = Pattern.compile(rexgString);//装载正则表达式
        Matcher matcher = pattern.matcher(desc);
        Bitmap bitmap = null;
        String uri = null;
        while (matcher.find()) {
            uri = matcher.group().replaceAll("<|>","");
            try {
                bitmap = MediaStore.Images.Media.getBitmap(resolver, Uri.parse(uri));
                builder.setSpan(
                        new ImageSpan(getContext(), bitmap), matcher.start(), matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        editText.setText(builder);
        editText.setSelection(desc.length());
    }

}
