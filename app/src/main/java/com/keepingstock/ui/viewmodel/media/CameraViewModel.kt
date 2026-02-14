package com.keepingstock.ui.viewmodel.media

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CameraUiState(
    val lastPhotoUri: Uri? = null,
    val labels: List<String> = emptyList()
)

class CameraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun onPhotoCaptured(uri: Uri, labels: List<String> = emptyList()) {
        _uiState.value = CameraUiState(lastPhotoUri = uri, labels = labels)
    }

    fun clearPhoto() {
        _uiState.value = CameraUiState()
    }
}
