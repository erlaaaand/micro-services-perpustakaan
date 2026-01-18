// AnggotaDeletedEvent.java
package com.perpustakaan.service_anggota.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnggotaDeletedEvent implements Serializable {
    private UUID id;
}