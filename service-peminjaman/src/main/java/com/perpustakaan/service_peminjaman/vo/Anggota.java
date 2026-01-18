package com.perpustakaan.service_peminjaman.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Anggota {
    private String id;
    private String nomorAnggota;
    private String nama;
    private String alamat;
    private String email;
}