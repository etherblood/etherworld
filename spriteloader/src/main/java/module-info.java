module com.etherblood.etherworld.spriteloader {
    exports com.etherblood.etherworld.spriteloader;
    exports com.etherblood.etherworld.spriteloader.aseprite;
    opens com.etherblood.etherworld.spriteloader.aseprite;
    requires com.fasterxml.jackson.annotation;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
}