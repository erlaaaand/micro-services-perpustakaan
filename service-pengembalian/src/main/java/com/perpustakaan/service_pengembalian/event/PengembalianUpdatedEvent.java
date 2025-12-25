package com.perpustakaan.service_pengembalian.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PengembalianUpdatedEvent implements Serializable {
    private Long id;
    private Long peminjamanId;
    private double denda;
}