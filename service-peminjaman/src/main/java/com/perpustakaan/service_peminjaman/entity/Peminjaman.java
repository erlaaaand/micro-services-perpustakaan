package com.perpustakaan.service_peminjaman.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Peminjaman {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long anggotaId;
    private Long bukuId;
    private String tanggalPinjam;
    private String tanggalKembali;
    private String status; // "DIPINJAM" atau "KEMBALI"
}