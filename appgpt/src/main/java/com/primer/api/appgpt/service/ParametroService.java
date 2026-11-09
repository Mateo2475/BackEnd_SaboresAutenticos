package com.primer.api.appgpt.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.primer.api.appgpt.entity.ParametroSistema;
import com.primer.api.appgpt.repository.ParametroSistemaRepository;

@Service
public class ParametroService {

    private final ParametroSistemaRepository repository;

    public ParametroService(ParametroSistemaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ParametroSistema> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public String obtenerValor(String clave, String porDefecto) {
        return repository.findById(clave).map(ParametroSistema::getValor).orElse(porDefecto);
    }

    @Transactional
    public ParametroSistema guardar(ParametroSistema parametro) {
        return repository.save(parametro);
    }

    @Transactional
    public ParametroSistema actualizarValor(String clave, String valor) {
        ParametroSistema p = repository.findById(clave).orElseGet(() -> {
            ParametroSistema np = new ParametroSistema();
            np.setClave(clave);
            return np;
        });
        p.setValor(valor);
        return repository.save(p);
    }

    @Transactional(readOnly = true)
    public double limiteCreditoDefault() {
        try {
            return Double.parseDouble(obtenerValor("LIMITE_CREDITO_DEFAULT", "500.00"));
        } catch (NumberFormatException e) {
            return 500.0;
        }
    }

    @Transactional(readOnly = true)
    public int diasAlertaCaducidad() {
        try {
            return Integer.parseInt(obtenerValor("DIAS_ALERTA_CADUCIDAD", "7"));
        } catch (NumberFormatException e) {
            return 7;
        }
    }
}