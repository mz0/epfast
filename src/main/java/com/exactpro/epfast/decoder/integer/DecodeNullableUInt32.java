/*
 * Copyright 2019-2020 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exactpro.epfast.decoder.integer;

import com.exactpro.epfast.decoder.message.UnionRegister;
import io.netty.buffer.ByteBuf;

public final class DecodeNullableUInt32 extends DecodeInteger {

    private static final int POSITIVE_LIMIT = 0x02000000;

    private boolean isUInt32Limit;

    private int value;

    public int startDecode(ByteBuf buf, UnionRegister register) {
        reset();
        value = 0;
        isUInt32Limit = false;
        inProgress = true;
        int readerIndex = buf.readerIndex();
        int readLimit = buf.writerIndex();
        int oneByte = buf.getByte(readerIndex++);
        accumulate(oneByte);
        if (oneByte < 0) {
            setRegisterValue(register);
            buf.readerIndex(readerIndex);
            return 1;
        }
        if (readerIndex < readLimit) {
            checkOverlong(buf.getByte(readerIndex)); //check second byte
            do {
                accumulate(buf.getByte(readerIndex++));
            } while (!ready && readerIndex < readLimit);
        } else {
            checkForSignExtension = true;
        }
        buf.readerIndex(readerIndex);
        if (ready) {
            setRegisterValue(register);
            return 1;
        } else {
            return 0;
        }
    }

    public int continueDecode(ByteBuf buf, UnionRegister register) {
        int readerIndex = buf.readerIndex();
        int readLimit = buf.writerIndex();
        if (checkForSignExtension) {
            checkOverlong(buf.getByte(readerIndex)); //continue checking
            checkForSignExtension = false;
        }
        do {
            accumulate(buf.getByte(readerIndex++));
        } while (!ready && readerIndex < readLimit);
        buf.readerIndex(readerIndex);
        if (ready) {
            setRegisterValue(register);
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void setRegisterValue(UnionRegister register) {
        inProgress = false;
        if (overflow) {
            register.isOverflow = true;
            register.errorMessage = "UInt32 Overflow";
        } else if (value == 0) {
            register.isOverflow = false;
            register.isNull = true;
        } else {
            register.isOverflow = false;
            register.isNull = false;
            register.uInt32Value = isUInt32Limit ? 0x0_FFFFFFFFL : value - 1 & 0x0_FFFFFFFFL;
        }
    }

    private void accumulate(int oneByte) {
        if (oneByte < 0) { // if stop bit is set
            oneByte &= CLEAR_STOP_BIT_MASK;
            ready = true;
        }
        if (value < POSITIVE_LIMIT) {
            value = (value << 7) | oneByte;
        } else if (value == POSITIVE_LIMIT && oneByte == 0 && ready) {
            isUInt32Limit = true;
        } else {
            overflow = true;
        }
    }

    private void checkOverlong(int secondByte) {
        overlong = value == 0 && ((secondByte & SIGN_BIT_MASK) == 0);
    }
}
