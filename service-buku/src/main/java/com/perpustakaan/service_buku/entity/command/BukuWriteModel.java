package com.perpustakaan.service_buku.entity.command;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "buku")
public class BukuWriteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "kode_buku", unique = true, nullable = false)
    private String kodeBuku;
    
    @Column(name = "judul", nullable = false)
    private String judul;
    
    @Column(name = "pengarang", nullable = false)
    private String pengarang;
    
    @Column(name = "penerbit")
    private String penerbit;
    
    @Column(name = "tahun_terbit", nullable = false)
    private Integer tahunTerbit;
}