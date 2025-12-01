package com.perpustakaan.service_buku.dto;
import lombok.Data;

@Data
public class BukuRequest {
    private String kodeBuku;
    private String judul;
    private String pengarang;
    private String penerbit;
    private int tahunTerbit;
}