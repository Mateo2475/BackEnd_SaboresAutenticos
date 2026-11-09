package com.primer.api.appgpt.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.primer.api.appgpt.dto.response.CuentaCreditoResponse;
import com.primer.api.appgpt.entity.Cliente;
import com.primer.api.appgpt.entity.CuentaCredito;
import com.primer.api.appgpt.repository.ClienteRepository;
import com.primer.api.appgpt.service.CuentaCreditoService;

@RestController
@RequestMapping("/api/creditos")
public class CuentaCreditoController {

    @Autowired
    private CuentaCreditoService cuentaCreditoService;

    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping("/deudores")
    public ResponseEntity<List<CuentaCreditoResponse>> listarDeudoresDashboard() {
        List<Object[]> filas = cuentaCreditoService.listarDeudores();
        return ResponseEntity.ok(mapearVistaACuentaResponse(filas));
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<CuentaCreditoResponse>> obtenerEstadoCuenta(@PathVariable Integer idCliente) {
        List<Object[]> filas = cuentaCreditoService.obtenerEstadoCuenta(idCliente);
        return ResponseEntity.ok(mapearVistaACuentaResponse(filas));
    }

    @PostMapping("/abrir/{idCliente}")
    public ResponseEntity<CuentaCredito> abrirCuenta(@PathVariable Integer idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + idCliente));
        return ResponseEntity.ok(cuentaCreditoService.abrirCuentaMensual(cliente));
    }

    private List<CuentaCreditoResponse> mapearVistaACuentaResponse(List<Object[]> filas) {
        List<CuentaCreditoResponse> lista = new ArrayList<>();
        for (Object[] f : filas) {
            CuentaCreditoResponse dto = new CuentaCreditoResponse();
            dto.setIdCliente(((Number) f[0]).intValue());
            dto.setNombres((String) f[1]);
            dto.setApellidos((String) f[2]);
            dto.setDni((String) f[3]);
            dto.setYear(((Number) f[4]).intValue());
            dto.setMonth(((Number) f[5]).intValue());

            BigDecimal limite = new BigDecimal(f[6].toString());
            BigDecimal consumido = new BigDecimal(f[7].toString());
            BigDecimal abonado = new BigDecimal(f[8].toString());
            BigDecimal deuda = new BigDecimal(f[9].toString());

            dto.setLimiteCredito(limite);
            dto.setMontoConsumido(consumido);
            dto.setMontoAbonado(abonado);
            dto.setDeudaPendiente(deuda);
            dto.setSaldoDisponible(limite.subtract(deuda));
            dto.setEstadoDeuda((String) f[10]);
            lista.add(dto);
        }
        return lista;
    }
}