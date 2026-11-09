-- =====================================================================
--  DATOS DE PRUEBA  -  SaboresAutenticos
-- =====================================================================
USE SaboresAutenticos;

SET @cajero   := (SELECT IdEmpleado FROM Empleado WHERE username = 'cajero');
SET @admin    := (SELECT IdEmpleado FROM Empleado WHERE username = 'admin');
SET @contador := (SELECT IdEmpleado FROM Empleado WHERE username = 'contador');
SET NAMES utf8mb4;
SET time_zone = '-05:00';
-- MySQL Workbench bloquea UPDATE sin clave en WHERE (modo seguro); este script los necesita.
SET SQL_SAFE_UPDATES = 0;
SET @ini_mes  := DATE_SUB(CURDATE(), INTERVAL DAYOFMONTH(CURDATE()) - 1 DAY);
SET @ini_prev := DATE_SUB(@ini_mes, INTERVAL 1 MONTH);
SET @anio := YEAR(@ini_mes);   SET @mes := MONTH(@ini_mes);
SET @anio_p := YEAR(@ini_prev); SET @mes_p := MONTH(@ini_prev);

SELECT @cajero AS idCajero, @admin AS idAdmin, @contador AS idContador;

-- Guardia: los empleados admin/cajero/contador los crea el BACKEND al arrancar por primera vez
-- (DataInitializer). Si aun no existen, se detiene aqui antes de insertar nada.
DROP PROCEDURE IF EXISTS sp_verificar_empleados_demo;
DELIMITER $$
CREATE PROCEDURE sp_verificar_empleados_demo()
BEGIN
  IF @cajero IS NULL OR @admin IS NULL OR @contador IS NULL THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Faltan los empleados admin/cajero/contador. Arranque el backend una vez y vuelva a ejecutar este script.';
  END IF;
END$$
DELIMITER ;
CALL sp_verificar_empleados_demo();
DROP PROCEDURE sp_verificar_empleados_demo;

-- ---------------- PARAMETROS DE SISTEMA ----------------
-- IGNORE: el esquema ya inserta estos parametros; si existen se respetan.
INSERT IGNORE INTO ParametroSistema (Clave, Valor, Descripcion) VALUES 
('LIMITE_CREDITO_DEFAULT', '500.00', 'Límite de crédito por defecto al abrir una cuenta mensual'),
('HORA_CRON_ALERTA_STOCK', '00:00', 'Hora de ejecución de las alertas de inventario'),
('DIAS_ALERTA_CADUCIDAD', '7', 'Días de anticipación para lanzar advertencia de vencimiento');

-- ---------------- CATEGORÍAS ----------------
INSERT INTO Categoria (Nombre, IdCategoriaPadre) VALUES
  ('Bebidas', NULL),
  ('Comidas', NULL),
  ('Postres', NULL);

SET @id_bebidas := (SELECT IdCategoria FROM Categoria WHERE Nombre = 'Bebidas');
SET @id_comidas := (SELECT IdCategoria FROM Categoria WHERE Nombre = 'Comidas');
SET @id_postres := (SELECT IdCategoria FROM Categoria WHERE Nombre = 'Postres');

INSERT INTO Categoria (Nombre, IdCategoriaPadre) VALUES
  ('Bebidas calientes', @id_bebidas),
  ('Bebidas frías', @id_bebidas),
  ('Desayunos', @id_comidas),
  ('Almuerzos', @id_comidas),
  ('Snacks y piqueos', @id_comidas);

