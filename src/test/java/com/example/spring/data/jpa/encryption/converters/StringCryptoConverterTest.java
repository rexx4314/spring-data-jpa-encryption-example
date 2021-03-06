package com.example.spring.data.jpa.encryption.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.AdditionalAnswers.returnsSecondArg;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class StringCryptoConverterTest {

    private static final String STRING_TO_CIPHER = "ma_chaine_a_chiffrer";
    private static final String STRING_TO_DECIPHER = "bWFfY2hhaW5lX2FfY2hpZmZyZXI=";

    private StringCryptoConverter stringCryptoConverter;

    private StringCryptoConverter spiedStringCryptoConverter;

    @Mock
    private CipherInitializer cipherInitializer;

    @BeforeEach
    void setUp() throws Exception {
        stringCryptoConverter = new StringCryptoConverter(cipherInitializer);

        spiedStringCryptoConverter = spy(stringCryptoConverter);
        doAnswer(returnsSecondArg()).when(spiedStringCryptoConverter).callCipherDoFinal(any(), any());

        KeyProperty.DATABASE_ENCRYPTION_KEY = "MySuperSecretKey";
    }

    @Nested
    class ConvertToDatabaseColumnShould {

        @Test
        void return_null_string_when_string_to_encrypt_is_null() {
            // Given
            String attribute = null;

            // When
            String result = stringCryptoConverter.convertToDatabaseColumn(attribute);

            // Then
            assertThat(result).isNull();
        }

        @Test
        void return_empty_string_when_string_to_encrypt_is_empty() {
            // Given
            String attribute = "";

            // When
            String result = stringCryptoConverter.convertToDatabaseColumn(attribute);

            // Then
            assertThat(result).isEqualTo(attribute);
        }

        @Test
        void return_encrypted_string_as_base_64() throws Exception {
            // Given
            Cipher cipher = mock(Cipher.class);
            when(cipherInitializer.prepareAndInitCipher(Cipher.ENCRYPT_MODE, KeyProperty.DATABASE_ENCRYPTION_KEY)).thenReturn(cipher);

            // When
            String result = spiedStringCryptoConverter.convertToDatabaseColumn(STRING_TO_CIPHER);

            // Then
            verify(spiedStringCryptoConverter).callCipherDoFinal(cipher, STRING_TO_CIPHER.getBytes());
            assertThat(result).isEqualTo(STRING_TO_DECIPHER);
        }

        @Test
        void return_unchanged_string_when_database_encryption_key_is_null() {
            // Given
            KeyProperty.DATABASE_ENCRYPTION_KEY = null;

            // When
            String result = spiedStringCryptoConverter.convertToDatabaseColumn(STRING_TO_CIPHER);

            // Then
            assertThat(result).isEqualTo(STRING_TO_CIPHER);
        }

        @Test
        void return_unchanged_string_when_database_encryption_key_is_empty() {
            // Given
            KeyProperty.DATABASE_ENCRYPTION_KEY = "";

            // When
            String result = spiedStringCryptoConverter.convertToDatabaseColumn(STRING_TO_CIPHER);

            // Then
            assertThat(result).isEqualTo(STRING_TO_CIPHER);
        }

        @Test
        void rethrow_exception_when_cipher_initialization_fails_with_InvalidKeyException() throws Exception {
            // Given
            InvalidKeyException invalidKeyException = new InvalidKeyException();
            when(cipherInitializer.prepareAndInitCipher(anyInt(), anyString())).thenThrow(invalidKeyException);

            // When
            Throwable throwable = catchThrowable(() -> spiedStringCryptoConverter.convertToDatabaseColumn(STRING_TO_CIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(invalidKeyException);
        }

        @Test
        void rethrow_exception_when_cipher_initialization_fails_with_NoSuchAlgorithmException() throws Exception {
            // Given
            NoSuchAlgorithmException noSuchAlgorithmException = new NoSuchAlgorithmException();
            when(cipherInitializer.prepareAndInitCipher(anyInt(), anyString())).thenThrow(noSuchAlgorithmException);

            // When
            Throwable throwable = catchThrowable(() -> spiedStringCryptoConverter.convertToDatabaseColumn(STRING_TO_CIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(noSuchAlgorithmException);
        }

        @Test
        void rethrow_exception_when_cipher_initialization_fails_with_NoSuchPaddingException() throws Exception {
            // Given
            NoSuchPaddingException noSuchPaddingException = new NoSuchPaddingException();
            when(cipherInitializer.prepareAndInitCipher(anyInt(), anyString())).thenThrow(noSuchPaddingException);

            // When
            Throwable throwable = catchThrowable(() -> spiedStringCryptoConverter.convertToDatabaseColumn(STRING_TO_CIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(noSuchPaddingException);
        }

        @Test
        void rethrow_exception_when_cipher_initialization_fails_with_InvalidAlgorithmParameterException() throws Exception {
            // Given
            InvalidAlgorithmParameterException invalidAlgorithmParameterException = new InvalidAlgorithmParameterException();
            when(cipherInitializer.prepareAndInitCipher(anyInt(), anyString())).thenThrow(invalidAlgorithmParameterException);

            // When
            Throwable throwable = catchThrowable(() -> spiedStringCryptoConverter.convertToDatabaseColumn(STRING_TO_CIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(invalidAlgorithmParameterException);
        }

        @Test
        void rethrow_exception_when_encryption_fails_with_BadPaddingException() throws Exception {
            // Given
            BadPaddingException badPaddingException = new BadPaddingException();
            when(spiedStringCryptoConverter.callCipherDoFinal(any(), any())).thenThrow(badPaddingException);

            // When
            Throwable throwable = catchThrowable(() -> spiedStringCryptoConverter.convertToDatabaseColumn(STRING_TO_CIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(badPaddingException);
        }

        @Test
        void rethrow_exception_when_encryption_fails_with_IllegalBlockSizeException() throws Exception {
            // Given
            IllegalBlockSizeException illegalBlockSizeException = new IllegalBlockSizeException();
            when(spiedStringCryptoConverter.callCipherDoFinal(any(), any())).thenThrow(illegalBlockSizeException);

            // When
            Throwable throwable = catchThrowable(() -> spiedStringCryptoConverter.convertToDatabaseColumn(STRING_TO_CIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(illegalBlockSizeException);
        }
    }

    @Nested
    class ConvertToEntityAttributeShould {

        @Test
        void return_null_string_when_string_to_decrypt_is_null() {
            // Given
            String dbData = null;

            // When
            String result = stringCryptoConverter.convertToEntityAttribute(dbData);

            // Then
            assertThat(result).isNull();
        }

        @Test
        void return_empty_string_when_string_to_decrypt_is_empty() {
            // Given
            String dbData = "";

            // When
            String result = stringCryptoConverter.convertToEntityAttribute(dbData);

            // Then
            assertThat(result).isEqualTo(dbData);
        }

        @Test
        void return_decrypted_string() throws Exception {
            // Given
            Cipher cipher = mock(Cipher.class);
            when(cipherInitializer.prepareAndInitCipher(Cipher.DECRYPT_MODE, KeyProperty.DATABASE_ENCRYPTION_KEY)).thenReturn(cipher);

            // When
            String result = spiedStringCryptoConverter.convertToEntityAttribute(STRING_TO_DECIPHER);

            // Then
            verify(spiedStringCryptoConverter).callCipherDoFinal(cipher, STRING_TO_CIPHER.getBytes());
            assertThat(result).isEqualTo(STRING_TO_CIPHER);
        }

        @Test
        void return_unchanged_string_when_database_encryption_key_is_null() {
            // Given
            KeyProperty.DATABASE_ENCRYPTION_KEY = null;

            // When
            String result = spiedStringCryptoConverter.convertToEntityAttribute(STRING_TO_DECIPHER);

            // Then
            assertThat(result).isEqualTo(STRING_TO_DECIPHER);
        }

        @Test
        void return_unchanged_string_when_database_encryption_key_is_empty() {
            // Given
            KeyProperty.DATABASE_ENCRYPTION_KEY = "";

            // When
            String result = spiedStringCryptoConverter.convertToEntityAttribute(STRING_TO_DECIPHER);

            // Then
            assertThat(result).isEqualTo(STRING_TO_DECIPHER);
        }

        @Test
        void rethrow_exception_when_cipher_initialization_fails_with_InvalidKeyException() throws Exception {
            // Given
            InvalidKeyException invalidKeyException = new InvalidKeyException();
            when(cipherInitializer.prepareAndInitCipher(anyInt(), anyString())).thenThrow(invalidKeyException);

            // When
            Throwable throwable = catchThrowable(() -> spiedStringCryptoConverter.convertToEntityAttribute(STRING_TO_DECIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(invalidKeyException);
        }

        @Test
        void rethrow_exception_when_cipher_initialization_fails_with_NoSuchAlgorithmException() throws Exception {
            // Given
            NoSuchAlgorithmException noSuchAlgorithmException = new NoSuchAlgorithmException();
            when(cipherInitializer.prepareAndInitCipher(anyInt(), anyString())).thenThrow(noSuchAlgorithmException);

            // When
            Throwable throwable = catchThrowable(() -> spiedStringCryptoConverter.convertToEntityAttribute(STRING_TO_DECIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(noSuchAlgorithmException);
        }

        @Test
        void rethrow_exception_when_cipher_initialization_fails_with_NoSuchPaddingException() throws Exception {
            // Given
            NoSuchPaddingException noSuchPaddingException = new NoSuchPaddingException();
            when(cipherInitializer.prepareAndInitCipher(anyInt(), anyString())).thenThrow(noSuchPaddingException);

            // When
            Throwable throwable = catchThrowable(() -> spiedStringCryptoConverter.convertToEntityAttribute(STRING_TO_DECIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(noSuchPaddingException);
        }

        @Test
        void rethrow_exception_when_cipher_initialization_fails_with_InvalidAlgorithmParameterException() throws Exception {
            // Given
            InvalidAlgorithmParameterException invalidAlgorithmParameterException = new InvalidAlgorithmParameterException();
            when(cipherInitializer.prepareAndInitCipher(anyInt(), anyString())).thenThrow(invalidAlgorithmParameterException);

            // When
            Throwable throwable = catchThrowable(() -> spiedStringCryptoConverter.convertToEntityAttribute(STRING_TO_DECIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(invalidAlgorithmParameterException);
        }

        @Test
        void rethrow_exception_when_decryption_fails_with_BadPaddingException() throws Exception {
            // Given
            BadPaddingException badPaddingException = new BadPaddingException();
            when(spiedStringCryptoConverter.callCipherDoFinal(any(), any())).thenThrow(badPaddingException);

            // When
            Throwable throwable = catchThrowable(() -> spiedStringCryptoConverter.convertToEntityAttribute(STRING_TO_DECIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(badPaddingException);
        }

        @Test
        void rethrow_exception_when_decryption_fails_with_IllegalBlockSizeException() throws Exception {
            // Given
            IllegalBlockSizeException illegalBlockSizeException = new IllegalBlockSizeException();
            when(spiedStringCryptoConverter.callCipherDoFinal(any(), any())).thenThrow(illegalBlockSizeException);

            // When
            Throwable throwable = catchThrowable(() -> spiedStringCryptoConverter.convertToEntityAttribute(STRING_TO_DECIPHER));

            // Then
            assertThat(throwable).isInstanceOf(RuntimeException.class).hasCause(illegalBlockSizeException);
        }
    }
}
