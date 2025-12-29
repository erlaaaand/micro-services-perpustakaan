package com.perpustakaan.service_peminjaman.vo;

import com.perpustakaan.service_peminjaman.entity.query.PeminjamanReadModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseTemplateVO {
    private PeminjamanReadModel peminjaman; // Menggunakan Read Model
    private Anggota anggota;
    private Buku buku;
}