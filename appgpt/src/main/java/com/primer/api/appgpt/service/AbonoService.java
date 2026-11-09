package com.primer.api.appgpt.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.primer.api.appgpt.dto.EntityMapper;
import com.primer.api.appgpt.dto.response.AbonoAplicadoResponse;
import com.primer.api.appgpt.entity.Abono;
import com.primer.api.appgpt.entity.CuentaCredito;
import com.primer.api.appgpt.entity.Empleado;
import com.primer.api.appgpt.repository.AbonoRepository;
import com.primer.api.appgpt.repository.CuentaCreditoRepository;

/**
 * Registro de abonos (RF05 / CU-004).
 *
 * El abono se aplica primero a la deuda más antigua y continúa con los meses
 * siguientes. Así se cubren deudas de meses anteriores y no solo el mes actual.
 */
@Service
public class AbonoService {

    private final AbonoRepository abonoRepository;
    private final CuentaCreditoRepository cuentaCreditoRepository;

    public AbonoService(AbonoRepository abonoRepository,
                        CuentaCreditoRepository cuentaCreditoRepository) {
        this.abonoRepository = abonoRepository;
        this.cuentaCreditoRepository = cuentaCreditoRepository;
    }

    @Transactional
    public AbonoAplicadoResponse registrarAbono(Integer idCliente, Empleado empleadoCajero,
                                                BigDecimal montoAbono, String metodoPago) {

        if (montoAbono == null || montoAbono.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El monto del abono debe ser mayor a S/ 0.00.");
        }

        List<CuentaCredito> cuentasConDeuda = cuentaCreditoRepository.findCuentasConDeuda(idCliente);
        if (cuentasConDeuda.isEmpty()) {
            throw new RuntimeException("El cliente no presenta deuda pendiente en ningún mes.");
        }

        BigDecimal deudaTotal = cuentasConDeuda.stream()
                .map(c -> c.getMontoConsumido().subtract(c.getMontoAbonado()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (montoAbono.compareTo(deudaTotal) > 0) {
            throw new RuntimeException("El monto a abonar (S/ " + montoAbono
                    + ") supera la deuda total del cliente (S/ " + deudaTotal + ").");
        }

        BigDecimal restante = montoAbono;
        List<Abono> abonosGenerados = new ArrayList<>();

        for (CuentaCredito cuenta : cuentasConDeuda) {
            if (restante.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal deudaCuenta = cuenta.getMontoConsumido().subtract(cuenta.getMontoAbonado());
            if (deudaCuenta.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal aplicar = restante.min(deudaCuenta);
            cuenta.setMontoAbonado(cuenta.getMontoAbonado().add(aplicar));

            BigDecimal nuevaDeudaCuenta = cuenta.getMontoConsumido().subtract(cuenta.getMontoAbonado());
            if (nuevaDeudaCuenta.compareTo(BigDecimal.ZERO) <= 0) {
                cuenta.setFechaSaldoCero(LocalDateTime.now(java.time.ZoneId.of("America/Lima")));
            }
            cuentaCreditoRepository.save(cuenta);

            Abono abono = new Abono();
            abono.setCuentaCredito(cuenta);
            abono.setEmpleado(empleadoCajero);
            abono.setMonto(aplicar);
            abono.setMetodoPago(metodoPago == null ? "EFECTIVO" : metodoPago.toUpperCase());
            abono.setFechaHora(LocalDateTime.now(java.time.ZoneId.of("America/Lima")));
            abonosGenerados.add(abonoRepository.save(abono));

            restante = restante.subtract(aplicar);
        }

        CuentaCredito primera = cuentasConDeuda.get(0);

        AbonoAplicadoResponse respuesta = new AbonoAplicadoResponse();
        respuesta.setIdCliente(idCliente);
        respuesta.setCliente(primera.getCliente().getNombres() + " " + primera.getCliente().getApellidos());
        respuesta.setMontoSolicitado(montoAbono);
        respuesta.setMontoAplicado(montoAbono.subtract(restante));
        respuesta.setDeudaRestante(cuentaCreditoRepository.totalDeudaCliente(idCliente));
        respuesta.setMensaje("Abono registrado y distribuido entre " + abonosGenerados.size()
                + " cuenta(s) mensual(es).");
        respuesta.setAbonos(abonosGenerados.stream().map(EntityMapper::toAbonoResponse).toList());

        return respuesta;
    }
}
