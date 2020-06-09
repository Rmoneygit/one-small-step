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

/**
 * Fetches JSON message from DataServlet.
 */
function getComments() {
  determineVisibleItems()

  // Get the max amount of comments allowed
  const maxComments = document.getElementById('comment-quantity').value;

  fetch('/data?comments=' + maxComments).then(response => response.json()).then((comments) => {
      // Build the list of comment entries.
      const commentEl = document.getElementById('comment-section');
      commentEl.innerHTML = '';
      comments.forEach((comment) => {
        commentEl.appendChild(createListElement(comment));
      });
  });
}

/** Deletes all comments */
function deleteComments() {
  const request = new Request('/delete-data', {method: 'POST'});
  fetch(request).then(getComments());
}

/** Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

/** Chooses what to display based on login status */
function determineVisibleItems() {
  fetch('/login').then(response => response.json()).then(loginInfo => {
    const linkEl = document.getElementById('login-link');

    // If the user is logged in
    if(loginInfo.status) {
      // Unhide comments
      document.getElementById('hidden').style.display = 'block';
      linkEl.innerHTML = 'You are currently logged in with your Google account. Log out <a href=\"' + loginInfo.url + '\">here</a>.';
    }
    // If the user is not logged in
    else {
      linkEl.innerHTML = 'Login <a href=\"' + loginInfo.url + '\">here</a> to post a comment.';
    }
  });
}
