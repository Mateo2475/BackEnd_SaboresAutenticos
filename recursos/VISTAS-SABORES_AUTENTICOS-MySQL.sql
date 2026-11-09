USE SaboresAutenticos;
SET time_zone = '-05:00';

DROP VIEW IF EXISTS V_ProductosMasVendidos;
CREATE VIEW V_ProductosMasVendidos AS
SELECT
    p.IdProducto,
    p.Nombre,
    CASE
        WHEN cp.IdCategoria IS NOT NULL THEN CONCAT(cp.Nombre, ' - ', c.Nombre)
        ELSE c.Nombre
    END AS Categoria,
    SUM(vd.Cantidad) AS TotalUnidadesVendidas,
    SUM(vd.Cantidad * vd.PrecioUnitario) AS IngresoTotalGenerado
FROM
    Producto p
INNER JOIN
    Categoria c ON p.IdCategoria = c.IdCategoria
LEFT JOIN
    Categoria cp ON c.IdCategoriaPadre = cp.IdCategoria
INNER JOIN
    VentaDetalle vd ON p.IdProducto = vd.IdProducto
INNER JOIN
    Venta v ON vd.IdVenta = v.IdVenta
WHERE
    v.Estado = 'REGISTRADA'
GROUP BY
    p.IdProducto, p.Nombre, Categoria;

DROP VIEW IF EXISTS V_EstadoCuentaCliente;
CREATE VIEW V_EstadoCuentaCliente AS
SELECT
    c.IdCliente,
    c.Nombres,
    c.Apellidos,
    c.DNI,
    cc.Year,
    cc.Month,
    cc.LimiteCredito,
    cc.MontoConsumido,
    cc.MontoAbonado,
    (cc.MontoConsumido - cc.MontoAbonado) AS DeudaPendiente,
    CASE
        WHEN (cc.MontoConsumido - cc.MontoAbonado) <= 0 THEN 'SIN DEUDA'
        ELSE 'CON DEUDA'
    END AS EstadoDeuda
FROM
    Cliente c
INNER JOIN
    CuentaCredito cc ON c.IdCliente = cc.IdCliente;

DROP VIEW IF EXISTS V_ReporteVentasMensual;
CREATE VIEW V_ReporteVentasMensual AS
SELECT
    YEAR(v.FechaHora) AS Anio,
    MONTH(v.FechaHora) AS Mes,
    v.TipoPago,
    COUNT(v.IdVenta) AS CantidadVentas,
    SUM(v.Total) AS TotalRecaudado
FROM
    Venta v
WHERE
    v.Estado = 'REGISTRADA'
GROUP BY
    YEAR(v.FechaHora), MONTH(v.FechaHora), v.TipoPago;

DROP VIEW IF EXISTS V_HistorialCobrosCajero;
CREATE VIEW V_HistorialCobrosCajero AS
SELECT
    e.IdEmpleado,
    CONCAT(e.Nombres, ' ', e.Apellidos) AS Cajero,
    a.FechaHora AS FechaOperacion,
    'ABONO DEUDA' AS TipoOperacion,
    a.MetodoPago,
    a.Monto
FROM
    Empleado e
INNER JOIN
    Abono a ON e.IdEmpleado = a.IdEmpleado
UNION ALL
SELECT
    e.IdEmpleado,
    CONCAT(e.Nombres, ' ', e.Apellidos) AS Cajero,
    v.FechaHora AS FechaOperacion,
    'VENTA DIRECTA' AS TipoOperacion,
    vp.MetodoPago,
    vp.Monto
FROM
    Empleado e
INNER JOIN
    Venta v ON e.IdEmpleado = v.IdEmpleado
INNER JOIN
    VentaPago vp ON v.IdVenta = vp.IdVenta
WHERE
    v.Estado = 'REGISTRADA';

DROP VIEW IF EXISTS V_AlertasInventario;
CREATE VIEW V_AlertasInventario AS
SELECT
    p.IdProducto,
    p.Nombre,
    CASE
        WHEN cp.IdCategoria IS NOT NULL THEN CONCAT(cp.Nombre, ' - ', c.Nombre)
        ELSE c.Nombre
    END AS Categoria,
    p.Stock,
    p.StockMinimo,
    p.FechaCaducidad,
    CASE
        WHEN p.Stock <= 0 THEN 'CRITICO - SIN STOCK'
        WHEN p.Stock <= p.StockMinimo THEN 'ADVERTENCIA - STOCK BAJO'
        ELSE 'OPTIMO'
    END AS EstadoStock,
    CASE
        WHEN p.FechaCaducidad IS NULL THEN 'NO APLICA'
        WHEN p.FechaCaducidad < CURDATE() THEN 'VENCIDO'
        WHEN p.FechaCaducidad <= DATE_ADD(CURDATE(), INTERVAL COALESCE(
             (SELECT CAST(ps.Valor AS UNSIGNED) FROM ParametroSistema ps
               WHERE ps.Clave = 'DIAS_ALERTA_CADUCIDAD'), 7) DAY) THEN 'PROXIMO A VENCER'
        ELSE 'VIGENTE'
    END AS EstadoCaducidad
FROM
    Producto p
INNER JOIN
    Categoria c ON p.IdCategoria = c.IdCategoria
LEFT JOIN
    Categoria cp ON c.IdCategoriaPadre = cp.IdCategoria
WHERE
    p.Activo = 1;

-- RF27: ranking de deudores (todos los meses), orden por deuda y desempate
-- alfabetico por apellidos. Se consulta con: SELECT * FROM V_TopDeudores LIMIT 10;
DROP VIEW IF EXISTS V_TopDeudores;
CREATE VIEW V_TopDeudores AS
SELECT
    c.IdCliente,
    c.Nombres,
    c.Apellidos,
    c.DNI,
    SUM(cc.MontoConsumido - cc.MontoAbonado) AS DeudaTotal,
    COUNT(*) AS MesesConDeuda
FROM
    Cliente c
INNER JOIN
    CuentaCredito cc ON c.IdCliente = cc.IdCliente
WHERE
    (cc.MontoConsumido - cc.MontoAbonado) > 0
GROUP BY
    c.IdCliente, c.Nombres, c.Apellidos, c.DNI
ORDER BY
    DeudaTotal DESC, c.Apellidos ASC, c.Nombres ASC;

-- RF05: consulta de auditoria de abonos y otras operaciones.
DROP VIEW IF EXISTS V_AuditoriaOperaciones;
CREATE VIEW V_AuditoriaOperaciones AS
SELECT
    IdAuditoriaOp,
    FechaHora,
    Usuario,
    Entidad,
    IdEntidad,
    Accion,
    Detalle
FROM
    AuditoriaOperacion;

-- RF28: resultado de cada envio de alerta (canal, estado e intentos).
DROP VIEW IF EXISTS V_HistorialAlertasEnvio;
CREATE VIEW V_HistorialAlertasEnvio AS
SELECT
    h.IdAlerta,
    CONCAT(c.Nombres, ' ', c.Apellidos) AS Cliente,
    h.TipoAlerta,
    h.Canal,
    h.Destino,
    h.Estado,
    h.Intentos,
    h.DetalleError,
    h.FechaEnvio
FROM
    HistorialAlerta h
INNER JOIN
    Cliente c ON h.IdCliente = c.IdCliente;
