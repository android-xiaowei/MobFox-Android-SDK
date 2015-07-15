#!/usr/bin/env bash
CLASSES=build/intermediates/classes/release/
TEST_CLASSES=build/intermediates/classes/test/debug/
java -cp $CLASSES:testUtils:./testUtils/hamcrest-core-1.3.jar:./testUtils/junit-4.12.jar org.junit.runner.JUnitCore test.WaterfallTest
java -cp $CLASSES:testUtils:./testUtils/:./testUtils/hamcrest-core-1.3.jar:./testUtils/junit-4.12.jar:./testUtils/json.jar org.junit.runner.JUnitCore test.WaterfallManagerTest
java -cp $CLASSES:testUtils:./testUtils/:./testUtils/hamcrest-core-1.3.jar:./testUtils/junit-4.12.jar:./testUtils/json.jar:./libs/simple-xml-2.7.1.jar org.junit.runner.JUnitCore test.VastTest
java -cp $CLASSES:testUtils:./testUtils/:./testUtils/hamcrest-core-1.3.jar:./testUtils/junit-4.12.jar:./testUtils/json.jar:./libs/simple-xml-2.7.1.jar org.junit.runner.JUnitCore test.UserAgentTest
