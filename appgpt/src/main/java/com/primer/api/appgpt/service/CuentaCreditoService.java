package com.primer.api.appgpt.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.primer.api.appgpt.entity.Cliente;
import com.primer.api.appgpt.entity.CuentaCredito;
import com.primer.api.appgpt.repository.CuentaCreditoRepository;

@Service
public class CuentaCreditoService {

    private final CuentaCreditoRepository cuentaCreditoRepository;
    private final ParametroService parametroService;

    public CuentaCreditoService(CuentaCreditoRepository cuentaCreditoRepository) {
        this(cuentaCreditoRepository, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public CuentaCreditoService(CuentaCreditoRepository cuentaCreditoRepository, ParametroService parametroService) {
        this.cuentaCreditoRepository = cuentaCreditoRepository;
        this.parametroService = parametroService;
    }

    @Transactional
    public CuentaCredito abrirCuentaMensual(Cliente cliente) {
        LocalDate hoy = LocalDate.now(java.time.ZoneId.of("America/Lima"));
        int year = hoy.getYear();
        int month = hoy.getMonthValue();

        Optional<CuentaCredito> cuentaExistente = cuentaCreditoRepository
                .findByClienteAndYearAndMonth(cliente.getIdCliente(), year, month);

        if (cuentaExistente.isPresent()) {
            return cuentaExistente.get();
        }

        CuentaCredito nuevaCuenta = new CuentaCredito();
        nuevaCuenta.setCliente(cliente);
        nuevaCuenta.setYear(year);
        nuevaCuenta.setMonth(month);
        double limite = parametroService != null ? parametroService.limiteCreditoDefault() : 500.0;
        nuevaCuenta.setLimiteCredito(java.math.BigDecimal.valueOf(limite));

        return cuentaCreditoRepository.save(nuevaCuenta);
    }

    /**
     * CU-015 / RF17: valida el límite de crédito considerando TODAS las deudas
     * pendientes del cliente (meses anteriores incluidos), no solo el mes actual.
     * Usa bloqueo pesimista para evitar sobrepasar el límite en ventas concurrentes.
     */
    @Transactional
    public CuentaCredito validarYRegistrarConsumo(Integer idCliente, BigDecimal montoNuevoPedido) {
        LocalDate hoy = LocalDate.now(java.time.ZoneId.of("America/Lima"));
        int year = hoy.getYear();
        int month = hoy.getMonthValue();

        CuentaCredito cuentaActual = cuentaCreditoRepository
                .findByClienteAndYearAndMonthForUpdate(idCliente, year, month)
                .orElseThrow(() -> new RuntimeException(
                        "El cliente no tiene una cuenta de crédito abierta para este mes. "
                        + "Ábrala antes de vender a crédito."));

        BigDecimal deudaAnterior = cuentaCreditoRepository.totalDeudaCliente(idCliente);
        if (deudaAnterior == null) {
            deudaAnterior = BigDecimal.ZERO;
        }

        BigDecimal deudaProyectada = deudaAnterior.add(montoNuevoPedido);

        if (deudaProyectada.compareTo(cuentaActual.getLimiteCredito()) > 0) {
            BigDecimal saldoDisponible = cuentaActual.getLimiteCredito().subtract(deudaAnterior);
            if (saldoDisponible.compareTo(BigDecimal.ZERO) < 0) {
                saldoDisponible = BigDecimal.ZERO;
            }
            throw new RuntimeException("Límite de crédito superado. Deuda actual (todos los meses): S/ "
                    + deudaAnterior + ". Saldo disponible: S/ " + saldoDisponible
                    + ". Pedido: S/ " + montoNuevoPedido + ".");
        }

        BigDecimal nuevoMontoConsumido = cuentaActual.getMontoConsumido().add(montoNuevoPedido);
        cuentaActual.setMontoConsumido(nuevoMontoConsumido);
        return cuentaCreditoRepository.save(cuentaActual);
    }

    @Transactional
    public CuentaCredito actualizarCuenta(CuentaCredito cuenta) {
        return cuentaCreditoRepository.save(cuenta);
    }

    @Transactional(readOnly = true)
    public BigDecimal totalDeudaCliente(Integer idCliente) {
        BigDecimal total = cuentaCreditoRepository.totalDeudaCliente(idCliente);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Transactional(readOnly = true)
    public BigDecimal totalDeudaGlobal() {
        BigDecimal total = cuentaCreditoRepository.totalDeudaGlobal();
        return total == null ? BigDecimal.ZERO : total;
    }

    @Transactional(readOnly = true)
    public Long contarClientesConDeuda() {
        return cuentaCreditoRepository.contarClientesConDeuda();
    }

    public List<Object[]> obtenerEstadoCuenta(Integer idCliente) {
        return cuentaCreditoRepository.obtenerEstadoCuentaPorCliente(idCliente);
    }

    public List<Object[]> listarDeudores() {
        return cuentaCreditoRepository.listarDeudoresDashboard();
    }

    public List<Object[]> listarTopDeudoresAgrupado() {
        return cuentaCreditoRepository.listarTopDeudoresAgrupado();
    }
}
