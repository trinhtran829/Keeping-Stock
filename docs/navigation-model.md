# Navigation Model

Initial navigation model - update as more screens are integrated into the app

## Screens:

### Core Browsers:
- ContainerBrowser
- ItemBrowser

### Detail Screens:
- ItemDetail
- ContainerDetail
    
### Add/Edit Forms:
- AddEditContainer
- AddEditItem

### Utility:
- QRScan

## Simplified Navigation Flow
```
Nav Menu
|- ContainerBrowser
|    |- tap info -> ContainerDetail(containerId)
|    |         |- edit -> AddEditContainer(containerId)
|    |- tab sub-container -> ContainerBrowser(containerId)
|    |- tap item -> ItemDetail(itemId)
|    |         |- edit -> AddEditItem(itemId)
|    |- tap add item -> AddEditItem(containerId)
|    |- add sub-container -> AddEditContainer(parentContainerId)
|- Scan QR -> QRScan -> ContainerBrowser(containerId)
|- ItemBrowser
    |- tap item -> ItemDetail(itemId)
    |         |- edit -> AddEditItem(itemId)
    |- tap add item -> AddEditItem()
```