-- ---------------- PRODUCTOS ----------------
INSERT INTO Producto (IdCategoria, Nombre, Precio, Stock, StockMinimo, FechaCaducidad, Activo) VALUES
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Bebidas calientes'), 'Café americano', 4.00, 60, 10, NULL, 1),
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Bebidas calientes'), 'Café con leche', 6.00, 45, 10, NULL, 1),
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Bebidas calientes'), 'Infusión de manzanilla', 3.50, 40, 8, NULL, 1),
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Bebidas frías'), 'Chicha morada (vaso)', 4.50, 35, 10, NULL, 1),
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Bebidas frías'), 'Jugo de naranja', 6.00, 25, 8, NULL, 1),
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Bebidas frías'), 'Gaseosa personal', 3.50, 48, 12, DATE_ADD(CURDATE(), INTERVAL 120 DAY), 1),
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Bebidas frías'), 'Agua mineral 625ml', 2.50, 70, 15, DATE_ADD(CURDATE(), INTERVAL 300 DAY), 1),
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Desayunos'), 'Pan con pollo', 6.50, 30, 8, NULL, 1),
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Desayunos'), 'Pan con huevo', 4.00, 30, 8, NULL, 1),
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Desayunos'), 'Sándwich mixto', 7.00, 20, 6, NULL, 1),
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Desayunos'), 'Desayuno completo', 9.00, 25, 6, NULL, 1),
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Almuerzos'), 'Menú del día', 12.00, 40, 10, NULL, 1),
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Almuerzos'), 'Ají de gallina', 14.00, 18, 6, NULL, 1),
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Almuerzos'), 'Lomo saltado', 16.00, 15, 5, NULL, 1),
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Almuerzos'), 'Arroz con pollo', 13.00, 20, 6, NULL, 1),
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Snacks y piqueos'), 'Empanada de carne', 4.50, 22, 8, NULL, 1),
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Snacks y piqueos'), 'Papa rellena', 5.00, 16, 6, NULL, 1),
  ((SELECT IdCategoria FROM Categoria WHERE Nombre = 'Snacks y piqueos'), 'Galletas surtidas', 1.50, 3, 10, DATE_ADD(CURDATE(), INTERVAL 60 DAY), 1),
  (@id_postres, 'Yogurt individual', 5.00, 12, 5, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 1),
  (@id_postres, 'Keke de zanahoria', 3.50, 6, 4, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1),
  (@id_postres, 'Gelatina', 2.50, 0, 5, NULL, 1),
  (@id_postres, 'Mazamorra morada', 4.00, 14, 5, NULL, 1);

-- ---------------- CLIENTES ----------------
INSERT INTO Cliente (Nombres, Apellidos, DNI, Correo, Telefono, Area, Activo) VALUES
  ('Carlos', 'Quispe Ramos', '45123678', 'carlos.quispe@sabores-demo.pe', '987100001', 'Logística', 1),
  ('María Fernanda', 'Torres Díaz', '46234789', 'maria.torres@sabores-demo.pe', '987100002', 'Informática', 1),
  ('José Luis', 'Mamani Condori', '40345890', 'jose.mamani@sabores-demo.pe', '987100003', 'Recursos Humanos', 1),
  ('Rosa Elena', 'Vargas Huamán', '41456901', 'rosa.vargas@sabores-demo.pe', '987100004', 'Contabilidad', 1),
  ('Luis Alberto', 'Chávez Soto', '42567012', 'luis.chavez@sabores-demo.pe', '987100005', 'Operaciones', 1),
  ('Ana Lucía', 'Paredes Ríos', '43678123', 'ana.paredes@sabores-demo.pe', '987100006', 'Asesoría Legal', 1),
  ('Jorge Enrique', 'Salas Medina', '44789234', 'jorge.salas@sabores-demo.pe', '987100007', 'Planeamiento', 1),
  ('Carmen Rosa', 'Flores Gutiérrez', '45890345', 'carmen.flores@sabores-demo.pe', '987100008', 'Logística', 1),
  ('Miguel Ángel', 'Rojas Cruz', '46901456', 'miguel.rojas@sabores-demo.pe', '987100009', 'Informática', 1),
  ('Patricia', 'Núñez Castillo', '47012567', 'patricia.nunez@sabores-demo.pe', '987100010', 'Gestión del Riesgo', 1),
  ('Fernando', 'Ccama Apaza', '48123678', 'fernando.ccama@sabores-demo.pe', '987100011', 'Almacén', 1),
  ('Sofía', 'Delgado Lara', '49234789', 'sofia.delgado@sabores-demo.pe', '987100012', 'Comunicaciones', 1),
  ('Ricardo', 'Palma Vega', '40456780', 'ricardo.palma@sabores-demo.pe', '987100013', 'Contabilidad', 0);

