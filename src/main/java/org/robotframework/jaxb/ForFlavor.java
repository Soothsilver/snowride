//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.11.16 at 04:23:27 PM CET 
//


package org.robotframework.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ForFlavor.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ForFlavor">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="IN"/>
 *     &lt;enumeration value="IN RANGE"/>
 *     &lt;enumeration value="IN ENUMERATE"/>
 *     &lt;enumeration value="IN ZIP"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ForFlavor")
@XmlEnum
public enum ForFlavor {

    IN("IN"),
    @XmlEnumValue("IN RANGE")
    IN_RANGE("IN RANGE"),
    @XmlEnumValue("IN ENUMERATE")
    IN_ENUMERATE("IN ENUMERATE"),
    @XmlEnumValue("IN ZIP")
    IN_ZIP("IN ZIP");
    private final String value;

    ForFlavor(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ForFlavor fromValue(String v) {
        for (ForFlavor c: ForFlavor.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
