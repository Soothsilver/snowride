module cz.hudecekpetr.snowride {
    requires javafx.controls;
    requires javafx.base;
    requires com.google.common;
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
    requires java.xml.bind;
    exports cz.hudecekpetr.snowride;

    opens cz.hudecekpetr.snowride.settings;
}