-- ---------------- CUENTAS DE CRÉDITO ----------------
INSERT INTO CuentaCredito (IdCliente, Year, Month, LimiteCredito, MontoConsumido, MontoAbonado, FechaSaldoCero) VALUES
  ((SELECT IdCliente FROM Cliente WHERE DNI = '40345890'), @anio_p, @mes_p, 500.00, 93.00, 0, NULL),
  ((SELECT IdCliente FROM Cliente WHERE DNI = '41456901'), @anio_p, @mes_p, 500.00, 407.00, 407.00, @ini_prev + INTERVAL 27 DAY + INTERVAL 9 HOUR),
  ((SELECT IdCliente FROM Cliente WHERE DNI = '45123678'), @anio_p, @mes_p, 500.00, 179.50, 179.50, @ini_prev + INTERVAL 26 DAY + INTERVAL 11 HOUR),
  ((SELECT IdCliente FROM Cliente WHERE DNI = '46234789'), @anio_p, @mes_p, 500.00, 303.00, 200.00, NULL),
  ((SELECT IdCliente FROM Cliente WHERE DNI = '40345890'), @anio, @mes, 500.00, 480.00, 0, NULL),
  ((SELECT IdCliente FROM Cliente WHERE DNI = '42567012'), @anio, @mes, 500.00, 36.00, 36.00, LEAST(NOW(), @ini_mes + INTERVAL 2 DAY + INTERVAL 17 HOUR)),
  ((SELECT IdCliente FROM Cliente WHERE DNI = '43678123'), @anio, @mes, 500.00, 500.00, 0, NULL),
  ((SELECT IdCliente FROM Cliente WHERE DNI = '45123678'), @anio, @mes, 500.00, 63.00, 0, NULL),
  ((SELECT IdCliente FROM Cliente WHERE DNI = '46234789'), @anio, @mes, 500.00, 144.50, 50.00, NULL),
  ((SELECT IdCliente FROM Cliente WHERE DNI = '41456901'), @anio, @mes, 500.00, 0, 0, NULL),
  ((SELECT IdCliente FROM Cliente WHERE DNI = '44789234'), @anio, @mes, 500.00, 0, 0, NULL);

-- ---------------- VENTAS ----------------
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, (SELECT IdCliente FROM Cliente WHERE DNI = '45123678'), (SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '45123678') AND Year = @anio_p AND Month = @mes_p), 'CREDITO', 107.00, 'REGISTRADA', @ini_prev + INTERVAL 4 DAY + INTERVAL 13 HOUR);
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 5, Precio FROM Producto WHERE Nombre = 'Menú del día';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 10, Precio FROM Producto WHERE Nombre = 'Café americano';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 2, Precio FROM Producto WHERE Nombre = 'Gaseosa personal';
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 1, 107.00, FechaHora FROM Venta WHERE IdVenta = @v;

INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, (SELECT IdCliente FROM Cliente WHERE DNI = '46234789'), (SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '46234789') AND Year = @anio_p AND Month = @mes_p), 'CREDITO', 190.00, 'REGISTRADA', @ini_prev + INTERVAL 6 DAY + INTERVAL 14 HOUR);
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 8, Precio FROM Producto WHERE Nombre = 'Lomo saltado';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 8, Precio FROM Producto WHERE Nombre = 'Café con leche';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 4, Precio FROM Producto WHERE Nombre = 'Keke de zanahoria';
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 2, 190.00, FechaHora FROM Venta WHERE IdVenta = @v;

INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, (SELECT IdCliente FROM Cliente WHERE DNI = '40345890'), (SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '40345890') AND Year = @anio_p AND Month = @mes_p), 'CREDITO', 93.00, 'REGISTRADA', @ini_prev + INTERVAL 9 DAY + INTERVAL 12 HOUR);
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 5, Precio FROM Producto WHERE Nombre = 'Arroz con pollo';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 4, Precio FROM Producto WHERE Nombre = 'Agua mineral 625ml';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 4, Precio FROM Producto WHERE Nombre = 'Empanada de carne';
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 3, 93.00, FechaHora FROM Venta WHERE IdVenta = @v;

INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, (SELECT IdCliente FROM Cliente WHERE DNI = '41456901'), (SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '41456901') AND Year = @anio_p AND Month = @mes_p), 'CREDITO', 407.00, 'REGISTRADA', @ini_prev + INTERVAL 12 DAY + INTERVAL 9 HOUR);
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 20, Precio FROM Producto WHERE Nombre = 'Desayuno completo';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 10, Precio FROM Producto WHERE Nombre = 'Café americano';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 15, Precio FROM Producto WHERE Nombre = 'Menú del día';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 2, Precio FROM Producto WHERE Nombre = 'Gaseosa personal';
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 4, 407.00, FechaHora FROM Venta WHERE IdVenta = @v;

INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, (SELECT IdCliente FROM Cliente WHERE DNI = '45123678'), (SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '45123678') AND Year = @anio_p AND Month = @mes_p), 'CREDITO', 72.50, 'REGISTRADA', @ini_prev + INTERVAL 15 DAY + INTERVAL 13 HOUR);
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 4, Precio FROM Producto WHERE Nombre = 'Menú del día';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 3, Precio FROM Producto WHERE Nombre = 'Jugo de naranja';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 1, Precio FROM Producto WHERE Nombre = 'Pan con pollo';
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 5, 72.50, FechaHora FROM Venta WHERE IdVenta = @v;

INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, (SELECT IdCliente FROM Cliente WHERE DNI = '46234789'), (SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '46234789') AND Year = @anio_p AND Month = @mes_p), 'CREDITO', 113.00, 'REGISTRADA', @ini_prev + INTERVAL 20 DAY + INTERVAL 13 HOUR);
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 5, Precio FROM Producto WHERE Nombre = 'Ají de gallina';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 6, Precio FROM Producto WHERE Nombre = 'Chicha morada (vaso)';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 4, Precio FROM Producto WHERE Nombre = 'Mazamorra morada';
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 6, 113.00, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 7: CREDITO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, (SELECT IdCliente FROM Cliente WHERE DNI = '45123678'), (SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '45123678') AND Year = @anio AND Month = @mes), 'CREDITO', 63.00, 'REGISTRADA', LEAST(NOW(), @ini_mes + INTERVAL 1 DAY + INTERVAL 13 HOUR));
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 4, Precio FROM Producto WHERE Nombre = 'Menú del día';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 3, Precio FROM Producto WHERE Nombre = 'Café americano';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 2, Precio FROM Producto WHERE Nombre = 'Galletas surtidas';
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 7, 63.00, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 8: CREDITO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, (SELECT IdCliente FROM Cliente WHERE DNI = '46234789'), (SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '46234789') AND Year = @anio AND Month = @mes), 'CREDITO', 122.00, 'REGISTRADA', LEAST(NOW(), @ini_mes + INTERVAL 1 DAY + INTERVAL 14 HOUR));
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 5, Precio FROM Producto WHERE Nombre = 'Lomo saltado';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 6, Precio FROM Producto WHERE Nombre = 'Chicha morada (vaso)';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 3, Precio FROM Producto WHERE Nombre = 'Papa rellena';
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 8, 122.00, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 9: CREDITO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, (SELECT IdCliente FROM Cliente WHERE DNI = '40345890'), (SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '40345890') AND Year = @anio AND Month = @mes), 'CREDITO', 440.00, 'REGISTRADA', LEAST(NOW(), @ini_mes + INTERVAL 1 DAY + INTERVAL 12 HOUR));
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 10, Precio FROM Producto WHERE Nombre = 'Lomo saltado';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 8, Precio FROM Producto WHERE Nombre = 'Ají de gallina';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 10, Precio FROM Producto WHERE Nombre = 'Menú del día';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 8, Precio FROM Producto WHERE Nombre = 'Café con leche';
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 9, 440.00, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 10: CREDITO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, (SELECT IdCliente FROM Cliente WHERE DNI = '43678123'), (SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '43678123') AND Year = @anio AND Month = @mes), 'CREDITO', 460.00, 'REGISTRADA', LEAST(NOW(), @ini_mes + INTERVAL 1 DAY + INTERVAL 11 HOUR));
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 10, Precio FROM Producto WHERE Nombre = 'Lomo saltado';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 10, Precio FROM Producto WHERE Nombre = 'Ají de gallina';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 10, Precio FROM Producto WHERE Nombre = 'Menú del día';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 10, Precio FROM Producto WHERE Nombre = 'Café americano';
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 10, 460.00, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 11: CREDITO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, (SELECT IdCliente FROM Cliente WHERE DNI = '46234789'), (SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '46234789') AND Year = @anio AND Month = @mes), 'CREDITO', 22.50, 'REGISTRADA', LEAST(NOW(), @ini_mes + INTERVAL 2 DAY + INTERVAL 9 HOUR));
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 3, Precio FROM Producto WHERE Nombre = 'Café con leche';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 1, Precio FROM Producto WHERE Nombre = 'Empanada de carne';
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 11, 22.50, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 12: CREDITO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, (SELECT IdCliente FROM Cliente WHERE DNI = '40345890'), (SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '40345890') AND Year = @anio AND Month = @mes), 'CREDITO', 40.00, 'REGISTRADA', LEAST(NOW(), @ini_mes + INTERVAL 2 DAY + INTERVAL 8 HOUR));
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 4, Precio FROM Producto WHERE Nombre = 'Desayuno completo';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 1, Precio FROM Producto WHERE Nombre = 'Café americano';
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 12, 40.00, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 13: CREDITO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, (SELECT IdCliente FROM Cliente WHERE DNI = '42567012'), (SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '42567012') AND Year = @anio AND Month = @mes), 'CREDITO', 36.00, 'REGISTRADA', LEAST(NOW(), @ini_mes + INTERVAL 2 DAY + INTERVAL 13 HOUR));
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 3, Precio FROM Producto WHERE Nombre = 'Menú del día';
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 13, 36.00, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 14: CREDITO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, (SELECT IdCliente FROM Cliente WHERE DNI = '43678123'), (SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '43678123') AND Year = @anio AND Month = @mes), 'CREDITO', 40.00, 'REGISTRADA', LEAST(NOW(), @ini_mes + INTERVAL 2 DAY + INTERVAL 10 HOUR));
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 4, Precio FROM Producto WHERE Nombre = 'Desayuno completo';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 1, Precio FROM Producto WHERE Nombre = 'Café americano';
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 14, 40.00, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 15: CONTADO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, NULL, NULL, 'CONTADO', 42.00, 'REGISTRADA', NOW() - INTERVAL 20 DAY - INTERVAL 5 HOUR);
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 4, Precio FROM Producto WHERE Nombre = 'Pan con pollo';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 4, Precio FROM Producto WHERE Nombre = 'Café americano';
INSERT INTO VentaPago (IdVenta, Monto, MetodoPago) VALUES (@v, 42.00, 'YAPE');
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 15, 42.00, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 16: CONTADO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, NULL, NULL, 'CONTADO', 72.00, 'REGISTRADA', NOW() - INTERVAL 12 DAY - INTERVAL 3 HOUR);
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 6, Precio FROM Producto WHERE Nombre = 'Menú del día';
INSERT INTO VentaPago (IdVenta, Monto, MetodoPago) VALUES (@v, 72.00, 'EFECTIVO');
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 16, 72.00, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 17: CONTADO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, NULL, NULL, 'CONTADO', 52.50, 'REGISTRADA', NOW() - INTERVAL 8 DAY - INTERVAL 2 HOUR);
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 3, Precio FROM Producto WHERE Nombre = 'Arroz con pollo';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 3, Precio FROM Producto WHERE Nombre = 'Chicha morada (vaso)';
INSERT INTO VentaPago (IdVenta, Monto, MetodoPago) VALUES (@v, 30.00, 'EFECTIVO');
INSERT INTO VentaPago (IdVenta, Monto, MetodoPago) VALUES (@v, 22.50, 'YAPE');
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 17, 52.50, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 18: CONTADO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, NULL, NULL, 'CONTADO', 39.00, 'REGISTRADA', NOW() - INTERVAL 5 DAY - INTERVAL 6 HOUR);
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 3, Precio FROM Producto WHERE Nombre = 'Sándwich mixto';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 3, Precio FROM Producto WHERE Nombre = 'Café con leche';
INSERT INTO VentaPago (IdVenta, Monto, MetodoPago) VALUES (@v, 39.00, 'EFECTIVO');
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 18, 39.00, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 19: CONTADO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, NULL, NULL, 'CONTADO', 33.00, 'REGISTRADA', NOW() - INTERVAL 3 DAY - INTERVAL 3 HOUR);
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 2, Precio FROM Producto WHERE Nombre = 'Ají de gallina';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 2, Precio FROM Producto WHERE Nombre = 'Agua mineral 625ml';
INSERT INTO VentaPago (IdVenta, Monto, MetodoPago) VALUES (@v, 33.00, 'YAPE');
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 19, 33.00, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 20: CONTADO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, NULL, NULL, 'CONTADO', 32.00, 'REGISTRADA', NOW() - INTERVAL 2 DAY - INTERVAL 4 HOUR);
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 4, Precio FROM Producto WHERE Nombre = 'Empanada de carne';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 4, Precio FROM Producto WHERE Nombre = 'Gaseosa personal';
INSERT INTO VentaPago (IdVenta, Monto, MetodoPago) VALUES (@v, 32.00, 'EFECTIVO');
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 20, 32.00, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 21: CONTADO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, NULL, NULL, 'CONTADO', 45.00, 'REGISTRADA', NOW() - INTERVAL 1 DAY - INTERVAL 5 HOUR);
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 3, Precio FROM Producto WHERE Nombre = 'Desayuno completo';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 3, Precio FROM Producto WHERE Nombre = 'Jugo de naranja';
INSERT INTO VentaPago (IdVenta, Monto, MetodoPago) VALUES (@v, 45.00, 'EFECTIVO');
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 21, 45.00, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 22: CONTADO (ANULADA)
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora, MotivoAnulacion, IdEmpleadoAnula, FechaAnulacion)
VALUES (@cajero, NULL, NULL, 'CONTADO', 39.00, 'ANULADA', NOW() - INTERVAL 1 DAY - INTERVAL 2 HOUR, 'Cliente se retiró antes de recibir el pedido y devolvió el pago', @admin, NOW());
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 2, Precio FROM Producto WHERE Nombre = 'Lomo saltado';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 2, Precio FROM Producto WHERE Nombre = 'Gaseosa personal';
INSERT INTO VentaPago (IdVenta, Monto, MetodoPago) VALUES (@v, 39.00, 'EFECTIVO');
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 22, 39.00, FechaHora FROM Venta WHERE IdVenta = @v;
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) VALUES (@v, 'NOTA_CREDITO', 'NC01', 1, 39.00, NOW());

