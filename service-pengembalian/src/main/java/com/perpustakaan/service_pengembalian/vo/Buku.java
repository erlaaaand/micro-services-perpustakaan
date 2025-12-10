package com.perpustakaan.service_pengembalian.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Buku {
    private Long id;
    private String kodeBuku;
    private String judul;
    private String pengarang;
    private String penerbit;
    private int tahunTerbit;
}