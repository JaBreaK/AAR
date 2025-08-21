package com.adetrifauzananisarahel.ui.transaksi;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TransaksiViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public TransaksiViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is transaksi fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}