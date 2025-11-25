package com.example.BackendSSA.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtoFavoritoProducto {

    private Integer idProducto;
    private Integer idUsuario;
}
