package com.aluracursos.literalura.repository;

import com.aluracursos.literalura.models.Autor;
import com.aluracursos.literalura.models.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    Optional<Libro> findByIdGutendex(Long idGutendex);
    Optional<Libro> findById(Long id);
    List<Libro> findAll();
    @Query("SELECT l FROM Libro l WHERE :lenguaje MEMBER OF l.lenguajes")
    List<Libro> buscarLibrosPorIdioma(String lenguaje);
}
