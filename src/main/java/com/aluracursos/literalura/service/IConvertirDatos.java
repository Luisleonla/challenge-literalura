package com.aluracursos.literalura.service;

public interface IConvertirDatos {
    <T> T convertir(String json, Class<T> clase);
}
