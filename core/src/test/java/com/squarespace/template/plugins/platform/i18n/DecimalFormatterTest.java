/**
 * Copyright (c) 2017 SQUAResPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIes OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.template.plugins.platform.i18n;

import static org.testng.Assert.fail;

import java.math.BigDecimal;
import java.util.Locale;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.DecimalNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.CodeException;
import com.squarespace.template.CodeMaker;
import com.squarespace.template.Context;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.Variables;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;


/**
 * Tests for the decimal formatter.
 *
 * NOTE: incomplete
 */
public class DecimalFormatterTest extends PlatformUnitTestBase {

  private static final String EN_US = "en-US";
  private static final String FR = "fr";
  private static final String DE = "de";
  private static final String ES = "es";

  private static final DecimalFormatter DECIMAL = new DecimalFormatter();
  private final CodeMaker mk = maker();
  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), DecimalFormatterTest.class);

  @Test
  public void testDecimal() {
    runner.run(
        "f-decimal-1.html",
        // f-decimal-2 defunct
        "f-decimal-3.html"
    );
  }

  @Test
  public void testArguments() {
    String args = " ";
    run(EN_US, "8915.34567", " style:decimal", "8,915.346");
    run(EN_US, "8915.34567", " style:percent", "891,535%");
    run(EN_US, "8915.34567", " style:permille", "8,915,346‰");
    run(EN_US, "8915.34567", " style:short", "8.9K");
    run(EN_US, "8915.34567", " style:long", "8.9 thousand");
    run(EN_US, "89.34567", args, "89.346");

    args += " minimumSignificantDigits:1 maxSig:6";
    run(EN_US, "89.34567", args, "89.3457");
  }

  @Test
  public void testRounding() {
    String args;

    args = " style:decimal round:truncate minFrac:2 maxFrac:2";
    run(EN_US, "1.2345", args, "1.23");
    run(EN_US, "1.5999", args, "1.59");

    args = " style:decimal round:truncate minFrac:2";
    run(EN_US, "1.2345", args, "1.234");
    run(EN_US, "1.5999", args, "1.599");

    args = " style:decimal round:floor minFrac:2";
    run(EN_US, "1.2345", args, "1.234");
    run(EN_US, "1.5999", args, "1.599");

    args = " style:decimal round:half-even minFrac:2";
    run(EN_US, "1.2345", args, "1.234");
    run(EN_US, "1.5999", args, "1.60");
  }

  @Test
  public void testStandard() {
    String args = " style:decimal grouping";
    run(EN_US, "0", args, "0");
    run(EN_US, "1", args, "1");
    run(EN_US, "3.59", args, "3.59");
    run(EN_US, "1200", args, "1,200");
    run(EN_US, "-15789.12", args, "-15,789.12");
    run(EN_US, "99999", args, "99,999");
    run(EN_US, "-100200300.40", args, "-100,200,300.4");
    run(EN_US, "10000000001", args, "10,000,000,001");

    run(ES, "0", args, "0");
    run(ES, "1", args, "1");
    run(ES, "3.59", args, "3,59");
    run(ES, "1200", args, "1200");
    run(ES, "-15789.12", args, "-15.789,12");
    run(ES, "99999", args, "99.999");
    run(ES, "-100200300.40", args, "-100.200.300,4");
    run(ES, "10000000001", args, "10.000.000.001");

    args = " style:decimal no-grouping";
    run(EN_US, "-15789.12", args, "-15789.12");
    run(EN_US, "99999", args, "99999");
    run(EN_US, "-100200300.40", args, "-100200300.4");
    run(EN_US, "10000000001", args, "10000000001");

    args = " style:decimal minFrac:4";
    run(EN_US, "-15789.12", args, "-15,789.1200");

    args = " style:decimal minInt:5 no-group";
    run(EN_US, "123.45", args, "00123.45");
  }

  @Test
  public void testFractions() {
    String args = " round:truncate maxFrac:3";
    run(EN_US, "3.14159", args, "3.141");

    args = " maxFrac:1 round:ceil";
    run(EN_US, "3.14159", args, "3.2");
  }

  @Test
  public void testShort() {
    String args = " style:short";
    run(EN_US, "0", args, "0");
    run(EN_US, "1", args, "1");
    run(EN_US, "3.59", args, "3.6");
    run(EN_US, "1200", args, "1.2K");
    run(EN_US, "-15789.12", args, "-16K");
    run(EN_US, "99999", args, "100K");
    run(EN_US, "-100200300.40", args, "-100M");
    run(EN_US, "10000000001", args, "10B");

    run(DE, "0", args, "0");
    run(DE, "1", args, "1");
    run(DE, "3.59", args, "3,6");
    run(DE, "1200", args, "1.200");
    run(DE, "-15789.12", args, "-15.789");
    run(DE, "99999", args, "99.999");
    run(DE, "-100200300.40", args, "-100 Mio.");
    run(DE, "10000000001", args, "10 Mrd.");

    args += " minSig:3 maxSig:4";
    run(EN_US, "0", args, "0.00");
    run(EN_US, "1", args, "1.00");
    run(EN_US, "3.59", args, "3.59");
    run(EN_US, "1200", args, "1.20K");
    run(EN_US, "-15789.12", args, "-15.79K");
    run(EN_US, "99999", args, "100K");
    run(EN_US, "-100200300.40", args, "-100.2M");
    run(EN_US, "10000000001", args, "10.0B");
  }

  @Test
  public void testLong() {
    String args = " style:long mode:significant";
    run(EN_US, "0", args, "0");
    run(EN_US, "1", args, "1");
    run(EN_US, "3.59", args, "3.6");
    run(EN_US, "1200", args, "1.2 thousand");
    run(EN_US, "2000", args, "2 thousand");
    run(EN_US, "-15789.12", args, "-16 thousand");
    run(EN_US, "99999", args, "100 thousand");
    run(EN_US, "-100200300.40", args, "-100 million");
    run(EN_US, "10000000001", args, "10 billion");

    run(FR, "0", args, "0");
    run(FR, "1", args, "1");
    run(FR, "3.59", args, "3,6");
    run(FR, "1200", args, "1,2 millier");
    run(FR, "2000", args, "2 mille");
    run(FR, "-15789.12", args, "-16 mille");
    run(FR, "99999", args, "100 mille");
    run(FR, "-100200300.40", args, "-100 millions");
    run(FR, "10000000001", args, "10 milliards");

    args += " minSig:3 maxSig:4";
    run(EN_US, "0", args, "0.00");
    run(EN_US, "1", args, "1.00");
    run(EN_US, "3.59", args, "3.59");
    run(EN_US, "1200", args, "1.20 thousand");
    run(EN_US, "2000", args, "2.00 thousand");
    run(EN_US, "-15789.12", args, "-15.79 thousand");
    run(EN_US, "99999", args, "100 thousand");
    run(EN_US, "-100200300.40", args, "-100.2 million");
    run(EN_US, "10000000001", args, "10.0 billion");

    run(FR, "0", args, "0,00");
    run(FR, "1", args, "1,00");
    run(FR, "3.59", args, "3,59");
    run(FR, "1200", args, "1,20 millier");
    run(FR, "2000", args, "2,00 mille");
    run(FR, "-15789.12", args, "-15,79 mille");
    run(FR, "99999", args, "100 mille");
    run(FR, "-100200300.40", args, "-100,2 millions");
    run(FR, "10000000001", args, "10,0 milliards");

    args = " style:long minSig:2";
    run(EN_US, "3.59", args, "3.6");
    run(EN_US, "3.519", args, "3.5");
    run(EN_US, "3.5999", args, "3.6");
  }

  private void run(String locale, String number, String args, String expected) {
    try {
      String json = new DecimalNode(new BigDecimal(number)).toString();
      String actual = format(locale, mk.args(args), json);
      Assert.assertEquals(actual, expected);
    } catch (CodeException e) {
      fail("formatter raised an error", e);
    }
  }

  private static String format(String locale,  Arguments args, String json) throws CodeException {
    Context ctx = new Context(JsonUtils.decode(json));
    ctx.javaLocale(Locale.forLanguageTag(locale));
    DECIMAL.validateArgs(args);
    Variables variables = new Variables("@", ctx.node());
    DECIMAL.apply(ctx, args, variables);
    return variables.first().node().asText();
  }

}
