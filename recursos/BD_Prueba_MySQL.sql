
CREATE DATABASE IF NOT EXISTS SaboresAutenticos
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
  
 -- Script para renombrar las columnas 
##ALTER TABLE Comprobante 
##HashFirma FirmaHash VARCHAR(128) NULL,
##CHANGE XmlContenido XmlComprobante LONGTEXT NULL;

USE SaboresAutenticos;

-- Zona horaria de Lima (UTC-5, sin horario de verano). Afecta NOW()/CURDATE()
-- en esta sesion. Para que aplique siempre, agregar en my.ini / my.cnf:
--   default-time-zone = '-05:00'
SET time_zone = '-05:00';

CREATE TABLE users (
  username VARCHAR(50) NOT NULL PRIMARY KEY,
  password VARCHAR(500) NOT NULL,
  enabled BOOLEAN NOT NULL,
  IntentosFallidos TINYINT NOT NULL DEFAULT 0,
  BloqueadoHasta DATETIME NULL
);

CREATE TABLE authorities (
  username VARCHAR(50) NOT NULL,
  authority VARCHAR(50) NOT NULL,
  CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users(username)
);

CREATE UNIQUE INDEX ix_auth_username ON authorities (username, authority);

CREATE TABLE Empleado (
  IdEmpleado INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NULL UNIQUE,
  Nombres VARCHAR(80) NOT NULL,
  Apellidos VARCHAR(80) NOT NULL,
  DNI CHAR(8) NOT NULL UNIQUE,
  Correo VARCHAR(100) NULL,
  Telefono VARCHAR(15) NULL,
  CONSTRAINT fk_empleado_users FOREIGN KEY (username) REFERENCES users(username)
);

CREATE TABLE Cliente (
  IdCliente INT AUTO_INCREMENT PRIMARY KEY,
  Nombres VARCHAR(80) NOT NULL,
  Apellidos VARCHAR(80) NOT NULL,
  DNI CHAR(8) NOT NULL UNIQUE,
  Correo VARCHAR(100) NULL UNIQUE, -- MODIFICACIÓN: Correo único
  Telefono VARCHAR(15) NULL,
  Area VARCHAR(80) NULL,
  Activo BOOLEAN NOT NULL DEFAULT 1
);

-- MODIFICACIÓN: Índices para búsqueda rápida (RF06)
CREATE INDEX ix_cliente_nombres ON Cliente(Nombres);
CREATE INDEX ix_cliente_apellidos ON Cliente(Apellidos);
CREATE INDEX ix_cliente_telefono ON Cliente(Telefono);


CREATE TABLE Categoria (
  IdCategoria INT AUTO_INCREMENT PRIMARY KEY,
  Nombre VARCHAR(60) NOT NULL,
  IdCategoriaPadre INT NULL,
  CONSTRAINT fk_subcategoria FOREIGN KEY (IdCategoriaPadre) REFERENCES Categoria(IdCategoria),
  UNIQUE(IdCategoriaPadre, Nombre) -- MODIFICACIÓN: Nombre único por padre
);

CREATE TABLE Producto (
  IdProducto INT AUTO_INCREMENT PRIMARY KEY,
  IdCategoria INT NOT NULL,
  Nombre VARCHAR(100) NOT NULL,
  Precio DECIMAL(10,2) NOT NULL,
  Stock INT NOT NULL DEFAULT 0,
  StockMinimo INT NOT NULL DEFAULT 5,
  FechaCaducidad DATE NULL,
  Activo BOOLEAN NOT NULL DEFAULT 1,
  CONSTRAINT fk_producto_categoria FOREIGN KEY (IdCategoria) REFERENCES Categoria(IdCategoria)
);

-- RF08: busqueda de productos por nombre. Un LIKE 'texto%' usa el indice;
-- un LIKE '%texto%' (contiene) no puede usar ningun indice B-tree.
CREATE INDEX ix_producto_nombre ON Producto(Nombre);
-- RF16: alertas de caducidad y stock.
CREATE INDEX ix_producto_caducidad ON Producto(Activo, FechaCaducidad);

