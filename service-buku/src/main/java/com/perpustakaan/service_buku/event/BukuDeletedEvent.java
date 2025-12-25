// BukuDeletedEvent.java
package com.perpustakaan.service_buku.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BukuDeletedEvent implements Serializable {
    private Long id;
}