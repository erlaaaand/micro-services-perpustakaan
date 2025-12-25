package com.perpustakaan.service_pengembalian.cqrs.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAllPengembalianQuery {
    private Integer page = 0;
    private Integer size = 10;
}