CREATE TABLE CuentaCredito (
  IdCuenta INT AUTO_INCREMENT PRIMARY KEY,
  IdCliente INT NOT NULL,
  Year SMALLINT NOT NULL,
  Month TINYINT NOT NULL,
  LimiteCredito DECIMAL(10,2) NOT NULL DEFAULT 500,
  MontoConsumido DECIMAL(10,2) NOT NULL DEFAULT 0,
  MontoAbonado DECIMAL(10,2) NOT NULL DEFAULT 0,
  FechaSaldoCero DATETIME NULL,
  UNIQUE (IdCliente, Year, Month),
  CONSTRAINT fk_cuenta_cliente FOREIGN KEY (IdCliente) REFERENCES Cliente(IdCliente),
  CONSTRAINT CHK_Abono_Consumo CHECK (MontoAbonado <= MontoConsumido)
);

CREATE TABLE Abono (
  IdAbono INT AUTO_INCREMENT PRIMARY KEY,
  IdCuenta INT NOT NULL,
  IdEmpleado INT NOT NULL,
  Monto DECIMAL(10,2) NOT NULL,
  MetodoPago VARCHAR(15) NOT NULL,
  FechaHora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_abono_cuenta FOREIGN KEY (IdCuenta) REFERENCES CuentaCredito(IdCuenta),
  CONSTRAINT fk_abono_empleado FOREIGN KEY (IdEmpleado) REFERENCES Empleado(IdEmpleado),
  CONSTRAINT CHK_Abono_Metodo CHECK (MetodoPago IN ('EFECTIVO','YAPE'))
);

-- MODIFICACIÓN: Índice para reportes de abonos
CREATE INDEX ix_abono_fechahora ON Abono(FechaHora);
-- Cuenta + fecha: detalle de abonos por cuenta (RF05). Cubre tambien la FK IdCuenta.
CREATE INDEX ix_abono_cuenta_fecha ON Abono(IdCuenta, FechaHora);
-- Empleado + fecha: historial/arqueo por cajero (RF30, CU-031).
CREATE INDEX ix_abono_empleado_fecha ON Abono(IdEmpleado, FechaHora);


CREATE TABLE GuiaRemision (
  IdGuia INT AUTO_INCREMENT PRIMARY KEY,
  NumeroGuia VARCHAR(20) NOT NULL,
  Proveedor VARCHAR(100) NOT NULL,
  IdEmpleado INT NOT NULL,
  FechaIngreso DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_guia_empleado FOREIGN KEY (IdEmpleado) REFERENCES Empleado(IdEmpleado)
);

CREATE TABLE GuiaRemisionDetalle (
  IdGuiaDetalle INT AUTO_INCREMENT PRIMARY KEY,
  IdGuia INT NOT NULL,
  IdProducto INT NOT NULL,
  Cantidad INT NOT NULL,
  CONSTRAINT fk_guiadetalle_guia FOREIGN KEY (IdGuia) REFERENCES GuiaRemision(IdGuia),
  CONSTRAINT fk_guiadetalle_producto FOREIGN KEY (IdProducto) REFERENCES Producto(IdProducto)
);

CREATE TABLE Venta (
  IdVenta INT AUTO_INCREMENT PRIMARY KEY,
  IdEmpleado INT NOT NULL,
  IdCliente INT NULL,
  IdCuenta INT NULL,
  TipoPago VARCHAR(10) NOT NULL,
  Total DECIMAL(10,2) NOT NULL,
  Estado VARCHAR(10) NOT NULL DEFAULT 'REGISTRADA',
  FechaHora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  MfaVerificado BOOLEAN NOT NULL DEFAULT 0,
  MotivoAnulacion VARCHAR(200) NULL,
  IdEmpleadoAnula INT NULL,
  FechaAnulacion DATETIME NULL,
  CONSTRAINT fk_venta_empleado FOREIGN KEY (IdEmpleado) REFERENCES Empleado(IdEmpleado),
  CONSTRAINT fk_venta_cliente FOREIGN KEY (IdCliente) REFERENCES Cliente(IdCliente),
  CONSTRAINT fk_venta_cuenta FOREIGN KEY (IdCuenta) REFERENCES CuentaCredito(IdCuenta),
  CONSTRAINT fk_venta_empleadoanula FOREIGN KEY (IdEmpleadoAnula) REFERENCES Empleado(IdEmpleado),
  CONSTRAINT CHK_Venta_TipoPago CHECK (TipoPago IN ('CONTADO','CREDITO')),
  CONSTRAINT CHK_Venta_Estado CHECK (Estado IN ('REGISTRADA','ANULADA'))
);

