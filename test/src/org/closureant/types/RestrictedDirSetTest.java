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

package org.closureant.types;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.closureant.MockProject;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link RestrictedDirSet}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
@RunWith(BlockJUnit4ClassRunner.class)
public final class RestrictedDirSetTest {

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

  @Test public void defaultOnlyMatchRootDir() throws IOException {
    RestrictedDirSet restrictedDirSet = new RestrictedDirSet();

    File rootDir = new File(".").getCanonicalFile();
    restrictedDirSet.setDir(rootDir);

    List<File> directories = restrictedDirSet.getMatchedDirectories();
    List<File> expectedDirectories = Lists.newArrayList(
        new File(".").getCanonicalFile());

    assertEquals(expectedDirectories, directories);

  }

  @Test public void matchRecursiveAll() throws IOException {
    RestrictedDirSet restrictedDirSet = new RestrictedDirSet();
    restrictedDirSet.setProject(MockProject.getProject());

    File rootDir = new File(".").getCanonicalFile();
    restrictedDirSet.setDir(rootDir);
    restrictedDirSet.setIncludes("**");

    List<File> directories = restrictedDirSet.getMatchedDirectories();
    Set<File> dirSet = ImmutableSet.copyOf(directories);

    assertTrue(dirSet.size() > 1);
  }

  @Test public void matchRecursiveIncludesBaseDirs() throws IOException {
    RestrictedDirSet restrictedDirSet = new RestrictedDirSet();
    restrictedDirSet.setProject(MockProject.getProject());

    File rootDir = new File(".").getCanonicalFile();
    restrictedDirSet.setDir(rootDir);
    restrictedDirSet.setIncludes("**/baseDir*/**");

    List<File> directories = restrictedDirSet.getMatchedDirectories();
    Set<File> dirSet = ImmutableSet.copyOf(directories);

    assertEquals(dirSet.size(), 2);
    assertTrue(dirSet.contains(commonBaseDir1.getCanonicalFile()));
    assertTrue(dirSet.contains(commonBaseDir2.getCanonicalFile()));
  }

  @Test public void matchSubDirectories() throws IOException {
    RestrictedDirSet restrictedDirSet = new RestrictedDirSet();
    restrictedDirSet.setProject(MockProject.getProject());

    File rootDir = new File(".").getCanonicalFile();
    restrictedDirSet.setDir(rootDir);
    restrictedDirSet.setIncludes("**/sub*/**");

    List<File> directories = restrictedDirSet.getMatchedDirectories();
    Set<File> dirSet = ImmutableSet.copyOf(directories);

    assertEquals(dirSet.size(), 2);
    assertTrue(dirSet.contains(baseDir1Sub1.getCanonicalFile()));
    assertTrue(dirSet.contains(baseDir2Sub1.getCanonicalFile()));
  }
}
