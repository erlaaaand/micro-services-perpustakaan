package com.perpustakaan.service_pengembalian.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Peminjaman {
    private String id;
    private String anggotaId;
    private String bukuId;
    private String tanggalPinjam;
    private String tanggalKembali;
    private String status;
}