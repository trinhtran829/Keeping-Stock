# KeepingStock Architecture Contracts (v1)

This document turns the navigation model into shared “contracts” (agreements) that
UI, data, and platform features must follow so we don’t drift during integration.

## Source Navigation Model (UI Lead)
Screens:
- Core Browsers: ContainerBrowser, ItemBrowser
- Detail: ItemDetail, ContainerDetail
- Add/Edit: AddEditContainer, AddEditItem
- Utility: QRScan

Flows:
- Nav Menu
  - ContainerBrowser
    - info -> ContainerDetail(containerId)
      - edit -> AddEditContainer(containerId)
    - sub-container tab -> ContainerBrowser(containerId)
    - item -> ItemDetail(itemId)
      - edit -> AddEditItem(itemId)
    - add item -> AddEditItem(containerId)
    - add sub-container -> AddEditContainer(parentContainerId)
  - Scan QR -> QRScan -> ContainerBrowser(containerId)
  - ItemBrowser
    - item -> ItemDetail(itemId)
      - edit -> AddEditItem(itemId)
    - add item -> AddEditItem(containerId)

## Contract: Route names + argument keys

Route names (do not hardcode strings outside Routes.kt):
- container_browser
- item_browser
- container_detail
- item_detail
- add_edit_container
- add_edit_item
- qr_scan

Argument keys (do not hardcode keys outside Routes.kt):
- containerId
- itemId
- parentContainerId

## Contract: ViewModel ownership (v1)
Each screen owns a ViewModel with consistent state + events:
- ContainerBrowser -> ContainerBrowserViewModel(containerId?)
- ItemBrowser -> ItemBrowserViewModel()
- ContainerDetail -> ContainerDetailViewModel(containerId)
- ItemDetail -> ItemDetailViewModel(itemId)
- AddEditContainer -> AddEditContainerViewModel(containerId?, parentContainerId?)
- AddEditItem -> AddEditItemViewModel(itemId?, containerId?)
- QRScan -> QrScanViewModel()

## Contract: Shared UI state pattern
All screens use UiState<T> for loading/success/error:
- Loading
- Success(data)
- Error(message, cause?)

## Contract: Repository + Platform Service Interfaces
UI/ViewModels depend on interfaces, not concrete implementations.

Repositories:
- ItemRepository
- ContainerRepository

Platform Services:
- CameraService
- QrService
- ImageLabelService
