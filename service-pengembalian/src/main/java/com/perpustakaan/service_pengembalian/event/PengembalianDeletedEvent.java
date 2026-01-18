package com.perpustakaan.service_pengembalian.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PengembalianDeletedEvent implements Serializable {
    private UUID id;
}