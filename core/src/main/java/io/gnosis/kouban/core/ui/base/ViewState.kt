package io.gnosis.kouban.core.ui.base

abstract class ViewState
data class Loading(val isLoading: Boolean) : ViewState()
data class Error(val throwable: Throwable) : ViewState()
