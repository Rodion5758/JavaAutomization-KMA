package org.example.validation.suite;

import org.example.validation.ValidationEngineTest;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Fast validation tests")
@SelectClasses(ValidationEngineTest.class)
@IncludeTags("fast")
public class FastSuite {}
