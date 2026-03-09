package com.keepingstock.ui.viewmodel.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.data.repositories.ContainerRepository
import com.keepingstock.core.contracts.QrService
import com.keepingstock.core.contracts.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QrScanResponse(
    val containerId: Long,
    val containerName: String
)

data class QrScanUiData(
    val response: QrScanResponse? = null
)

private fun initialQrScanUiState(): UiState<QrScanUiData> {
    return UiState.Success(QrScanUiData())
}

/**
 * ViewModel owns QR scan UI state and business flow (scan -> resolve container -> emit state).
 *
 * Android docs:
 * - ViewModel overview: https://developer.android.com/topic/libraries/architecture/viewmodel
 * - Coroutines with lifecycle-aware scope: https://developer.android.com/topic/libraries/architecture/coroutines
 * - StateFlow usage on Android: https://developer.android.com/kotlin/flow/stateflow-and-sharedflow
 */
class QrScanViewModel(
    val qrService: QrService,
    private val containerRepository: ContainerRepository
) : ViewModel() {

    // Expose immutable state to UI and keep mutation private in ViewModel.
    private val _uiState = MutableStateFlow<UiState<QrScanUiData>>(initialQrScanUiState())
    val uiState: StateFlow<UiState<QrScanUiData>> = _uiState.asStateFlow()

    fun onContainerDetected(containerId: ContainerId) {
        viewModelScope.launch {
            if (_uiState.value is UiState.Loading) return@launch

            _uiState.value = UiState.Loading
            try {
                // Use getContainerById as defined in the project's ContainerRepository interface
                val container = containerRepository.getContainerById(containerId)
                if (container != null) {
                    _uiState.value = UiState.Success(
                        QrScanUiData(
                            response = QrScanResponse(
                                containerId = container.id.value,
                                containerName = container.name
                            )
                        )
                    )
                } else {
                    _uiState.value = UiState.Error("Container not found")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error resolving container: ${e.message}")
            }
        }
    }

    fun reset() {
        _uiState.value = initialQrScanUiState()
    }
}
