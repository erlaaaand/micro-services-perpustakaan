package com.perpustakaan.service_peminjaman.entity.command;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "peminjaman")
public class PeminjamanWriteModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID anggotaId;

    @Column(nullable = false)
    private UUID bukuId;

    @Column(nullable = false)
    private String tanggalPinjam;

    @Column(nullable = false)
    private String tanggalKembali;

    @Column(nullable = false)
    private String status;
}