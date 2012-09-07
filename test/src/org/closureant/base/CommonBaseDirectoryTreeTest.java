/*
 * Copyright (C) 2012 Christopher Peisert. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.closureant.base;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link JsClosureSourceFile}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
@RunWith(BlockJUnit4ClassRunner.class)
public final class CommonBaseDirectoryTreeTest {

  private static File commonBaseDir1;
  private static File commonBaseDir2;
  private static File baseDir1Sub1;
  private static File baseDir1Sub1Sub2;
  private static File baseDir1Sub1Sub2Sub3;
  private static File baseDir2Sub1;
  private static File baseDir2Sub1Sub2;

  private static List<File> directories;

  @BeforeClass
  public static void suiteSetup() throws IOException {
    commonBaseDir1 = new File("./baseDir1");
    commonBaseDir2 = new File("./baseDir2");
    baseDir1Sub1 = new File(commonBaseDir1, "sub1");
    baseDir1Sub1Sub2 = new File(baseDir1Sub1, "sub2");
    baseDir1Sub1Sub2Sub3 = new File(baseDir1Sub1Sub2, "sub3");
    baseDir2Sub1 = new File(commonBaseDir2, "sub1");
    baseDir2Sub1Sub2 = new File(baseDir2Sub1, "sub2");

    baseDir1Sub1Sub2Sub3.mkdirs();
    baseDir2Sub1Sub2.mkdirs();

    directories = Lists.newArrayList();
    directories.add(commonBaseDir1);
    directories.add(commonBaseDir2);
    directories.add(baseDir1Sub1);
    directories.add(baseDir1Sub1Sub2);
    directories.add(baseDir1Sub1Sub2Sub3);
    directories.add(baseDir2Sub1);
    directories.add(baseDir2Sub1Sub2);
  }

  @AfterClass
  public static void suiteTearDown() {
    baseDir2Sub1Sub2.delete();
    baseDir2Sub1.delete();
    commonBaseDir2.delete();

    baseDir1Sub1Sub2Sub3.delete();
    baseDir1Sub1Sub2.delete();
    baseDir1Sub1.delete();
    commonBaseDir1.delete();
  }

  @Test public void getCommonBaseDirectoryOfEmptyList() throws IOException {
    List<File> dirs = Lists.newArrayList();
    CommonBaseDirectoryTree tree = new CommonBaseDirectoryTree(dirs);
    List<File> actual = tree.getCommonBaseDirectories();
    List<File> expected = ImmutableList.of();

    assertEquals(expected, actual);
  }

  @Test public void getCommonBaseDirectoryOneBranch() throws IOException {
    List<File> dirs = Lists.newArrayList(commonBaseDir1, baseDir1Sub1Sub2Sub3);
    CommonBaseDirectoryTree tree = new CommonBaseDirectoryTree(dirs);
    List<File> actual = tree.getCommonBaseDirectories();
    List<File> expected = ImmutableList.of(commonBaseDir1.getCanonicalFile());

    assertEquals(expected, actual);
  }

  @Test public void getCommonBaseDirectoriesTwoBranches() throws IOException {
    CommonBaseDirectoryTree tree = new CommonBaseDirectoryTree(directories);
    List<File> actual = tree.getCommonBaseDirectories();
    List<File> expected = ImmutableList.of(commonBaseDir2.getCanonicalFile(),
        commonBaseDir1.getCanonicalFile());

    assertEquals(expected, actual);
  }
}
