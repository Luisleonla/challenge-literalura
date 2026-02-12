package com.aluracursos.literalura.repository;

import com.aluracursos.literalura.models.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AutorRepository  extends JpaRepository<Autor, Long> {
    Optional<Autor> findByAutorIgnoreCase(String nombre);
    @Query("""
            SELECT a FROM Autor a 
            WHERE a.anioNacimiento <= :anio 
            AND (a.anioDeceso >= :anio 
                OR (a.anioDeceso IS NULL AND (:anio - a.anioNacimiento) <= 100))
            """)
    List<Autor> busquedaAutoresEnAnio(int anio);
}
