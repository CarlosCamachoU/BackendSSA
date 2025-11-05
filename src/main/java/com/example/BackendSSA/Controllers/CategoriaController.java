package com.example.BackendSSA.Controllers;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BackendSSA.Entities.CategoriaEntities;
import com.example.BackendSSA.Repositories.CategoriaRepository;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    
    @Autowired
    private CategoriaRepository categoriaRepository;


    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaEntities>> getAllCategorias() {
        List<CategoriaEntities> categorias = categoriaRepository.findAll();
        return ResponseEntity.ok(categorias);
    }
 
}
