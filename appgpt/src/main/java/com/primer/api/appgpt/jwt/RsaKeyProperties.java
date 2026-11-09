package com.primer.api.appgpt.jwt;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Par de claves RSA (RS256) usado para firmar y verificar los JWT.
 *
 * Si las propiedades jwt.public-key / jwt.private-key vienen vacías,
 * se genera un par de claves al iniciar la aplicación (formato DER en Base64:
 * X.509 para la pública y PKCS#8 para la privada).
 */
@Component
public class RsaKeyProperties {

    private static final Logger log = LoggerFactory.getLogger(RsaKeyProperties.class);

    @Value("${jwt.public-key:}")
    private String publicKeyBase64;

    @Value("${jwt.private-key:}")
    private String privateKeyBase64;

    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    private static final Path ARCHIVO_CLAVES = Paths.get("config", "jwt-keys.properties");

    @PostConstruct
    public void inicializar() throws Exception {
        if (publicKeyBase64 != null && !publicKeyBase64.isBlank()
                && privateKeyBase64 != null && !privateKeyBase64.isBlank()) {
            cargarDesdeBase64(publicKeyBase64, privateKeyBase64);
            log.info("Claves RSA cargadas desde variables/propiedades");
            return;
        }

        // Persistencia en archivo para no regenerar claves en cada arranque
        if (Files.exists(ARCHIVO_CLAVES)) {
            Properties props = new Properties();
            try (var in = Files.newInputStream(ARCHIVO_CLAVES)) {
                props.load(in);
            }
            cargarDesdeBase64(props.getProperty("jwt.public-key"), props.getProperty("jwt.private-key"));
            log.info("Claves RSA cargadas desde {}", ARCHIVO_CLAVES);
            return;
        }

        KeyPairGenerator generador = KeyPairGenerator.getInstance("RSA");
        generador.initialize(2048);
        KeyPair par = generador.generateKeyPair();
        this.publicKey = (RSAPublicKey) par.getPublic();
        this.privateKey = (RSAPrivateKey) par.getPrivate();

        Files.createDirectories(ARCHIVO_CLAVES.getParent());
        Properties props = new Properties();
        props.setProperty("jwt.public-key", Base64.getEncoder().encodeToString(this.publicKey.getEncoded()));
        props.setProperty("jwt.private-key", Base64.getEncoder().encodeToString(this.privateKey.getEncoded()));
        try (var out = Files.newOutputStream(ARCHIVO_CLAVES, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            props.store(out, "Claves RSA persistentes - no compartir");
        }
        log.warn("Se generó y persistió un nuevo par de claves RSA en {}", ARCHIVO_CLAVES);
    }

    private void cargarDesdeBase64(String pub, String priv) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] pubBytes = Base64.getDecoder().decode(pub.trim());
        byte[] privBytes = Base64.getDecoder().decode(priv.trim());
        this.publicKey = (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(pubBytes));
        this.privateKey = (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privBytes));
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