-- Venta 23: CONTADO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, NULL, NULL, 'CONTADO', 16.00, 'REGISTRADA', NOW() - INTERVAL 0 DAY - INTERVAL 3 HOUR);
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 2, Precio FROM Producto WHERE Nombre = 'Café americano';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 2, Precio FROM Producto WHERE Nombre = 'Pan con huevo';
INSERT INTO VentaPago (IdVenta, Monto, MetodoPago) VALUES (@v, 16.00, 'EFECTIVO');
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 23, 16.00, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 24: CONTADO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, NULL, NULL, 'CONTADO', 31.00, 'REGISTRADA', NOW() - INTERVAL 0 DAY - INTERVAL 2 HOUR);
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 2, Precio FROM Producto WHERE Nombre = 'Menú del día';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 2, Precio FROM Producto WHERE Nombre = 'Gaseosa personal';
INSERT INTO VentaPago (IdVenta, Monto, MetodoPago) VALUES (@v, 31.00, 'YAPE');
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 24, 31.00, FechaHora FROM Venta WHERE IdVenta = @v;

-- Venta 25: CONTADO
INSERT INTO Venta (IdEmpleado, IdCliente, IdCuenta, TipoPago, Total, Estado, FechaHora)
VALUES (@cajero, NULL, NULL, 'CONTADO', 24.50, 'REGISTRADA', NOW() - INTERVAL 0 DAY - INTERVAL 1 HOUR);
SET @v := LAST_INSERT_ID();
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 1, Precio FROM Producto WHERE Nombre = 'Lomo saltado';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 1, Precio FROM Producto WHERE Nombre = 'Chicha morada (vaso)';
INSERT INTO VentaDetalle (IdVenta, IdProducto, Cantidad, PrecioUnitario) SELECT @v, IdProducto, 1, Precio FROM Producto WHERE Nombre = 'Mazamorra morada';
INSERT INTO VentaPago (IdVenta, Monto, MetodoPago) VALUES (@v, 10.00, 'YAPE');
INSERT INTO VentaPago (IdVenta, Monto, MetodoPago) VALUES (@v, 14.50, 'EFECTIVO');
INSERT INTO Comprobante (IdVenta, Tipo, Serie, Numero, Total, FechaEmision) SELECT @v, 'BOLETA', 'B001', 25, 24.50, FechaHora FROM Venta WHERE IdVenta = @v;


