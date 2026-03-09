package com.julien.lotterysystem.mapper.handler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EncryptTypeHandlerTest {

    @Test
    void shouldRoundTripPhoneNumberWithHexCipherText() {
        String phoneNumber = "13800138000";

        String cipherText = EncryptTypeHandler.encryptValue(phoneNumber);
        String plainText = EncryptTypeHandler.decryptValue(cipherText);

        assertEquals(phoneNumber, plainText);
    }
}