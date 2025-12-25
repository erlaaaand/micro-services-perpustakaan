package com.perpustakaan.service_buku.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBukuCommand {
    private Long id;
    private String nomorAnggota;
    private String nama;
    private String alamat;
    private String email;
}