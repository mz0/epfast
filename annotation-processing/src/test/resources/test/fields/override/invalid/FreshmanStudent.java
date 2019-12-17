package test.fields.override.invalid;

import com.exactpro.epfast.annotations.FastField;
import com.exactpro.epfast.annotations.FastType;

import java.util.List;

@FastType
public class FreshmanStudent extends Student {

    private StringBuilder name;

    @FastField(name = "name")
    public void setName(StringBuilder name) {
        this.name = name;
    }

}