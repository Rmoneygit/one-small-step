// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

/** Class containing comment data. */
public final class Comment {

  private final String entry;
  private final String imageUrl;

  public Comment(String entry, String imageUrl) {
    this.entry = entry;
    this.imageUrl = imageUrl;
  }

  public String getEntry() {
    return entry;
  }

  public String getImageUrl() {
    return imageUrl;
  }
}
