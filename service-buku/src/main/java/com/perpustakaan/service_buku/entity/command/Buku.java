package com.perpustakaan.service_buku.entity.command;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "buku")
public class Buku {
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