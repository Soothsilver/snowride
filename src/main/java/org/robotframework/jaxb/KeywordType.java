//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.11.16 at 04:23:27 PM CET 
//


package org.robotframework.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for KeywordType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="KeywordType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SETUP"/>
 *     &lt;enumeration value="TEARDOWN"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "KeywordType")
@XmlEnum
public enum KeywordType {

    SETUP,
    TEARDOWN,
    TEST_SETUP,
    TEST_TEARDOWN;

    public String value() {
        return name();
    }

    public static KeywordType fromValue(String v) {
        return valueOf(v);
    }

}
