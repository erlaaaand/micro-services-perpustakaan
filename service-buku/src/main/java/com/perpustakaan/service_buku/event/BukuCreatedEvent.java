// BukuCreatedEvent.java
package com.perpustakaan.service_buku.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BukuCreatedEvent implements Serializable {
    private UUID id;
    private String kodeBuku;
    private String judul;
    private String pengarang;
    private String penerbit;
    private Integer tahunTerbit;
}
