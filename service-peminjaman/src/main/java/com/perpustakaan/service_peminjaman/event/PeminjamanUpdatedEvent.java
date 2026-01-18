package com.perpustakaan.service_peminjaman.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeminjamanUpdatedEvent implements Serializable {
    private UUID id;
    private String status;
    private String tanggalKembali;
}