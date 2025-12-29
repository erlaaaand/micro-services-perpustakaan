package com.perpustakaan.service_peminjaman.entity.command;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "peminjaman")
public class Peminjaman {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long anggotaId;

    @Column(nullable = false)
    private Long bukuId;

    @Column(nullable = false)
    private String tanggalPinjam;

    @Column(nullable = false)
    private String tanggalKembali;

    @Column(nullable = false)
    private String status;
}