-- MODIFICACIÓN: Índice para reportes de ventas
CREATE INDEX ix_venta_fechahora ON Venta(FechaHora);
-- Historial de consumo por cliente, paginado y ordenado por fecha (RF07).
CREATE INDEX ix_venta_cliente_fecha ON Venta(IdCliente, FechaHora);
-- Cuenta de credito de la venta (cubre la FK IdCuenta).
CREATE INDEX ix_venta_cuenta ON Venta(IdCuenta);
-- Arqueo por cajero (RF30).
CREATE INDEX ix_venta_empleado_fecha ON Venta(IdEmpleado, FechaHora);
-- Reportes de ventas validas por rango de fechas (RF14).
CREATE INDEX ix_venta_estado_fecha ON Venta(Estado, FechaHora);


CREATE TABLE VentaDetalle (
  IdVentaDetalle INT AUTO_INCREMENT PRIMARY KEY,
  IdVenta INT NOT NULL,
  IdProducto INT NOT NULL,
  Cantidad INT NOT NULL,
  PrecioUnitario DECIMAL(10,2) NOT NULL,
  CONSTRAINT fk_ventadetalle_venta FOREIGN KEY (IdVenta) REFERENCES Venta(IdVenta),
  CONSTRAINT fk_ventadetalle_producto FOREIGN KEY (IdProducto) REFERENCES Producto(IdProducto)
);

CREATE TABLE VentaPago (
  IdVentaPago INT AUTO_INCREMENT PRIMARY KEY,
  IdVenta INT NOT NULL,
  Monto DECIMAL(10,2) NOT NULL,
  MetodoPago VARCHAR(15) NOT NULL,
  UNIQUE(IdVenta, MetodoPago),
  CONSTRAINT fk_ventapago_venta FOREIGN KEY (IdVenta) REFERENCES Venta(IdVenta),
  CONSTRAINT CHK_VentaPago_Metodo CHECK (MetodoPago IN ('EFECTIVO','YAPE'))
);

CREATE TABLE Comprobante (
  IdComprobante INT AUTO_INCREMENT PRIMARY KEY,
  IdVenta INT NOT NULL,
  Tipo VARCHAR(12) NOT NULL,
  Serie CHAR(4) NOT NULL,
  Numero INT NOT NULL,
  Total DECIMAL(10,2) NOT NULL,
  FechaEmision DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  -- RF21: la Nota de Credito referencia al comprobante que anula.
  IdComprobanteRef INT NULL,
  -- RF12 / RF21: datos de la emision electronica SUNAT (simulada).
  XmlContenido LONGTEXT NULL,            -- XML UBL enviado
  HashFirma VARCHAR(128) NULL,           -- resumen/firma del XML
  EstadoSunat VARCHAR(10) NOT NULL DEFAULT 'PENDIENTE',
  CodigoCdr VARCHAR(10) NULL,            -- codigo de respuesta del CDR
  MensajeCdr VARCHAR(255) NULL,
  TicketSunat VARCHAR(40) NULL,
  FechaCdr DATETIME NULL,
  UNIQUE (Tipo, Serie, Numero),
  CONSTRAINT fk_comprobante_venta FOREIGN KEY (IdVenta) REFERENCES Venta(IdVenta),
  CONSTRAINT fk_comprobante_ref FOREIGN KEY (IdComprobanteRef) REFERENCES Comprobante(IdComprobante),
  CONSTRAINT CHK_Comprobante_Tipo CHECK (Tipo IN ('BOLETA','FACTURA','NOTA_CREDITO')),
  CONSTRAINT CHK_Comprobante_EstadoSunat CHECK (EstadoSunat IN ('PENDIENTE','ACEPTADO','RECHAZADO'))
);

-- MODIFICACIÓN: Índice para reportes de comprobantes
CREATE INDEX ix_comprobante_fechaemision ON Comprobante(FechaEmision);
-- Localizar comprobantes pendientes/rechazados para reintentar el envio.
CREATE INDEX ix_comprobante_estadosunat ON Comprobante(EstadoSunat);


