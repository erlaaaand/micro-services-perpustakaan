package com.perpustakaan.service_anggota.dto;

import lombok.Data;

@Data
public class AnggotaRequest {
    private String nomorAnggota;
    private String nama;
    private String alamat;
    private String email;
}