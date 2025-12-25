// BukuUpdatedEvent.java
package com.perpustakaan.service_buku.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BukuUpdatedEvent implements Serializable {
    private Long id;
    private String kodeBuku;
    private String judul;
    private String pengarang;
    private String penerbit;
    private Integer tahunTerbit;
}