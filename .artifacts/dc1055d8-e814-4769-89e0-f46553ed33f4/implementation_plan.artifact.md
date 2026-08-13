# Soporte para Código de Barras en Inventario y Ventas

Este plan adapta la aplicación para que los productos puedan identificarse mediante un código de barras (barcode) además de su ID interno autogenerado. Esto permite que el escáner funcione correctamente con códigos largos sin interferir con la lógica de IDs de la base de datos.

## User Review Required

> [!IMPORTANT]
> Para que esto funcione, el backend debe soportar el campo `barcode` en el objeto `ProductDto`. Si estás usando una base de datos directamente, debes añadir una columna llamada `barcode` (de tipo texto) a tu tabla de productos.

## Proposed Changes

### Feature: Inventory (Gestión de Productos)

#### [MODIFY] [EditProductDialog.kt](file:///C:/Users/anderson/Documents/GitHub/fliigo/app/src/main/java/com/arstudios/fliigo/inventory/ui/components/EditProductDialog.kt)
Añadir un campo de texto para visualizar y editar el código de barras del producto.

#### [MODIFY] [InventoryViewModel.kt](file:///C:/Users/anderson/Documents/GitHub/fliigo/app/src/main/java/com/arstudios/fliigo/inventory/viewmodel/InventoryViewModel.kt)
Actualizar la función `updateProduct` para incluir el parámetro `barcode` y enviarlo al servidor.

#### [MODIFY] [InventoryScreen.kt](file:///C:/Users/anderson/Documents/GitHub/fliigo/app/src/main/java/com/arstudios/fliigo/inventory/ui/screens/InventoryScreen.kt)
Pasar el código de barras actual al diálogo de edición y enviarlo de vuelta al ViewModel al guardar cambios.

### Feature: Balance (Ventas)

#### [MODIFY] [BalanceViewModel.kt](file:///C:/Users/anderson/Documents/GitHub/fliigo/app/src/main/java/com/arstudios/fliigo/balance/viewmodel/BalanceViewModel.kt)
Modificar la lógica de validación de ventas para que busque productos por `barcode` primero, y si no hay coincidencia, intente buscar por `id`.

#### [MODIFY] [RegisterSaleDialog.kt](file:///C:/Users/anderson/Documents/GitHub/fliigo/app/src/main/java/com/arstudios/fliigo/balance/ui/components/RegisterSaleDialog.kt)
Cambiar las etiquetas de "ID Producto" a "ID o Código" para que el usuario entienda que puede ingresar cualquiera de los dos.

## Verification Plan

### Manual Verification
1.  **Inventario**: Editar un producto existente y asignarle un código de barras largo (ej: `770123456789`). Guardar y verificar que se mantenga.
2.  **Ventas**: Abrir el diálogo de registro de venta.
3.  **Escaneo**: Escanear el código de barras del producto anterior. Verificar que la aplicación encuentre el producto, muestre su nombre y precio correctamente, a pesar de que su ID interno sea un número pequeño (ej: `1`).
4.  **Ingreso Manual**: Ingresar manualmente el ID (ej: `1`) y verificar que también funcione como respaldo.