-- ---------------- ABONOS ----------------
INSERT INTO Abono (IdCuenta, IdEmpleado, Monto, MetodoPago, FechaHora) VALUES
  ((SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '45123678') AND Year = @anio_p AND Month = @mes_p), @cajero, 100.00, 'EFECTIVO', @ini_prev + INTERVAL 24 DAY + INTERVAL 10 HOUR),
  ((SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '45123678') AND Year = @anio_p AND Month = @mes_p), @cajero, 79.50, 'YAPE', @ini_prev + INTERVAL 26 DAY + INTERVAL 11 HOUR),
  ((SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '46234789') AND Year = @anio_p AND Month = @mes_p), @cajero, 200.00, 'YAPE', @ini_prev + INTERVAL 25 DAY + INTERVAL 15 HOUR),
  ((SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '41456901') AND Year = @anio_p AND Month = @mes_p), @cajero, 407.00, 'EFECTIVO', @ini_prev + INTERVAL 27 DAY + INTERVAL 9 HOUR),
  ((SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '46234789') AND Year = @anio AND Month = @mes), @cajero, 50.00, 'EFECTIVO', LEAST(NOW(), @ini_mes + INTERVAL 3 DAY + INTERVAL 10 HOUR)),
  ((SELECT IdCuenta FROM CuentaCredito WHERE IdCliente = (SELECT IdCliente FROM Cliente WHERE DNI = '42567012') AND Year = @anio AND Month = @mes), @cajero, 36.00, 'EFECTIVO', LEAST(NOW(), @ini_mes + INTERVAL 2 DAY + INTERVAL 17 HOUR));

-- ---------------- GUÍAS DE REMISIÓN ----------------
INSERT INTO GuiaRemision (NumeroGuia, Proveedor, IdEmpleado, FechaIngreso)
VALUES ('T001-000451', 'Distribuidora Andina S.A.C.', @contador, NOW() - INTERVAL 6 DAY);
SET @g := LAST_INSERT_ID();
INSERT INTO GuiaRemisionDetalle (IdGuia, IdProducto, Cantidad) SELECT @g, IdProducto, 48 FROM Producto WHERE Nombre = 'Gaseosa personal';
INSERT INTO GuiaRemisionDetalle (IdGuia, IdProducto, Cantidad) SELECT @g, IdProducto, 70 FROM Producto WHERE Nombre = 'Agua mineral 625ml';