CREATE TABLE AuditoriaAnulacion (
  IdAuditoria INT AUTO_INCREMENT PRIMARY KEY,
  IdVenta INT NOT NULL,
  IdEmpleadoAnula INT NOT NULL,
  Motivo VARCHAR(255) NOT NULL,
  FechaHora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_auditoria_venta FOREIGN KEY (IdVenta) REFERENCES Venta(IdVenta),
  CONSTRAINT fk_auditoria_empleado FOREIGN KEY (IdEmpleadoAnula) REFERENCES Empleado(IdEmpleado)
);

-- RF28: cada fila es UN envio (un cliente, un canal).
-- Intentos: 1 envio inicial + hasta 3 reintentos = maximo 4.
CREATE TABLE HistorialAlerta (
  IdAlerta INT AUTO_INCREMENT PRIMARY KEY,
  IdCliente INT NOT NULL,
  TipoAlerta VARCHAR(50) NOT NULL,       -- DEUDA, DEUDA_INDIVIDUAL, LIMITE_CREDITO...
  Canal VARCHAR(10) NULL,                -- SMS | CORREO
  Destino VARCHAR(100) NULL,             -- telefono o correo usado
  Mensaje VARCHAR(255) NOT NULL,
  Estado VARCHAR(10) NOT NULL DEFAULT 'ENVIADO',
  Intentos TINYINT NOT NULL DEFAULT 1,
  DetalleError VARCHAR(255) NULL,
  FechaEnvio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_alerta_cliente FOREIGN KEY (IdCliente) REFERENCES Cliente(IdCliente),
  CONSTRAINT CHK_Alerta_Canal CHECK (Canal IS NULL OR Canal IN ('SMS','CORREO')),
  CONSTRAINT CHK_Alerta_Estado CHECK (Estado IN ('PENDIENTE','ENVIADO','FALLIDO')),
  CONSTRAINT CHK_Alerta_Intentos CHECK (Intentos BETWEEN 0 AND 4)
);

CREATE INDEX ix_alerta_cliente_fecha ON HistorialAlerta(IdCliente, FechaEnvio);
CREATE INDEX ix_alerta_estado ON HistorialAlerta(Estado);
CREATE INDEX ix_historialalerta_canal ON HistorialAlerta(Canal);

-- MODIFICACIÓN: Tabla de Parámetros del Sistema (RF02 y RF16)
CREATE TABLE ParametroSistema (
  Clave VARCHAR(50) PRIMARY KEY,
  Valor VARCHAR(100) NOT NULL,
  Descripcion VARCHAR(255) NULL
);

-- Valores por defecto: el sistema funciona aunque no se carguen los datos de prueba.
INSERT INTO ParametroSistema (Clave, Valor, Descripcion) VALUES
  ('LIMITE_CREDITO_DEFAULT', '500.00', 'Límite de crédito por defecto al abrir una cuenta mensual'),
  ('HORA_CRON_ALERTA_STOCK', '00:00', 'Hora de ejecución de las alertas de inventario'),
  ('DIAS_ALERTA_CADUCIDAD', '7', 'Días de anticipación para lanzar advertencia de vencimiento');

-- =====================================================================
--  SERIES Y CORRELATIVOS (RF12 / RF21)
--  Una fila por (tipo, serie). El correlativo se obtiene de forma atomica
--  con sp_SiguienteNumeroComprobante, en lugar de MAX(Numero)+1.
-- =====================================================================
CREATE TABLE SerieComprobante (
  Tipo VARCHAR(12) NOT NULL,
  Serie CHAR(4) NOT NULL,
  UltimoNumero INT NOT NULL DEFAULT 0,
  Activo BOOLEAN NOT NULL DEFAULT 1,
  PRIMARY KEY (Tipo, Serie),
  CONSTRAINT CHK_Serie_Tipo CHECK (Tipo IN ('BOLETA','FACTURA','NOTA_CREDITO'))
);

INSERT INTO SerieComprobante (Tipo, Serie, UltimoNumero) VALUES
  ('BOLETA', 'B001', 0),
  ('FACTURA', 'F001', 0),
  ('NOTA_CREDITO', 'NC01', 0);

