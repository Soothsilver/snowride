module cz.hudecekpetr.snowride {
    requires javafx.controls;
    requires javafx.base;
    requires org.apache.commons.lang3;
    requires org.controlsfx.controls;
    requires org.apache.commons.io;
    requires flowless;
    requires javafx.web;
    requires java.datatransfer;
    requires java.desktop;
    requires xstream;
    requires org.fxmisc.richtext;
    requires zt.process.killer;
    requires jsoniter;
    requires org.antlr.antlr4.runtime;
    requires jakarta.xml.bind;
    requires kotlin.stdlib;
    requires org.fxmisc.undo;
    requires wellbehavedfx;
    exports cz.hudecekpetr.snowride;

    opens cz.hudecekpetr.snowride.settings;
    opens org.robotframework.jaxb to jakarta.xml.bind;
}