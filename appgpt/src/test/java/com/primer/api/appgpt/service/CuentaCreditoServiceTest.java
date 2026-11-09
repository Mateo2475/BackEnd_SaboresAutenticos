package com.primer.api.appgpt.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.primer.api.appgpt.entity.Cliente;
import com.primer.api.appgpt.entity.CuentaCredito;
import com.primer.api.appgpt.repository.CuentaCreditoRepository;

@ExtendWith(MockitoExtension.class)
class CuentaCreditoServiceTest {

    @Mock private CuentaCreditoRepository cuentaCreditoRepository;

    @Test
    void abrirCuentaExistenteDevuelveLaMisma() {
        CuentaCredito existente = new CuentaCredito();
        Cliente c = new Cliente();
        c.setIdCliente(5);
        existente.setCliente(c);
        when(cuentaCreditoRepository.findByClienteAndYearAndMonth(org.mockito.ArgumentMatchers.eq(5),
                org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(Optional.of(existente));

        CuentaCreditoService service = new CuentaCreditoService(cuentaCreditoRepository);
        Cliente cliente = new Cliente();
        cliente.setIdCliente(5);
        assertEquals(existente, service.abrirCuentaMensual(cliente));
    }

    @Test
    void totalDeudaClienteNuloDevuelveCero() {
        when(cuentaCreditoRepository.totalDeudaCliente(9)).thenReturn(null);
        CuentaCreditoService service = new CuentaCreditoService(cuentaCreditoRepository);
        assertEquals(BigDecimal.ZERO, service.totalDeudaCliente(9));
    }
}