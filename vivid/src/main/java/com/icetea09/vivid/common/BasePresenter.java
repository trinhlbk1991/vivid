package com.icetea09.vivid.common;

public class BasePresenter<T> {

    protected T view;

    public void attachView(T view) {
        this.view = view;
    }

    public void detachView() {
        view = null;
    }

    protected boolean isViewAttached() {
        return view != null;
    }
}
