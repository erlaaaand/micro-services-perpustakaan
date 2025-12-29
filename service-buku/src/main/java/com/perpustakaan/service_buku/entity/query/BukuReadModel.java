package com.perpustakaan.service_buku.entity.query;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "buku_read")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BukuReadModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String kodeBuku;
    
    @Column(nullable = false)
    private String judul;
    
    @Column(nullable = false)
    private String pengarang;
    
    private String penerbit;
    
    @Column(nullable = false)
    private Integer tahunTerbit;
}
