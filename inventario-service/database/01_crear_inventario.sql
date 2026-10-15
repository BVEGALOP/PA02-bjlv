/*
  Instalación limpia de InventarioDB.
  Ejecutar con una cuenta administradora en SQL Server Management Studio (SSMS).
*/

IF DB_ID(N'InventarioDB') IS NULL
BEGIN
    CREATE DATABASE InventarioDB;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.server_principals
    WHERE name = N'inventario_app'
)
BEGIN
    CREATE LOGIN inventario_app
        WITH PASSWORD = 'Inventario2026!',
             CHECK_POLICY = ON;
END;
GO

USE InventarioDB;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.database_principals
    WHERE name = N'inventario_app'
)
BEGIN
    CREATE USER inventario_app FOR LOGIN inventario_app WITH DEFAULT_SCHEMA = dbo;
END;
GO

IF OBJECT_ID(N'dbo.producto_inventario', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.producto_inventario (
        id       BIGINT IDENTITY(1,1) NOT NULL,
        codigo   VARCHAR(30)          NOT NULL,
        nombre   VARCHAR(120)         NOT NULL,
        precio   DECIMAL(12,2)        NOT NULL,
        stock    INT                  NOT NULL,
        version  BIGINT               NOT NULL CONSTRAINT DF_producto_version DEFAULT (0),
        CONSTRAINT PK_producto_inventario PRIMARY KEY (id),
        CONSTRAINT UQ_producto_inventario_codigo UNIQUE (codigo),
        CONSTRAINT CK_producto_inventario_precio CHECK (precio >= 0),
        CONSTRAINT CK_producto_inventario_stock CHECK (stock >= 0)
    );
END;
GO

GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.producto_inventario TO inventario_app;
GO

MERGE dbo.producto_inventario AS target
USING (
    VALUES
        ('LAP-001', 'Laptop',  CAST(2500.00 AS DECIMAL(12,2)), 10),
        ('MON-001', 'Monitor', CAST(850.00  AS DECIMAL(12,2)), 20),
        ('TEC-001', 'Teclado', CAST(120.00  AS DECIMAL(12,2)), 30)
) AS source(codigo, nombre, precio, stock)
ON target.codigo = source.codigo
WHEN MATCHED THEN
    UPDATE SET
        target.nombre = source.nombre,
        target.precio = source.precio
WHEN NOT MATCHED THEN
    INSERT (codigo, nombre, precio, stock, version)
    VALUES (source.codigo, source.nombre, source.precio, source.stock, 0);
GO

SELECT id, codigo, nombre, precio, stock, version
FROM dbo.producto_inventario
ORDER BY id;
GO
