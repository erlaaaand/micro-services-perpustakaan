package com.perpustakaan.service_buku.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBukuCommand {
    private Long id;
    private String kodeBuku;
    private String judul;
    // Diubah dari penulis -> pengarang agar konsisten
    private String pengarang;
    private String penerbit;
    private Integer tahunTerbit;
}