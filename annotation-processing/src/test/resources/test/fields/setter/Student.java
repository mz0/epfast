package test.fields.setter;

import com.exactpro.epfast.annotations.FastField;
import com.exactpro.epfast.annotations.FastType;

import java.util.List;

@FastType
public class Student {

    private String name;

    @FastField(name = "name")
    public void set(String name) {
        this.name = name;
    }

}