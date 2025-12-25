package com.perpustakaan.service_pengembalian.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePengembalianCommand {
    private Long peminjamanId;
    private String tanggalDikembalikan;
    private int terlambat;
    private double denda;
}