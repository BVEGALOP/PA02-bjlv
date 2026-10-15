/*
  Restablece el stock de demostración sin recrear la base de datos.
  No modifica los precios.
*/
USE InventarioDB;
GO

UPDATE dbo.producto_inventario SET stock = 10 WHERE codigo = 'LAP-001';
UPDATE dbo.producto_inventario SET stock = 20 WHERE codigo = 'MON-001';
UPDATE dbo.producto_inventario SET stock = 30 WHERE codigo = 'TEC-001';
GO

SELECT id, codigo, nombre, precio, stock, version
FROM dbo.producto_inventario
ORDER BY id;
GO
