package com.moment.whynote.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.moment.whynote.data.ResData;
import com.moment.whynote.database.ResRepository;

import java.util.List;

public class ResViewModel extends ViewModel {
    private ResRepository repository = ResRepository.getInstance();
    public LiveData<List<ResData>> dataList = repository.getAllResData();
}
