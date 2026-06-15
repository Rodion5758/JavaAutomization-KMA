package org.example.validation.suite;

import org.example.validation.ValidationEngineTest;
import org.junit.platform.suite.api.*;

@Suite
@SuiteDisplayName("Fast validation tests")
@SelectClasses(ValidationEngineTest.class)
@IncludeTags("fast")
public class FastSuite {}
