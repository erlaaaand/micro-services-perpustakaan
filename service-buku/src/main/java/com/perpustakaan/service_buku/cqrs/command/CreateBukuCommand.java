package com.perpustakaan.service_buku.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class CreateBukuCommand {
    private String nomorBuku;
    private String judul;
    private String penulis;
    private Integer tahunTerbit;
}