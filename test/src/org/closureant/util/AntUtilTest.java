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

package org.closureant.util;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.tools.ant.types.FileSet;

import org.closureant.MockProject;

import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link org.closureant.util.AntUtil}.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
@RunWith(BlockJUnit4ClassRunner.class)
public final class AntUtilTest {

  @Test public void getListOfFilesFromAntFileSet() {
    FileSet fileSet = new FileSet();
    fileSet.setProject(MockProject.getProject());

    File file = new File("file.tmp");
    try {
      file.createNewFile();
      fileSet.setFile(file);
      fileSet.setDir(new File("."));
      
      List<File> files = AntUtil.getListOfFilesFromAntFileSet(
          MockProject.getProject(), fileSet);
      List<File> expectedFiles = Lists.newArrayList(new File("file.tmp"));
      
      assertEquals(expectedFiles.get(0).getAbsolutePath().toLowerCase(),
          files.get(0).getAbsolutePath().toLowerCase());
    } catch (IOException e) {
      throw Throwables.propagate(e);
    } finally {
      file.delete();
    }
  }

  @Test public void getFilePathsFromCollectionOfFileSet() {
    FileSet fileSet1 = new FileSet();
    FileSet fileSet2 = new FileSet();
    fileSet1.setProject(MockProject.getProject());
    fileSet2.setProject(MockProject.getProject());

    File file1 = new File("file1.tmp");
    File file2 = new File("file2.tmp");
    try {
      file1.createNewFile();
      file2.createNewFile();
      fileSet1.setFile(file1);
      fileSet1.setDir(new File("."));

      fileSet2.setFile(file2);
      fileSet2.setDir(new File("."));

      List<FileSet> fileSetList = ImmutableList.of(fileSet1, fileSet2);
      List<String> files = AntUtil.getFilePathsFromCollectionOfFileSet(
          MockProject.getProject(), fileSetList);
      List<String> expectedFiles = ImmutableList.of(file1.getAbsolutePath(),
          file2.getAbsolutePath());

      String expected = expectedFiles.toString();
      String actual = files.toString();
      assertEquals(expected.toLowerCase(), actual.toLowerCase());
    } catch (IOException e) {
      throw Throwables.propagate(e);
    } finally {
      file1.delete();
      file2.delete();
    }
  }
}