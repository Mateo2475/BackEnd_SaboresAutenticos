package com.primer.api.appgpt.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.primer.api.appgpt.entity.Producto;

import jakarta.persistence.LockModeType;


public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    /**
     * Bloqueo pesimista de fila: serializa las ventas concurrentes del mismo
     * producto para impedir la doble venta cuando queda poco stock (RF10 / CU-011).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.idProducto = :idProducto")
    Optional<Producto> findByIdForUpdate(@Param("idProducto") Integer idProducto);

       @Query(value = "SELECT * FROM V_ProductosMasVendidos ORDER BY TotalUnidadesVendidas DESC", nativeQuery = true)
    List<Object[]> obtenerRankingDeVentas();

   
    @Query(value = "SELECT * FROM V_AlertasInventario WHERE EstadoStock != 'OPTIMO' OR EstadoCaducidad != 'VIGENTE'", nativeQuery = true)
    List<Object[]> obtenerAlertasInventario();
    
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    boolean existsByCategoria_IdCategoria(Integer idCategoria);

    org.springframework.data.domain.Page<Producto> findByActivoTrue(org.springframework.data.domain.Pageable pageable);
}