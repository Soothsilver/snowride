package org.robotframework.jaxb;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.List;

@XmlTransient
abstract public class OutputElement {

    public OutputElement parent;

    @XmlAttribute(name = "name")
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    abstract public List<OutputElement> getElements();

    public String getFullName() {
        return getFullName(null);
    }

    public String getFullName(OutputElement upto) {
        if (parent != null) {
            if (upto == parent) {
                return parent.name + "." + name;
            }
            return parent.getFullName() + "." + name;
        }
        return name;
    }
}
