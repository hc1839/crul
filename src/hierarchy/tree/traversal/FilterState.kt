/*
 *  Copyright Han Chen
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

package crul.hierarchy.tree.traversal

import crul.hierarchy.tree.Node

/**
 *  Acceptance of a subtree indicated by a node filter.
 *
 *  A filter determines the visibility of a node and its children.
 */
enum class FilterState {
    /**
     *  Node is accepted and visible, and its children are considered.
     */
    ACCEPT_SUBTREE,

    /**
     *  Node is accepted and visible, and its children are not considered.
     */
    ACCEPT_SELF_ONLY,

    /**
     *  Node is rejected and not visible, and its children are not considered.
     */
    REJECT_SUBTREE
}
