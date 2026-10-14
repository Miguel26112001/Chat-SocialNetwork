package com.my.enterprise.chat_backend.identity.infrastructure.adapters;

import com.my.enterprise.chat_backend.identity.application.outboundservices.ports.Clock;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Adaptador del puerto {@code Clock}: reloj del sistema.
 * En tests se sustituye por un reloj fijo.
 */
@Component
public class SystemClock implements Clock {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
