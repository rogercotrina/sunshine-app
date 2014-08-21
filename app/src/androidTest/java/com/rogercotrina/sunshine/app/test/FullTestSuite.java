package com.rogercotrina.sunshine.app.test;

import android.test.suitebuilder.TestSuiteBuilder;
import junit.framework.Test;

/**
 * Created by rogercotrina on 7/31/14.
 */
public class FullTestSuite {

    public static Test suite() {
        return new TestSuiteBuilder(FullTestSuite.class).includeAllPackagesUnderHere().build();
    }

    public FullTestSuite() {
        super();
    }
}
