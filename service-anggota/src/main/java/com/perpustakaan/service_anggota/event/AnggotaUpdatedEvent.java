// AnggotaUpdatedEvent.java
package com.perpustakaan.service_anggota.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnggotaUpdatedEvent implements Serializable {
    private Long id;
    private String nomorAnggota;
    private String nama;
    private String alamat;
    private String email;
}