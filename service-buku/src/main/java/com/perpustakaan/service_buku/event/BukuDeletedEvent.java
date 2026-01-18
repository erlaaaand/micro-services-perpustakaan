// BukuDeletedEvent.java
package com.perpustakaan.service_buku.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BukuDeletedEvent implements Serializable {
    private UUID id;
}