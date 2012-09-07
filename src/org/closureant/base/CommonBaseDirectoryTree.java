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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A tree data structure that ensures that given an initial set of directories,
 * only the common base-directories will be represented as branches in the
 * tree.
 *
 * <p>More formally, let <b>S</b> be the initial set of directories used to
 * build tree <b>T</b> and let <b>D</b> be some directory in <b>S</b>. There
 * exists a branch <b>B</b> in <b>T</b> corresponding to directory <b>D</b> if
 * and only if there does not exist some directory <b>D2</b> in <b>S</b> such
 * that <b>D</b> is a subdirectory of <b>D2</b>.
 *
 * <p>Example:</p>
 *
 * <p><b>Set of initial directory paths:</b>
 * <ul>
 * <li>/closure-library/closure/goog/</li>
 * <li>/closure-library/closure/goog/array/</li>
 * <li>/closure-library/closure/goog/dom/</li>
 * <li>/closure-library/closure/goog/events/</li>
 * <li>/home/bob/project/</li>
 * <li>/home/bob/project/css/</li>
 * <li>/home/bob/project/html/</li>
 * <li>/home/bob/project/js/</li>
 * </ul>
 * </p>
 *
 * <p><b>Branches in tree:</b>
 * <ul>
 * <li>[tree root] - [/] - [closure-library] - [closure] - [goog]</li>
 * <li>[tree root] - [/] - [home] - [bob] - [project]</li>
 * </ul>
 * </p>
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class CommonBaseDirectoryTree {

  private final DirectoryNode root;

  /**
   * Constructs a {@link CommonBaseDirectoryTree} containing the common base
   * directories from the set of initial directories.
   *
   * @param directories the initial directories from which to find common base
   *     directories
   * @throws IOException if the canonical path for a directory cannot be
   *     obtained
   */
  public CommonBaseDirectoryTree(Collection<File> directories)
      throws IOException {
    this.root = new DirectoryNode("root", null);

    for (File dir : directories) {
      addDirectoryToTree(dir, this.root);
    }
  }

  /**
   * Gets the common base-directories of the initial collection of directories
   * passed to the constructor.
   *
   * @return the common base directories
   * @throws IOException if the canonical path for a directory cannot be
   *     obtained
   */
  public List<File> getCommonBaseDirectories() throws IOException {
    List<File> baseDirectories = Lists.newArrayList();

    for (DirectoryNode child : this.root.getChildren()) {
      File currentPath = new File(child.getName());
      getCommonBaseDirectoryPaths(child, currentPath, baseDirectories);
    }

    return baseDirectories;
  }

  /**
   * Recursively descend tree branches to build the common base directory
   * paths.
   *
   * @param currentNode the directory node being traversed
   * @param currentPath the current director path for the branch level being
   *     traversed
   * @param baseDirectories list of common base directories being generated
   * @throws IOException if the canonical path for a directory cannot be
   *     obtained
   */
  private void getCommonBaseDirectoryPaths(DirectoryNode currentNode,
      File currentPath, List<File> baseDirectories) throws IOException {

    if (currentNode.isLeafNode()) {
      baseDirectories.add(currentPath.getCanonicalFile());
      return;
    }

    for (DirectoryNode child : currentNode.getChildren()) {
      File childPath = new File(currentPath, child.getName());
      getCommonBaseDirectoryPaths(child, childPath, baseDirectories);
    }
  }

  /**
   * Adds a directory to the tree. If the tree already contains a directory
   * that is a base directory of {@code directory}, then the tree is not
   * modified.
   *
   * @param directory the directory to add to the tree
   * @param root the root note of the tree
   * @throws IOException if the canonical path for directory cannot be
   *     obtained
   */
  private void addDirectoryToTree(File directory, DirectoryNode root)
      throws IOException {
    List<String> dirNames = getListOfDirectoryNames(directory);
    Iterator<String> dirNameIterator = dirNames.iterator();

    addNextDirAsChildNode(dirNameIterator, root);
  }

  /**
   * Adds the next directory returned by the specified directory path-name
   * iterator as a child of {@code currentNode}. If {@code currentNode}
   * already has a child node with the next directory name, then recursively
   * call this method on the child node. However, if the child node is not a
   * leaf node and there are no more subdirectories in the directory path-name
   * iterator, then prune the tree at the child node, since the directory being
   * added is a base directory of the current branch.
   *
   * @param dirNameIterator iterator of directory names comprising a path
   * @param currentNode the directory node whose children are being compared
   *     against the next directory returned by the directory name iterator
   */
  private void addNextDirAsChildNode(Iterator<String> dirNameIterator,
      DirectoryNode currentNode) {
    if (dirNameIterator.hasNext()) {
      String dirName = dirNameIterator.next();
      DirectoryNode childNode = currentNode.getChild(dirName);
      if (childNode == null) {
        // Add a new branch to the tree.
        currentNode.addChild(new DirectoryNode(dirName, currentNode));
        addBranchToTree(dirNameIterator, currentNode.getChild(dirName));
      } else {
        if (!childNode.isLeafNode()) {
          if (dirNameIterator.hasNext()) {
            addNextDirAsChildNode(dirNameIterator, childNode);
          } else {
            // Since the current directory being added is a base directory of
            // of this branch, prune the branch at this point.
            childNode.deleteChildren();
          }
        } else {
          // Since the childNode is a leaf node, the current branch is a
          // base-directory of the directory path being added. Nothing more to
          // do.
        }
      }
    } else {
      // End of directory path being added.
    }
  }

  /**
   * Starting at the specified node, recursively add child nodes for each
   * directory name returned by the directory name iterator.
   *
   * @param dirNameIterator iterator of directory names comprising a path
   * @param currentNode the directory node to which to add a new child node
   */
  private void addBranchToTree(Iterator<String> dirNameIterator,
      DirectoryNode currentNode) {
    if (dirNameIterator.hasNext()) {
      String dirName = dirNameIterator.next();
      currentNode.addChild(new DirectoryNode(dirName, currentNode));
      addBranchToTree(dirNameIterator, currentNode.getChild(dirName));
    }
  }

  /**
   * Splits the canonical file path of a directory into a list of directory
   * names starting with the root of the file system.
   *
   * @param directory the directory to split into directory names
   * @return a list of directory names starting with the root of file system
   * @throws IOException if the canonical path for directory cannot be
   *     obtained
   */
  private List<String> getListOfDirectoryNames(File directory)
      throws IOException {
    File canonicalDir = directory.getCanonicalFile();
    List<String> directoryNames = Lists.newLinkedList();

    if (canonicalDir.isDirectory()) {
      directoryNames.add(canonicalDir.getName());
    }
    canonicalDir = canonicalDir.getParentFile();

    while (canonicalDir != null) {
      directoryNames.add(0, canonicalDir.getName());
      canonicalDir = canonicalDir.getParentFile();
    }

    return directoryNames;
  }


  //----------------------------------------------------------------------------


  /**
   * A node representing a file system directory.
   */
  private static class DirectoryNode {

    private final Map<String, DirectoryNode> children;
    private final String name;
    private final DirectoryNode parent;


    /**
     * Constructs a {@link DirectoryNode} representing a file system directory.
     * The directory name is the relative name, not the full path.
     *
     * @param directoryName the name of the directory represented by this
     *     node. This should be the relative directory name, not the path.
     * @throws NullPointerException if {@code directoryName} is {@code null}
     */
    public DirectoryNode(String directoryName, DirectoryNode parent) {
      Preconditions.checkNotNull(directoryName, "directoryName is null");
      this.children = Maps.newHashMap();
      this.name = directoryName;
      this.parent = parent;
    }

    /**
     * Adds a child directory node (that is, a subdirectory).
     *
     * @param child a directory node representing a subdirectory
     */
    public void addChild(DirectoryNode child) {
      if (!this.children.containsKey(child.getName())) {
        this.children.put(child.getName(), child);
      }
    }

    /**
     * Deletes all child notes of this directory node.
     */
    public void deleteChildren() {
      this.children.clear();
    }

    /**
     * Gets the child node (subdirectory) with the specified director name. If
     * no such child node exists, returns {@code null}.
     *
     * @param directoryName the directory name to find amongst the children
     * @return the child node corresponding to the specified directory name or
     *     {@code null} if not found
     */
    public DirectoryNode getChild(String directoryName) {
      if (this.children.containsKey(directoryName)) {
        return this.children.get(directoryName);
      } else {
        return null;
      }
    }

    /**
     * Gets an immutable list of child nodes.
     *
     * @return an immutable list of child nodes
     */
    public List<DirectoryNode> getChildren() {
      return ImmutableList.copyOf(this.children.values());
    }

    /**
     * Gets the directory name.
     *
     * @return the directory name
     */
    public String getName() {
      return this.name;
    }

    /**
     * Gets the parent directory node.
     *
     * @return the parent directory node
     */
    public DirectoryNode getParent() {
      return this.parent;
    }

    /**
     * Determines if a node is a child of this directory node.
     *
     * @param node a directory node
     * @return {@code true} if node is a child of this directory node
     */
    public boolean hasChild(DirectoryNode node) {
      return this.children.containsKey(node.getName());
    }

    /**
     * Determines if a directory is a child of this directory node.
     *
     * @param directoryName the directory to check
     * @return {@code true} if the specified directory name is a child of this
     *     directory node
     */
    public boolean hasChild(String directoryName) {
      return this.children.containsKey(directoryName);
    }

    /**
     * Whether this directory node a leaf node (that is, has no children).
     *
     * @return {@code true} if this directory node is a leaf node
     */
    public boolean isLeafNode() {
      return this.children.size() == 0;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      DirectoryNode that = (DirectoryNode) o;

      if (!children.equals(that.children)) {
        return false;
      }
      if (!name.equals(that.name)) {
        return false;
      }
      if (parent != null ? !parent.equals(that.parent) : that.parent != null) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = children.hashCode();
      result = 31 * result + name.hashCode();
      result = 31 * result + (parent != null ? parent.hashCode() : 0);
      return result;
    }
  }
}