INSERT INTO GuiaRemision (NumeroGuia, Proveedor, IdEmpleado, FechaIngreso)
VALUES ('T001-000467', 'Lácteos del Valle E.I.R.L.', @contador, NOW() - INTERVAL 2 DAY);
SET @g := LAST_INSERT_ID();
INSERT INTO GuiaRemisionDetalle (IdGuia, IdProducto, Cantidad) SELECT @g, IdProducto, 12 FROM Producto WHERE Nombre = 'Yogurt individual';
INSERT INTO GuiaRemisionDetalle (IdGuia, IdProducto, Cantidad) SELECT @g, IdProducto, 6  FROM Producto WHERE Nombre = 'Keke de zanahoria';

-- ---------------- HISTORIAL ALERTA (RF28) ----------------
INSERT INTO HistorialAlerta (IdCliente, TipoAlerta, Canal, Destino, Mensaje, Estado, Intentos, DetalleError, FechaEnvio) VALUES
((SELECT IdCliente FROM Cliente WHERE DNI = '40345890'), 'DEUDA', 'SMS', '987100003', 'Sabores Auténticos: Su deuda pendiente es de S/ 480.00. Por favor, regularice su pago.', 'ENVIADO', 1, NULL, NOW() - INTERVAL 2 DAY),
((SELECT IdCliente FROM Cliente WHERE DNI = '43678123'), 'LIMITE_CREDITO', 'CORREO', 'ana.paredes@sabores-demo.pe', 'Estimado cliente, su cuenta ha alcanzado el límite de crédito (S/ 500.00).', 'ENVIADO', 2, NULL, NOW() - INTERVAL 1 DAY),
((SELECT IdCliente FROM Cliente WHERE DNI = '46234789'), 'DEUDA', 'SMS', '987100002', 'Sabores Auténticos: Su deuda pendiente es de S/ 197.50. Por favor, regularice su pago.', 'FALLIDO', 4, 'Proveedor SMS no disponible tras 3 reintentos', NOW() - INTERVAL 1 DAY);

-- ---------------- COMPROBANTES: DATOS SUNAT SIMULADOS (RF12 / RF21) ----------------
-- Todos los comprobantes de prueba se consideran aceptados por SUNAT.
UPDATE Comprobante
SET EstadoSunat = 'ACEPTADO',
    CodigoCdr   = '0',
    MensajeCdr  = 'El comprobante ha sido aceptado (simulado)',
    TicketSunat = CONCAT('SUNAT-', LPAD(IdComprobante, 8, '0')),
    FechaCdr    = FechaEmision,
    XmlContenido = CONCAT('<?xml version="1.0" encoding="UTF-8"?><Invoice><ID>', Serie, '-', Numero,
                          '</ID><Tipo>', Tipo, '</Tipo><Total>', Total, '</Total></Invoice>'),
    HashFirma   = SHA2(CONCAT(Tipo, '|', Serie, '|', Numero, '|', Total), 256);

-- La Nota de Crédito referencia a la boleta que anula.
UPDATE Comprobante nc
JOIN Comprobante bo ON bo.IdVenta = nc.IdVenta AND bo.Tipo = 'BOLETA'
SET nc.IdComprobanteRef = bo.IdComprobante
WHERE nc.Tipo = 'NOTA_CREDITO';

-- Auditoría de la anulación de la venta de prueba (RF11).
INSERT INTO AuditoriaAnulacion (IdVenta, IdEmpleadoAnula, Motivo, FechaHora)
SELECT IdVenta, @admin, MotivoAnulacion, FechaAnulacion FROM Venta WHERE Estado = 'ANULADA';

-- Sincroniza los correlativos con lo ya emitido, para que el siguiente numero sea consecutivo.
UPDATE SerieComprobante s
SET UltimoNumero = COALESCE(
  (SELECT MAX(c.Numero) FROM Comprobante c WHERE c.Tipo = s.Tipo AND c.Serie = s.Serie), 0);

-- Verificacion rapida
SELECT Tipo, Serie, UltimoNumero FROM SerieComprobante;
SELECT COUNT(*) AS AbonosAuditados FROM AuditoriaOperacion WHERE Entidad = 'ABONO';
