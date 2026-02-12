package com.aluracursos.literalura.service;

import com.aluracursos.literalura.models.Autor;
import com.aluracursos.literalura.models.DatosLibro;
import com.aluracursos.literalura.models.Libro;
import com.aluracursos.literalura.repository.AutorRepository;
import com.aluracursos.literalura.repository.LibroRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LibroService {
    @Autowired
    private LibroRepository libroRepository;
    @Autowired
    private AutorRepository autorRepository;

    @Transactional
    public void guardarLibro(DatosLibro datosLibro) {
        if (libroRepository.findByIdGutendex(datosLibro.idGutendex()).isPresent()) {
            System.out.println("Este libro ya se encuentra en tu base de datos");
            return;
        }
        Libro libro = new Libro();
        libro.setIdGutendex(datosLibro.idGutendex());
        libro.setTitulo(datosLibro.titulo());
        List<Autor> autoresEntity = datosLibro.autores().stream()
                .map(da -> autorRepository.findByAutorIgnoreCase(da.autor())
                        .orElseGet(() -> {
                            Autor nuevoAutor = new Autor();
                            nuevoAutor.setAutor(da.autor());
                            nuevoAutor.setAnioNacimiento(da.anioNacimiento());
                            nuevoAutor.setAnioDeceso(da.anioDeceso());
                            return autorRepository.save(nuevoAutor);
                        }))
                .collect(Collectors.toList());
        libro.setAutores(autoresEntity);
        libro.setLenguajes(datosLibro.lenguajes());
        libro.setDescargas(datosLibro.descargas());

        libroRepository.save(libro);
        System.out.println("Libro guardado exitosamente!");
    }
}
