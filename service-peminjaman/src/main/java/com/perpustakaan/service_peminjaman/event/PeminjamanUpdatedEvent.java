package com.perpustakaan.service_peminjaman.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeminjamanUpdatedEvent implements Serializable {
    private Long id;
    private String status;
    private String tanggalKembali;
}