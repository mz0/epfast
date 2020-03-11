package com.exactpro.epfast.template;

import java.util.List;

public interface Sequence extends FieldInstruction {

    Reference getTypeRef();

    LengthField getLength();

    List<? extends Instruction> getInstructions();

}