-- =====================================================================
--  AUDITORIA GENERAL (RF05)
--  Quien (Usuario), cuando (FechaHora) y que (Entidad/Accion/Detalle).
-- =====================================================================
CREATE TABLE AuditoriaOperacion (
  IdAuditoriaOp BIGINT AUTO_INCREMENT PRIMARY KEY,
  Entidad VARCHAR(30) NOT NULL,          -- ABONO, CUENTA_CREDITO...
  IdEntidad INT NOT NULL,
  Accion VARCHAR(20) NOT NULL,           -- INSERT, UPDATE, SALDAR...
  Usuario VARCHAR(50) NULL,
  FechaHora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  Detalle VARCHAR(255) NULL
);

CREATE INDEX ix_auditoriaop_entidad ON AuditoriaOperacion(Entidad, IdEntidad);
CREATE INDEX ix_auditoriaop_fecha ON AuditoriaOperacion(FechaHora);
CREATE INDEX ix_auditoriaop_usuario ON AuditoriaOperacion(Usuario, FechaHora);

-- =====================================================================
--  PROCEDIMIENTO Y TRIGGERS
--  (ejecutar con el cliente mysql / Workbench; usan DELIMITER)
-- =====================================================================
DELIMITER $$

-- Correlativo atomico. Si la transaccion de la venta hace rollback, el numero
-- se libera y no quedan huecos. Uso: CALL sp_SiguienteNumeroComprobante('BOLETA','B001',@n);
CREATE PROCEDURE sp_SiguienteNumeroComprobante(
  IN p_tipo VARCHAR(12), IN p_serie CHAR(4), OUT p_numero INT)
BEGIN
  UPDATE SerieComprobante
     SET UltimoNumero = LAST_INSERT_ID(UltimoNumero + 1)
   WHERE Tipo = p_tipo AND Serie = p_serie AND Activo = 1;
  IF ROW_COUNT() = 0 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'La serie del comprobante no existe o esta inactiva.';
  END IF;
  SET p_numero = LAST_INSERT_ID();
END$$

-- RF12: el correlativo SUNAT es inalterable.
CREATE TRIGGER trg_comprobante_inalterable BEFORE UPDATE ON Comprobante
FOR EACH ROW
BEGIN
  IF NOT (NEW.Tipo <=> OLD.Tipo) OR NOT (NEW.Serie <=> OLD.Serie)
     OR NOT (NEW.Numero <=> OLD.Numero) OR NOT (NEW.Total <=> OLD.Total)
     OR NOT (NEW.IdVenta <=> OLD.IdVenta) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Un comprobante emitido no puede cambiar tipo, serie, numero, total ni venta.';
  END IF;
END$$

CREATE TRIGGER trg_comprobante_no_eliminar BEFORE DELETE ON Comprobante
FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Un comprobante emitido no se puede eliminar; emita una Nota de Credito.';
END$$

-- RF05: auditoria automatica de cada abono (usuario, fecha y hora).
CREATE TRIGGER trg_abono_auditoria AFTER INSERT ON Abono
FOR EACH ROW
BEGIN
  INSERT INTO AuditoriaOperacion (Entidad, IdEntidad, Accion, Usuario, FechaHora, Detalle)
  VALUES ('ABONO', NEW.IdAbono, 'INSERT',
          (SELECT username FROM Empleado WHERE IdEmpleado = NEW.IdEmpleado),
          NEW.FechaHora,
          CONCAT('Cuenta ', NEW.IdCuenta, ' | ', NEW.MetodoPago, ' | S/ ', NEW.Monto));
END$$

-- RF29: al llegar la deuda a cero se registra la fecha de liquidacion;
-- si vuelve a haber deuda, se limpia (cuenta de nuevo "Con deuda").
CREATE TRIGGER trg_cuenta_saldo_cero BEFORE UPDATE ON CuentaCredito
FOR EACH ROW
BEGIN
  IF NEW.MontoConsumido - NEW.MontoAbonado <= 0 THEN
    SET NEW.FechaSaldoCero = COALESCE(NEW.FechaSaldoCero, NOW());
  ELSE
    SET NEW.FechaSaldoCero = NULL;
  END IF;
END$$

DELIMITER ;
