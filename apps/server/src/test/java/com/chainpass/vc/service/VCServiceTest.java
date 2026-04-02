package com.chainpass.vc.service;

import com.chainpass.did.service.DIDService;
import com.chainpass.vc.dto.VCDto;
import com.chainpass.vc.entity.VCRecord;
import com.chainpass.vc.entity.VCType;
import com.chainpass.vc.mapper.VCRecordMapper;
import com.chainpass.vc.mapper.VCTypeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * VC服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class VCServiceTest {

    @Mock
    private VCRecordMapper vcRecordMapper;

    @Mock
    private VCTypeMapper vcTypeMapper;

    @Mock
    private DIDService didService;

    @InjectMocks
    private VCService vcService;

    private VCDto.IssueVCRequest issueRequest;
    private VCType vcType;

    @BeforeEach
    void setUp() {
        issueRequest = new VCDto.IssueVCRequest();
        issueRequest.setHolderDid("did:chainpass:test");
        issueRequest.setVcType("KYCCredential");

        vcType = new VCType();
        vcType.setTypeCode("KYCCredential");
        vcType.setTypeName("KYC认证凭证");
        vcType.setValidityDays(730);
        vcType.setStatus(0);
    }

    @Test
    @DisplayName("签发凭证 - 成功")
    void testIssueCredential_Success() {
        // Given
        when(didService.isValidDID(issueRequest.getHolderDid())).thenReturn(true);
        when(vcTypeMapper.findByTypeCode("KYCCredential")).thenReturn(vcType);
        when(vcRecordMapper.insert(any())).thenReturn(1);

        // When
        var credential = vcService.issueCredential(issueRequest);

        // Then
        assertNotNull(credential);
        assertTrue(credential.getId().startsWith("urn:uuid:"));
        assertTrue(credential.getType().contains("VerifiableCredential"));
        assertTrue(credential.getType().contains("KYCCredential"));
        assertNotNull(credential.getProof());

        verify(vcRecordMapper, times(1)).insert(any());
    }

    @Test
    @DisplayName("验证凭证 - 凭证不存在")
    void testVerifyCredential_NotFound() {
        // Given
        String vcId = "urn:uuid:nonexistent";
        when(vcRecordMapper.findByVcId(vcId)).thenReturn(null);

        // When
        VCDto.VerifyResult result = vcService.verifyCredential(vcId);

        // Then
        assertFalse(result.isValid());
        assertEquals("凭证不存在", result.getMessage());
    }

    @Test
    @DisplayName("验证凭证 - 凭证已吊销")
    void testVerifyCredential_Revoked() {
        // Given
        String vcId = "urn:uuid:revoked";
        VCRecord record = new VCRecord();
        record.setVcId(vcId);
        record.setStatus(2); // 已吊销
        record.setExpiresAt(Instant.now().plusSeconds(3600));
        when(vcRecordMapper.findByVcId(vcId)).thenReturn(record);

        // When
        VCDto.VerifyResult result = vcService.verifyCredential(vcId);

        // Then
        assertFalse(result.isValid());
        assertEquals("凭证已被吊销", result.getMessage());
    }

    @Test
    @DisplayName("获取凭证类型列表")
    void testGetAllVCTypes() {
        // Given
        when(vcTypeMapper.findAllEnabled()).thenReturn(List.of(vcType));

        // When
        List<VCType> types = vcService.getAllVCTypes();

        // Then
        assertNotNull(types);
        assertEquals(1, types.size());
        assertEquals("KYCCredential", types.get(0).getTypeCode());
    }
}