package com.perpustakaan.service_peminjaman.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePeminjamanCommand {
    private Long id;
    private Long anggotaId;
    private Long bukuId;
    private String tanggalPinjam;
    private String tanggalKembali;
    private String status;
}