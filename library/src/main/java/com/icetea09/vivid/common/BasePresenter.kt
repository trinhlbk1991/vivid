package com.icetea09.vivid.common

open class BasePresenter<T> {

    protected var view: T? = null

    open fun attachView(view: T) {
        this.view = view
    }

    fun detachView() {
        view = null
    }
}
