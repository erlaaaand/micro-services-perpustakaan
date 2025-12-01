package com.perpustakaan.service_peminjaman.dto;
import lombok.Data;

@Data
public class PeminjamanRequest {
    private Long anggotaId;
    private Long bukuId;
    private String tanggalPinjam;
    private String tanggalKembali;
    private String status;
}