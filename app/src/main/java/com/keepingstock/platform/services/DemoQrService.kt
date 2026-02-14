package com.keepingstock.platform.services

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.QrService
import kotlinx.coroutines.delay

/**
 * Minimal QR service implementation for feature integration before camera/QR hardware wiring.
 */
class DemoQrService(
    private val scannedIds: List<ContainerId> = listOf(
        ContainerId(1L),
        ContainerId(2L),
        ContainerId(3L)
    )
) : QrService {

    private var index: Int = 0

    override suspend fun scanContainerQr(): ContainerId {
        delay(300)
        if (scannedIds.isEmpty()) return ContainerId(0L)

        val scanned = scannedIds[index % scannedIds.size]
        index += 1
        return scanned
    }

    override suspend fun generateContainerQr(containerId: ContainerId): String {
        return "keepingstock://container/${containerId.value}"
    }
}
