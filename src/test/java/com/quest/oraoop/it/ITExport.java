/**
 *   Copyright 2012 Quest Software, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.quest.oraoop.it;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.quest.oraoop.OraOopTestCase;

public class ITExport extends OraOopTestCase {
  
  private static final ITExport testCase = new ITExport();

  @BeforeClass
  public static void setUpHdfsData() throws Exception {
    //Copy the TST_PRODUCT table into HDFS which can be used for the export tests
    testCase.setSqoopTargetDirectory(testCase.getSqoopTargetDirectory() + "tst_product");
    testCase.createTable("table_tst_product.xml");

    int retCode = testCase.runImport("tst_product", testCase.getSqoopConf(), false);
    Assert.assertEquals("Return code should be 0", 0,retCode);
  }

  @Test
  public void testProductExport() throws Exception {
    int retCode = testCase.runExportFromTemplateTable("tst_product", "tst_product_exp");
    Assert.assertEquals("Return code should be 0", 0,retCode);
  }

  @Test
  public void testProductExportMixedCaseTableName() throws Exception {
    int retCode = testCase.runExportFromTemplateTable("tst_product", "\"\"T5+_Pr#duct_Exp\"\"");
    Assert.assertEquals("Return code should be 0", 0,retCode);
  }

  @AfterClass
  public static void cleanUpHdfsData() throws Exception {
    testCase.cleanupFolders();
    testCase.closeTestEnvConnection();
  }

}