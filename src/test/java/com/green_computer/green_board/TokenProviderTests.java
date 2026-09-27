package com.green_computer.green_board;

import com.green_computer.green_board.global.TokenProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenProviderTests {
    private final TokenProvider tokens = new TokenProvider("qa-only-secret-key-at-least-32-bytes-long");

    @Test
    void distinguishesAccessAndRefreshTokens() {
        String access = tokens.generateAccessToken("qa-user");
        String refresh = tokens.generateRefreshToken("qa-user");

        assertTrue(tokens.validateAccessToken(access));
        assertFalse(tokens.validateRefreshToken(access));
        assertTrue(tokens.validateRefreshToken(refresh));
        assertFalse(tokens.validateAccessToken(refresh));
        assertEquals("qa-user", tokens.getUsernameFromToken(access));
    }

    @Test
    void createsDifferentTokensEvenForTheSameUser() {
        assertNotEquals(tokens.generateAccessToken("qa-user"), tokens.generateAccessToken("qa-user"));
        assertNotEquals(tokens.generateRefreshToken("qa-user"), tokens.generateRefreshToken("qa-user"));
    }

    @Test
    void rejectsMalformedTokens() {
        assertFalse(tokens.validateAccessToken("invalid"));
        assertFalse(tokens.validateRefreshToken("invalid"));
    }
}
