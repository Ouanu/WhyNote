package com.moment.whynote.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.moment.whynote.fragment.ConnectFragment;

public class ConnectService extends Service implements ConnectFragment.ConnectListener {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onConnectSelected(Bundle bundle) {

    }
}
