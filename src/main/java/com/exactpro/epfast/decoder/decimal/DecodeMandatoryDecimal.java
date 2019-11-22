package com.exactpro.epfast.decoder.decimal;

import com.exactpro.epfast.decoder.OverflowException;
import com.exactpro.epfast.decoder.integer.DecodeMandatoryInt32;
import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;

public final class DecodeMandatoryDecimal extends DecodeDecimal {

    private DecodeMandatoryInt32 exponentDecoder = new DecodeMandatoryInt32();

    private int exponent;

    public void decode(ByteBuf buf) {
        reset();
        exponentDecoder.decode(buf);
        if (exponentDecoder.isReady()) {
            exponentReady = true;
            try {
                exponent = exponentDecoder.getValue();
            } catch (OverflowException ex) {
                exponentOverflow = true;
            }
            if (buf.isReadable()) {
                mantissaDecoder.decode(buf);
                startedMantissa = true;
                if (mantissaDecoder.isReady()) {
                    ready = true;
                    try {
                        mantissa = mantissaDecoder.getValue();
                    } catch (OverflowException ex) {
                        mantissaOverflow = true;
                    }
                }
            }
        }
    }

    public void continueDecode(ByteBuf buf) {
        if (exponentReady && startedMantissa) {
            mantissaDecoder.continueDecode(buf);
            if (mantissaDecoder.isReady()) {
                ready = true;
                try {
                    mantissa = mantissaDecoder.getValue();
                } catch (OverflowException ex) {
                    mantissaOverflow = true;
                }
            }
        } else if (exponentReady) {
            mantissaDecoder.decode(buf);
            startedMantissa = true;
            if (mantissaDecoder.isReady()) {
                ready = true;
                try {
                    mantissa = mantissaDecoder.getValue();
                } catch (OverflowException ex) {
                    mantissaOverflow = true;
                }
            }
        } else {
            exponentDecoder.continueDecode(buf);
            if (exponentDecoder.isReady()) {
                exponentReady = true;
                try {
                    exponent = exponentDecoder.getValue();
                } catch (OverflowException ex) {
                    exponentOverflow = true;
                }
                if (buf.isReadable()) {
                    mantissaDecoder.decode(buf);
                    startedMantissa = true;
                    if (mantissaDecoder.isReady()) {
                        ready = true;
                        try {
                            mantissa = mantissaDecoder.getValue();
                        } catch (OverflowException ex) {
                            mantissaOverflow = true;
                        }
                    }
                }
            }
        }
    }

    public BigDecimal getValue() throws OverflowException {
        if (exponent >= -63 && exponent <= 63) {
            return new BigDecimal(mantissa).movePointRight(exponent);
        } else if (exponentOverflow) {
            throw new OverflowException("exponent value range is int32");
        } else if (mantissaOverflow) {
            throw new OverflowException("mantissa value range is int64");
        } else {
            throw new OverflowException("exponent value allowed range is -63 ... 63");
        }
    }
}