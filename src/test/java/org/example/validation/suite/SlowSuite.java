package org.example.validation.suite;

import org.example.validation.ValidationEngineTest;
import org.junit.platform.suite.api.*;

@Suite
@SuiteDisplayName("Slow validation tests")
@SelectClasses(ValidationEngineTest.class)
@IncludeTags("slow")
public class SlowSuite {}
