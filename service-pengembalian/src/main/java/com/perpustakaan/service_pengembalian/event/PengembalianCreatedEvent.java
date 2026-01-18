package com.perpustakaan.service_pengembalian.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PengembalianCreatedEvent implements Serializable {
    private UUID id;
    private UUID peminjamanId;
    private String tanggalDikembalikan;
    private int terlambat; // Tambahkan ini
    private double denda;
}