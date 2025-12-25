package com.perpustakaan.service_anggota.service;

import com.perpustakaan.service_anggota.dto.AnggotaRequest;
import com.perpustakaan.service_anggota.entity.Anggota;
import com.perpustakaan.service_anggota.repository.AnggotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnggotaServiceTest {

    @Mock
    private AnggotaRepository anggotaRepository;

    @InjectMocks
    private AnggotaService anggotaService;

    private AnggotaRequest validRequest;
    private Anggota validAnggota;

    @BeforeEach
    void setUp() {
        validRequest = new AnggotaRequest();
        validRequest.setNomorAnggota("A001");
        validRequest.setNama("John Doe");
        validRequest.setAlamat("Jl. Test No. 123");
        validRequest.setEmail("john@test.com");

        validAnggota = new Anggota();
        validAnggota.setId(1L);
        validAnggota.setNomorAnggota("A001");
        validAnggota.setNama("John Doe");
        validAnggota.setAlamat("Jl. Test No. 123");
        validAnggota.setEmail("john@test.com");
    }

    @Test
    @DisplayName("Should save anggota successfully")
    void testSaveAnggota_Success() {
        when(anggotaRepository.findByNomorAnggota(anyString())).thenReturn(null);
        when(anggotaRepository.save(any(Anggota.class))).thenReturn(validAnggota);

        Anggota result = anggotaService.saveAnggota(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNomorAnggota()).isEqualTo("A001");
        assertThat(result.getNama()).isEqualTo("John Doe");
        assertThat(result.getAlamat()).isEqualTo("Jl. Test No. 123");
        assertThat(result.getEmail()).isEqualTo("john@test.com");

        verify(anggotaRepository, times(1)).findByNomorAnggota("A001");
        verify(anggotaRepository, times(1)).save(any(Anggota.class));
    }

    @Test
    @DisplayName("Should throw exception when nomor anggota already exists")
    void testSaveAnggota_DuplicateNomorAnggota() {
        when(anggotaRepository.findByNomorAnggota(anyString())).thenReturn(validAnggota);

        assertThatThrownBy(() -> anggotaService.saveAnggota(validRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Nomor anggota sudah digunakan")
            .hasMessageContaining("A001");

        verify(anggotaRepository, times(1)).findByNomorAnggota("A001");
        verify(anggotaRepository, never()).save(any(Anggota.class));
    }

    @Test
    @DisplayName("Should get anggota by id successfully")
    void testGetAnggotaById_Success() {
        when(anggotaRepository.findById(1L)).thenReturn(Optional.of(validAnggota));

        Anggota result = anggotaService.getAnggotaById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNama()).isEqualTo("John Doe");
        assertThat(result.getNomorAnggota()).isEqualTo("A001");

        verify(anggotaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return null when anggota not found by id")
    void testGetAnggotaById_NotFound() {
        when(anggotaRepository.findById(999L)).thenReturn(Optional.empty());

        Anggota result = anggotaService.getAnggotaById(999L);

        assertThat(result).isNull();
        verify(anggotaRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should get all anggota successfully")
    void testGetAllAnggota_Success() {
        Anggota anggota2 = new Anggota();
        anggota2.setId(2L);
        anggota2.setNomorAnggota("A002");
        anggota2.setNama("Jane Doe");
        anggota2.setAlamat("Jl. Test No. 456");
        anggota2.setEmail("jane@test.com");

        List<Anggota> anggotaList = Arrays.asList(validAnggota, anggota2);
        when(anggotaRepository.findAll()).thenReturn(anggotaList);

        List<Anggota> result = anggotaService.getAllAnggota();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNama()).isEqualTo("John Doe");
        assertThat(result.get(1).getNama()).isEqualTo("Jane Doe");

        verify(anggotaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no anggota exists")
    void testGetAllAnggota_EmptyList() {
        when(anggotaRepository.findAll()).thenReturn(Collections.emptyList());

        List<Anggota> result = anggotaService.getAllAnggota();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(anggotaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update anggota successfully")
    void testUpdateAnggota_Success() {
        AnggotaRequest updateRequest = new AnggotaRequest();
        updateRequest.setNomorAnggota("A001-UPD");
        updateRequest.setNama("John Updated");
        updateRequest.setAlamat("Jl. Updated");
        updateRequest.setEmail("updated@test.com");

        Anggota updatedAnggota = new Anggota();
        updatedAnggota.setId(1L);
        updatedAnggota.setNomorAnggota("A001-UPD");
        updatedAnggota.setNama("John Updated");
        updatedAnggota.setAlamat("Jl. Updated");
        updatedAnggota.setEmail("updated@test.com");

        when(anggotaRepository.findById(1L)).thenReturn(Optional.of(validAnggota));
        when(anggotaRepository.findByNomorAnggota("A001-UPD")).thenReturn(null);
        when(anggotaRepository.save(any(Anggota.class))).thenReturn(updatedAnggota);

        Anggota result = anggotaService.updateAnggota(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getNomorAnggota()).isEqualTo("A001-UPD");
        assertThat(result.getNama()).isEqualTo("John Updated");
        assertThat(result.getAlamat()).isEqualTo("Jl. Updated");
        assertThat(result.getEmail()).isEqualTo("updated@test.com");

        verify(anggotaRepository, times(1)).findById(1L);
        verify(anggotaRepository, times(1)).findByNomorAnggota("A001-UPD");
        verify(anggotaRepository, times(1)).save(any(Anggota.class));
    }

    @Test
    @DisplayName("Should return null when updating non-existent anggota")
    void testUpdateAnggota_NotFound() {
        when(anggotaRepository.findById(999L)).thenReturn(Optional.empty());

        Anggota result = anggotaService.updateAnggota(999L, validRequest);

        assertThat(result).isNull();
        verify(anggotaRepository, times(1)).findById(999L);
        verify(anggotaRepository, never()).save(any(Anggota.class));
    }

    @Test
    @DisplayName("Should throw exception when updating with duplicate nomor anggota")
    void testUpdateAnggota_DuplicateNomorAnggota() {
        Anggota conflictAnggota = new Anggota();
        conflictAnggota.setId(2L);
        conflictAnggota.setNomorAnggota("A002");
        conflictAnggota.setNama("Conflict User");

        AnggotaRequest updateRequest = new AnggotaRequest();
        updateRequest.setNomorAnggota("A002");
        updateRequest.setNama("John Updated");
        updateRequest.setAlamat("Jl. Updated");
        updateRequest.setEmail("updated@test.com");

        when(anggotaRepository.findById(1L)).thenReturn(Optional.of(validAnggota));
        when(anggotaRepository.findByNomorAnggota("A002")).thenReturn(conflictAnggota);

        assertThatThrownBy(() -> anggotaService.updateAnggota(1L, updateRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Nomor anggota sudah digunakan")
            .hasMessageContaining("A002");

        verify(anggotaRepository, times(1)).findById(1L);
        verify(anggotaRepository, times(1)).findByNomorAnggota("A002");
        verify(anggotaRepository, never()).save(any(Anggota.class));
    }

    @Test
    @DisplayName("Should delete anggota successfully")
    void testDeleteAnggota_Success() {
        when(anggotaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(anggotaRepository).deleteById(1L);

        boolean result = anggotaService.deleteAnggota(1L);

        assertThat(result).isTrue();
        verify(anggotaRepository, times(1)).existsById(1L);
        verify(anggotaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent anggota")
    void testDeleteAnggota_NotFound() {
        when(anggotaRepository.existsById(999L)).thenReturn(false);

        boolean result = anggotaService.deleteAnggota(999L);

        assertThat(result).isFalse();
        verify(anggotaRepository, times(1)).existsById(999L);
        verify(anggotaRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should handle same nomor anggota when updating without change")
    void testUpdateAnggota_SameNomorAnggota() {
        when(anggotaRepository.findById(1L)).thenReturn(Optional.of(validAnggota));
        when(anggotaRepository.save(any(Anggota.class))).thenReturn(validAnggota);

        Anggota result = anggotaService.updateAnggota(1L, validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getNomorAnggota()).isEqualTo("A001");
        
        verify(anggotaRepository, times(1)).findById(1L);
        verify(anggotaRepository, never()).findByNomorAnggota(anyString());
        verify(anggotaRepository, times(1)).save(any(Anggota.class));
    }

    @Test
    @DisplayName("Should correctly map request to entity when saving")
    void testSaveAnggota_CorrectMapping() {
        when(anggotaRepository.findByNomorAnggota(anyString())).thenReturn(null);
        when(anggotaRepository.save(any(Anggota.class))).thenReturn(validAnggota);

        ArgumentCaptor<Anggota> anggotaCaptor = ArgumentCaptor.forClass(Anggota.class);

        anggotaService.saveAnggota(validRequest);

        verify(anggotaRepository).save(anggotaCaptor.capture());
        Anggota capturedAnggota = anggotaCaptor.getValue();

        assertThat(capturedAnggota.getNomorAnggota()).isEqualTo(validRequest.getNomorAnggota());
        assertThat(capturedAnggota.getNama()).isEqualTo(validRequest.getNama());
        assertThat(capturedAnggota.getAlamat()).isEqualTo(validRequest.getAlamat());
        assertThat(capturedAnggota.getEmail()).isEqualTo(validRequest.getEmail());
    }

    @Test
    @DisplayName("Should correctly update all fields")
    void testUpdateAnggota_AllFieldsUpdated() {
        AnggotaRequest updateRequest = new AnggotaRequest();
        updateRequest.setNomorAnggota("A999");
        updateRequest.setNama("Completely New");
        updateRequest.setAlamat("New Address");
        updateRequest.setEmail("new@test.com");

        when(anggotaRepository.findById(1L)).thenReturn(Optional.of(validAnggota));
        when(anggotaRepository.findByNomorAnggota("A999")).thenReturn(null);
        when(anggotaRepository.save(any(Anggota.class))).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<Anggota> anggotaCaptor = ArgumentCaptor.forClass(Anggota.class);

        anggotaService.updateAnggota(1L, updateRequest);

        verify(anggotaRepository).save(anggotaCaptor.capture());
        Anggota capturedAnggota = anggotaCaptor.getValue();

        assertThat(capturedAnggota.getId()).isEqualTo(1L);
        assertThat(capturedAnggota.getNomorAnggota()).isEqualTo("A999");
        assertThat(capturedAnggota.getNama()).isEqualTo("Completely New");
        assertThat(capturedAnggota.getAlamat()).isEqualTo("New Address");
        assertThat(capturedAnggota.getEmail()).isEqualTo("new@test.com");
    }
}