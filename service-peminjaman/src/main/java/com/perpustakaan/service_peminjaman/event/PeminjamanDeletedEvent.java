package com.perpustakaan.service_peminjaman.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeminjamanDeletedEvent implements Serializable {
    private Long id;
}