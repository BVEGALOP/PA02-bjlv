/*
  MIGRACIÓN para una InventarioDB ya creada con la versión anterior.
  Agrega la columna precio sin eliminar el stock actual.
*/
USE InventarioDB;
GO

IF COL_LENGTH('dbo.producto_inventario', 'precio') IS NULL
BEGIN
    ALTER TABLE dbo.producto_inventario
        ADD precio DECIMAL(12,2) NULL;
END;
GO

UPDATE dbo.producto_inventario
SET precio = CASE codigo
    WHEN 'LAP-001' THEN 2500.00
    WHEN 'MON-001' THEN 850.00
    WHEN 'TEC-001' THEN 120.00
    ELSE COALESCE(precio, 0.00)
END;
GO

IF EXISTS (
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID(N'dbo.producto_inventario')
      AND name = N'precio'
      AND is_nullable = 1
)
BEGIN
    ALTER TABLE dbo.producto_inventario
        ALTER COLUMN precio DECIMAL(12,2) NOT NULL;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = N'CK_producto_inventario_precio'
)
BEGIN
    ALTER TABLE dbo.producto_inventario
        ADD CONSTRAINT CK_producto_inventario_precio CHECK (precio >= 0);
END;
GO

SELECT id, codigo, nombre, precio, stock, version
FROM dbo.producto_inventario
ORDER BY id;
GO
