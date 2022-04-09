package com.moment.whynote.utils;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;

/*
让控件悬浮在键盘上方
 */
public class FloatViewUtil {
    private static int height;
    private final Activity context;

    public FloatViewUtil(Activity context) {
        this.context = context;
        if (height == 0) {
//            Display display = context.getWindowManager().getDefaultDisplay();
            Display display = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                display = context.getDisplay();
            }
            Point point = new Point();
            display.getSize(point);
            height = point.y;
        }
    }

    /**
     * 设置控件悬浮方法
     * @param root 根布局
     * @param floatView 控件
     */
    public void setFloatView(View root, View floatView, int diff) {
        ViewTreeObserver.OnGlobalLayoutListener layoutListener = () -> {
            Rect rect = new Rect();
            context.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            int nowHeight = height - (rect.bottom - rect.top);
            Log.d("KEYBOARD_________", "setFloatView: " + nowHeight);
            boolean isShowing = nowHeight > height / 3;
            Log.d("KEYBOARD_________", "setFloatView: " + height);
            if (isShowing) {
                floatView.animate().translationY(-nowHeight + diff).setDuration(0).start();
            } else {
                floatView.animate().translationY(0).start();
            }
        };
        root.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
    }
}
