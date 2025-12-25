package com.perpustakaan.service_buku.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateBukuCommand {
    // Diubah dari nomorBuku -> kodeBuku agar konsisten
    private String kodeBuku;
    private String judul;
    // Diubah dari penulis -> pengarang agar konsisten
    private String pengarang;
    private String penerbit;
    private Integer tahunTerbit;
}