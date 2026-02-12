package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.models.*;
import com.aluracursos.literalura.repository.AutorRepository;
import com.aluracursos.literalura.repository.LibroRepository;
import com.aluracursos.literalura.service.ConsumoAPIGutendex;
import com.aluracursos.literalura.service.ConvertirDatos;
import com.aluracursos.literalura.service.LibroService;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private final String url = "https://gutendex.com/books/?search=";
    private ConsumoAPIGutendex consulta = new ConsumoAPIGutendex();
    private Scanner entrada = new Scanner(System.in);
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;
    private Optional<DatosLibro> libroEncontrado;
    private LibroService libroService;
    private List<Libro> librosDB;

    public Principal(LibroService libroService, LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroService = libroService;
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void correrPrograma() {
        String opcion = "-1";
        while (!opcion.equals("0")) {
            System.out.println("""
                    \n----------------------------------------
                    Escribe la opción deseada:                    
                    1 - Busqueda de libro por palabra clave
                    2 - Eliminar libro
                    3 - Listar libros registrados
                    4 - Listar autores registrados
                    5 - Buscar autores vivos en determinado año
                    6 - Buscar libros por lenguaje
                                        
                    0 - Salir
                    ----------------------------------------
                    """);
            opcion = entrada.nextLine();
            switch (opcion) {
                case "1" -> busqueda();
                case "2" -> eliminarLibro();
                case "3" -> listarDBLibros();
                case "4" -> listarDBAutores();
                case "5" -> busquedaAutoresEnAnio();
                case "6" -> busquedaPorLenguaje();
                case "0" -> System.out.println("Fin del programa");
                default -> System.out.println("Opción no valida:" + opcion);
            }
        }
    }

    public void busqueda() {
        boolean continuarBusqueda = true;
        while (continuarBusqueda) {
            System.out.println("\nEscribe el titulo o palabra clave del libro:");
            String busquedaPorTitulo = entrada.nextLine();

            try {
                String jsonConsulta = consulta.consultaDatosLibros(url + busquedaPorTitulo.replace(" ", "%20"));
                //System.out.println(jsonConsulta);
                ConvertirDatos convertirDatos = new ConvertirDatos();
                Resultados resultados = convertirDatos.convertir(jsonConsulta, Resultados.class);
                libroEncontrado = resultados.listaLibros().stream()
                        .findFirst();
                if (libroEncontrado.isEmpty()) {
                    System.out.println("Libro no encontrado. Intenta con otras palabras.");
                    return;
                }
                DatosLibro datos = libroEncontrado.get();
                Libro libro = new Libro(datos);
                mostrarResumenLibro(libro);
                System.out.println("\n¿ES el libro que buscabas? (1 - Si / 0 - No)");
                String respuesta = entrada.nextLine();
                if (respuesta.equals("1")) {
                    realizarGuardado(datos);
                    continuarBusqueda = false;
                } else {
                    System.out.println("Intentemos de nuevo...");
                }
            } catch (Exception e) {
                System.out.println("Ocurrio un error: " + e.getMessage());
                break;
            }
        }
    }

    public void mostrarResumenLibro(Libro libro) {
        System.out.printf("""
                        \n----------- LIBRO -----------
                        Titulo: %s
                        Autor: %s
                        Idioma: %s
                        Número de descargas: %d
                        ----------------------------------\n""",
                libro.getTitulo(),
                libro.getAutores().stream().findFirst().get().getAutor(),
                libro.getLenguajes().stream().findFirst().orElse("Desconocido"),
                libro.getDescargas()
        );
    }

    public void mostrarResumenAutor(Autor datosAutores) {
        List<String> tituloLibros = datosAutores.getLibros().stream()
                .map(Libro::getTitulo)
                .collect(Collectors.toList());
        System.out.printf("""
                        \n----------- AUTOR -----------
                        Nombre: %s
                        Año de nacimiento: %s
                        Año de fallecimiento: %s
                        Libros: %s
                        ----------------------------------\n""",
                datosAutores.getAutor(),
                datosAutores.getAnioNacimiento(),
                datosAutores.getAnioDeceso(),
                tituloLibros
        );
    }

    public void realizarGuardado(DatosLibro datos){
        try {
            libroService.guardarLibro(datos);
        }catch (Exception e) {
            System.out.println("No se pudo guardar el libro: " + e.getMessage());
        }

    }

    public void eliminarLibro() {
        libroRepository.findAll().stream()
                .forEach(l -> System.out.println(l.getId() + ": " +
                        l.getTitulo() + " de " + l.getAutores().stream().findFirst().get().getAutor()));
        System.out.println("Indica que libro deseas eliminar o digita 0 para cancelar");
        String eliminarLibro = entrada.nextLine();
        if (eliminarLibro.equals("0")) {
            return;
        } else {
            try {
                Long numeroEliminarLibro = Long.parseLong(eliminarLibro);
                if (libroRepository.existsById(numeroEliminarLibro)) {
                    libroRepository.deleteById(numeroEliminarLibro);
                    System.out.println("Libro eliminado con exito");
                } else {
                    System.out.println("No se encontró ningún libro con el ID interno:");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Debes ingresar un número válido para el ID.");
            }
        }
    }

    public void listarDBLibros() {
        librosDB = libroRepository.findAll();
        librosDB.stream()
                .forEach(l -> mostrarResumenLibro(l));
    }

    public void listarDBAutores() {
        List<Autor> autoresDB = autorRepository.findAll();
        autoresDB.stream()
                .forEach(a -> mostrarResumenAutor(a));
    }

    public void busquedaAutoresEnAnio() {
        System.out.println("Esribe el año en que deseas tu busqueda");
        String anioBusqueda = entrada.nextLine();

        try {
            int numeroAnioBusqueda = Integer.parseInt(anioBusqueda);
            List<Autor> listaAutores = autorRepository.busquedaAutoresEnAnio(numeroAnioBusqueda);
            if(listaAutores.isEmpty()){
                System.out.println("No se encontraron autores vivos en el año indicado");
            }else {
                System.out.println("\n --------------- AUTORES VIVOS EN " + numeroAnioBusqueda + " ---------------");
                listaAutores.forEach(a -> System.out.printf(
                        "Autor: %s | Nacimiento: %s | Deceso: %s\n",
                        a.getAutor(),
                        a.getAnioNacimiento(),
                        (a.getAnioDeceso() == null ? "N/A (Sigue vivo)" : a.getAnioDeceso())
                ));
            }
        }catch (NumberFormatException e) {
            System.out.println("Error: Por favor, igresa un año válido en formato numérico");
        }
    }

    public void busquedaPorLenguaje() {
        librosDB = libroRepository.findAll();
        Set<String> listaLenguajes = librosDB.stream()
                .flatMap(l -> l.getLenguajes().stream())
                .collect(Collectors.toCollection(TreeSet::new));
        System.out.println("Indica la busqueda de libros por idioma que deseas: ");
        listaLenguajes.forEach(len -> System.out.println(len + " - " + obtenerNombreIdioma(len)));
        String idioma = entrada.nextLine();
        librosDB = libroRepository.buscarLibrosPorIdioma(idioma);
        librosDB.stream()
                .forEach(l -> mostrarResumenLibro(l));

    }

    private String obtenerNombreIdioma(String len){
        return switch (len.toLowerCase()) {
            case "es" -> "español";
            case "en" -> "inglés";
            case "fr" -> "francés";
            case"pt" -> "portugués";
            case "it" -> "italiano";
            case "de" -> "alemán";
            case "hu" -> "húngaro";
            case "ru" -> "ruso";
            default -> "desconocido (" + len + ")";
        };
    }
}
