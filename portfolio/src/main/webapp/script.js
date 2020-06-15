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
  // Get the max amount of comments allowed.
  const maxComments = document.getElementById('comment-quantity').value;

  fetch('/data?comments=' + maxComments).then(response => response.json()).then((comments) => {
      // Build the list of comment entries.
      const commentEl = document.getElementById('comment-section');
      commentEl.innerHTML = '';
      comments.forEach((comment) => {
        // Add the comment entry's text.
        commentEl.appendChild(createListElement(comment.entry));
        // If there is an image attached to the comment, display it.
        if(comment.imageUrl != null) {
          var image = document.createElement('IMG');
          image.src = comment.imageUrl;
          commentEl.appendChild(image);
        }
      });
  });
}

/** Deletes all comments. */
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

/** Chooses what to display upon loading based on login status. */
function determineVisibleItems() {
  fetch('/login').then(response => response.json()).then(loginInfo => {
    const linkEl = document.getElementById('login-link');

    // If the user is logged in, unhide comment form.
    if(loginInfo.status) {
      document.getElementById('comment-form').classList.remove('hidden');
      linkEl.innerHTML = 'You are currently logged in with your Google account. Log out <a href=\"' + loginInfo.url + '\">here</a>.';
    }
    // If the user is not logged in, comment form remains hidden.
    else {
      linkEl.innerHTML = 'Login <a href=\"' + loginInfo.url + '\">here</a> to post a comment.';
    }
  });
}

/** Fetches and sets the upload url for images in the comment form. */
function fetchBlobstoreUrl() {
  fetch('/image-upload-url').then(response => response.text()).then(imageUploadUrl => {
    const commentForm = document.getElementById('comment-form');
    commentForm.action = imageUploadUrl;
  });
}
