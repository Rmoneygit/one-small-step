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

package com.google.sps.servlets;

import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns comment data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Query all comments from Datastore.
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Fill commentEntries arrayList with data from Datastore.
    ArrayList<String> commentEntries = new ArrayList<String>();
    for (Entity entity : results.asIterable()) {
      String commentName = (String) entity.getProperty("name");
      String commentText = (String) entity.getProperty("text");
      String commentEntry = commentName + ": \"" + commentText + "\"";
      commentEntries.add(commentEntry);
    }

    // Trim commentEntries arrayList to be under the max comment amount.
    int maxComments = Integer.parseInt(request.getParameter("comments"));
    if(commentEntries.size() > maxComments) {
      commentEntries.subList(maxComments, commentEntries.size()).clear();
    }

    // Convert to JSON and send it as the response.
    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(commentEntries));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Create a new Comment entity with data from the request.
    String commentText = request.getParameter("comment-input");
    String commentName = request.getParameter("name-input");
    long timestamp = System.currentTimeMillis();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("text", commentText);
    commentEntity.setProperty("name", commentName);
    commentEntity.setProperty("timestamp", timestamp);

    // Store the Comment entity in Datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    response.sendRedirect("/index.html");
  }
}
