package com.your.summoner;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {
  private static final String BASE_PACKAGE = "com.your.summoner";
  @Test void domainRemainsFrameworkIndependent() {
    ArchRule rule = noClasses().that().resideInAPackage("..domain..").should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "lombok..", "org.mapstruct..", "com.fasterxml..", "jakarta..", "..application..", "..infrastructure..").allowEmptyShould(true);
    rule.check(new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS).importPackages(BASE_PACKAGE));
  }
  @Test void applicationDoesNotDependOnInfrastructureOrSpringOutsideStereotypes() {
    ArchRule rule = noClasses().that().resideInAPackage("..application..").should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "org.springframework.beans..", "org.springframework.context..", "org.springframework.data..", "org.springframework.web..", "org.springframework.scheduling..", "org.springframework.cache..").allowEmptyShould(true);
    rule.check(new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS).importPackages(BASE_PACKAGE));
  }
}
