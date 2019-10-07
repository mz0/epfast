package com.exactpro.epfast.decoder.decimal;

import com.exactpro.epfast.decoder.IDecodeContext;
import com.exactpro.epfast.decoder.OverflowException;
import com.exactpro.epfast.decoder.integer.DecodeMandatoryInt64;
import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;

public abstract class DecodeDecimal implements IDecodeContext {

    DecodeMandatoryInt64 mantissaDecoder = new DecodeMandatoryInt64();

    long mantissa;

    boolean exponentReady;

    protected boolean ready;

    boolean startedMantissa;

    boolean exponentOverflow;

    boolean mantissaOverflow;

    public abstract void decode(ByteBuf buf);

    public abstract void continueDecode(ByteBuf buf);

    public abstract BigDecimal getValue() throws OverflowException;

    public boolean isReady() {
        return ready;
    }
}
