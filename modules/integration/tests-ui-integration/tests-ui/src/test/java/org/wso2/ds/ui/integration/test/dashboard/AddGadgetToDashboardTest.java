/*
 * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.ds.ui.integration.test.dashboard;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.ds.ui.integration.util.DSUIIntegrationTest;
import org.wso2.ds.ui.integration.util.DSWebDriver;

import javax.xml.xpath.XPathExpressionException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

/**
 * Tests the dashboard related functionality such as adding and removing blocks and gadgets, gadget maximization
 * and toggle fluid layout
 */
public class AddGadgetToDashboardTest extends DSUIIntegrationTest {
    private static final String DASHBOARD_TITLE = "sampledashboard1";

    @Factory(dataProvider = "userMode")
    public AddGadgetToDashboardTest(TestUserMode userMode, String dashboardTitle) {
        super(userMode);
    }

    @DataProvider(name = "userMode")
    public static Object[][] userModeProvider() {
        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN, DASHBOARD_TITLE}};
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        login(getCurrentUsername(), getCurrentPassword());
        addDashBoard(DASHBOARD_TITLE, "This is a test dashboard");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        try {
            logout();
        } finally {
            getDriver().quit();
        }
    }


    @Test(groups = "wso2.ds.dashboard", description = "Adding gadgets to an existing dashboard from dashboard server")
    public void testAddGadgetToDashboard() throws Exception {
        DSWebDriver driver = getDriver();
        String[][] gadgetMappings = {{"g1", "b"}, {"usa-map", "c"}};
        String script = generateAddGadgetScript(gadgetMappings);

        driver.get(getBaseUrl() + "/portal/dashboards");
        driver.findElement(By.cssSelector("#" + DASHBOARD_TITLE + " a.ues-edit")).click();

        driver.findElement(By.cssSelector("i.fw.fw-pie-chart")).click();
        driver.executeScript(script);
        // TODO: change the behaviour in the dashboard to reflect the change after saving the change. Then remove sleep
        Thread.sleep(1000);
        driver.findElement(By.cssSelector("a.ues-dashboard-preview")).click();
        pushWindow();
        // verify the existence of gadgets in the view dashboard mode
        boolean gadgetsAvailable = true;
        for (String[] mapping : gadgetMappings) {
            List<WebElement> elements = driver.findElements(By.cssSelector("div#" + mapping[1] + ".ues-component-box " +
                    ".ues-component"));
            if (elements.size() == 0) {
                gadgetsAvailable = false;
            }
        }
        assertTrue(gadgetsAvailable, "The gadget(s) not found");
        driver.close();
        popWindow();
    }

    @Test(groups = "wso2.ds.dashboard", description = "maximizing gadget which added to dashboard", dependsOnMethods
            = "testAddGadgetToDashboard")
    public void testMaximizeGadgetinView() throws Exception {
        DSWebDriver driver = getDriver();
        //TODO need to test for gadget maximization fot gadget after fix in edit/personalize mode
        driver.findElement(By.cssSelector("a.ues-dashboard-preview")).click();
        String parentWindow = driver.getWindowHandle();
        for (String childWindow : driver.getWindowHandles()) {
            if (!childWindow.contains(parentWindow)) {
                driver.switchTo().window(childWindow);
                Object txt = driver.executeScript("var iframe = $(\"iFrame[title='USA Map']\")[0];\n" +
                        "var innerDoc = iframe.contentDocument || iframe.contentWindow.document;\n" +
                        "return innerDoc.getElementById(\"defaultViewLabel\").textContent;");
                assertEquals("USA MAP (This is default view)", txt.toString());
                WebElement panel = driver.findElement(By.id("c"));
                panel.findElement(By.cssSelector("i.fw.fw-laptop")).click();
                //maximized Window view
                Object txtMax = driver.executeScript("var iframe = $(\"iFrame[title='USA Map']\")[0];\n" +
                        "var innerDoc = iframe.contentDocument || iframe.contentWindow.document;\n" +
                        "return innerDoc.getElementById(\"fullViewLabel\").textContent;");
                assertEquals("USA MAP(this is full screen view)", txtMax.toString());
                break;
            }
        }
        driver.close();
        driver.switchTo().window(parentWindow);

    }
}