package br.com.memdeck;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "br.com.memdeck", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

	@ArchTest
	static final ArchRule domainIsFrameworkFree = classes()
			.that().resideInAPackage("..domain..")
			.should().onlyDependOnClassesThat().resideInAnyPackage("..domain..", "java..");

	@ArchTest
	static final ArchRule applicationDoesNotKnowTheOuterLayers = noClasses()
			.that().resideInAPackage("..application..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"..infrastructure..", "..presentation..",
					"jakarta.persistence..", "org.springframework.data..", "org.springframework.web..");

	@ArchTest
	static final ArchRule presentationDoesNotKnowInfrastructure = noClasses()
			.that().resideInAPackage("..presentation..")
			.should().dependOnClassesThat().resideInAPackage("..infrastructure..");

	@ArchTest
	static final ArchRule infrastructureDoesNotKnowPresentation = noClasses()
			.that().resideInAPackage("..infrastructure..")
			.should().dependOnClassesThat().resideInAPackage("..presentation..");
}
