package com.keepingstock.ui.viewmodel.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepingstock.core.contracts.ContainerRepository
import com.keepingstock.core.contracts.QrService
import com.keepingstock.core.contracts.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QrScanRequest(
    val requestId: String,
    val source: String = "qr_scan_screen"
)

data class QrScanResponse(
    val requestId: String,
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
    private val qrService: QrService,
    private val containerRepository: ContainerRepository
) : ViewModel() {

    // Expose immutable state to UI and keep mutation private in ViewModel.
    private val _uiState = MutableStateFlow<UiState<QrScanUiData>>(initialQrScanUiState())
    val uiState: StateFlow<UiState<QrScanUiData>> = _uiState.asStateFlow()

    fun scanContainer() {
        viewModelScope.launch {
            if (_uiState.value is UiState.Loading) return@launch

            _uiState.value = UiState.Loading

            try {
                val request = QrScanRequest(
                    requestId = "scan-${System.currentTimeMillis()}"
                )
                val scannedContainerId = qrService.scanContainerQr()
                val container = containerRepository.getContainer(scannedContainerId)

                if (container == null) {
                    _uiState.value = UiState.Error(
                        message = "Scanned container ${scannedContainerId.value} was not found."
                    )
                    return@launch
                }

                _uiState.value = UiState.Success(
                    QrScanUiData(
                        response = QrScanResponse(
                            requestId = request.requestId,
                            containerId = container.id.value,
                            containerName = container.name
                        )
                    )
                )
            } catch (t: Throwable) {
                _uiState.value = UiState.Error(
                    message = "Failed to scan QR container.",
                    cause = t
                )
            }
        }
    }

    fun reset() {
        _uiState.value = initialQrScanUiState()
    }
}
