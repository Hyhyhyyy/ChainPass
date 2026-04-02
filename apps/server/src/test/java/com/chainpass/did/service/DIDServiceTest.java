package com.chainpass.did.service;

import com.chainpass.did.entity.DIDDocument;
import com.chainpass.did.mapper.DIDMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DID服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class DIDServiceTest {

    @Mock
    private DIDMapper didMapper;

    @InjectMocks
    private DIDService didService;

    private Long testUserId;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
    }

    @Test
    @DisplayName("创建DID - 成功")
    void testCreateDID_Success() {
        // Given
        when(didMapper.findByUserId(testUserId)).thenReturn(null);
        when(didMapper.insert(any())).thenReturn(1);

        // When
        DIDDocument document = didService.createDID(testUserId);

        // Then
        assertNotNull(document);
        assertTrue(document.getId().startsWith("did:chainpass:"));
        assertNotNull(document.getVerificationMethod());
        assertEquals(1, document.getVerificationMethod().size());
        assertNotNull(document.getAuthentication());

        verify(didMapper, times(1)).insert(any());
    }

    @Test
    @DisplayName("创建DID - 用户已存在DID")
    void testCreateDID_AlreadyExists() {
        // Given
        com.chainpass.did.entity.DIDRecord existingRecord = new com.chainpass.did.entity.DIDRecord();
        existingRecord.setDid("did:chainpass:existing");
        when(didMapper.findByUserId(testUserId)).thenReturn(existingRecord);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            didService.createDID(testUserId);
        });

        verify(didMapper, never()).insert(any());
    }

    @Test
    @DisplayName("验证DID有效性 - DID不存在")
    void testIsValidDID_NotExists() {
        // Given
        String did = "did:chainpass:nonexistent";
        when(didMapper.countValidDID(did)).thenReturn(0);

        // When
        boolean valid = didService.isValidDID(did);

        // Then
        assertFalse(valid);
    }

    @Test
    @DisplayName("验证DID有效性 - DID有效")
    void testIsValidDID_Valid() {
        // Given
        String did = "did:chainpass:valid";
        when(didMapper.countValidDID(did)).thenReturn(1);

        // When
        boolean valid = didService.isValidDID(did);

        // Then
        assertTrue(valid);
    }
}