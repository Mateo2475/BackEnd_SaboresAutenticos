package com.primer.api.appgpt.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.primer.api.appgpt.entity.Cliente;
import com.primer.api.appgpt.entity.CuentaCredito;
import com.primer.api.appgpt.repository.AbonoRepository;
import com.primer.api.appgpt.repository.CuentaCreditoRepository;

@ExtendWith(MockitoExtension.class)
class AbonoServiceTest {

    @Mock private AbonoRepository abonoRepository;
    @Mock private CuentaCreditoRepository cuentaCreditoRepository;

    @Test
    void abonoMayorQueDeudaDeberiaFallar() {
        AbonoService service = new AbonoService(abonoRepository, cuentaCreditoRepository);
        CuentaCredito c = new CuentaCredito();
        Cliente cl = new Cliente();
        cl.setNombres("Ana");
        cl.setApellidos("Lopez");
        c.setCliente(cl);
        c.setMontoConsumido(new BigDecimal("50.00"));
        c.setMontoAbonado(BigDecimal.ZERO);
        when(cuentaCreditoRepository.findCuentasConDeuda(1)).thenReturn(List.of(c));
        assertThrows(RuntimeException.class, () -> service.registrarAbono(1, null, new BigDecimal("100.00"), "EFECTIVO"));
    }

    @Test
    void abonoValidoDeberiaAplicarse() {
        AbonoService service = new AbonoService(abonoRepository, cuentaCreditoRepository);
        CuentaCredito c = new CuentaCredito();
        Cliente cl = new Cliente();
        cl.setNombres("Ana");
        cl.setApellidos("Lopez");
        c.setCliente(cl);
        c.setMontoConsumido(new BigDecimal("50.00"));
        c.setMontoAbonado(BigDecimal.ZERO);
        when(cuentaCreditoRepository.findCuentasConDeuda(1)).thenReturn(List.of(c));
        when(cuentaCreditoRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(i -> i.getArgument(0));
        when(abonoRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(i -> i.getArgument(0));
        when(cuentaCreditoRepository.totalDeudaCliente(1)).thenReturn(new BigDecimal("0.00"));
        var r = service.registrarAbono(1, null, new BigDecimal("20.00"), "EFECTIVO");
        assertEquals(new BigDecimal("20.00"), r.getMontoAplicado());
    }
}