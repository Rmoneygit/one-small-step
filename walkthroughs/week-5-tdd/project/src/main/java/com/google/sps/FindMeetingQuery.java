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

package com.google.sps;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;

public final class FindMeetingQuery {
  /*
  * Goal: Return a collection of free time ranges
  * Free time ranges: time ranges where no meeting attendees have an event scheduled,
  * and it is long enough to conduct the whole meeting.
  *
  **/
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> ranges = new ArrayList<TimeRange>();
    ArrayList<Event> eventList = new ArrayList<Event>(events);
    Collection<String> attendees = request.getAttendees();

    // Removes all events that are fully contained within another event.
    if(eventList.size() > 1) {
      for(int i = 1; i < eventList.size(); i++) {
        if(eventList.get(i - 1).getWhen().contains(eventList.get(i).getWhen())) {
          eventList.remove(i);
        }
      }
    }

    // Remove all events that have no attendees that are in the meeting we're trying to schedule.
    for(int i = 0; i < eventList.size(); i++) {
      ArrayList<String> eventAttendees = new ArrayList(eventList.get(i).getAttendees());
      boolean hasAtLeastOneAttendeeInCommon = false;

      for(String attendee: attendees) {
        if(eventAttendees.contains(attendee)) {
          hasAtLeastOneAttendeeInCommon = true;
        }
      }
      if(!hasAtLeastOneAttendeeInCommon) {
        eventList.remove(i);
      }
    }

    // If there are no events left, and the meeting is less than 24 hours long...
    if(eventList.isEmpty() && request.getDuration() < TimeRange.WHOLE_DAY.duration()) {
      // ...allow the whole day.
      ranges.add(TimeRange.WHOLE_DAY);
    }

    // Go through all events and populate "ranges" with periods of free time that have no meetings for any attendees.
    for(int i = 0; i < eventList.size(); i++) {
      int start;
      int end;

      // Looking at the first event.
      if(i == 0) {
        start = TimeRange.START_OF_DAY;
        end = eventList.get(i).getWhen().start();
        if(end - start >= request.getDuration()) {
          ranges.add(TimeRange.fromStartEnd(start, end, false));
        }
      }

      // Looking at any event in between
      if(i > 0 && i < eventList.size() - 1) {
        start = eventList.get(i - 1).getWhen().end();
        end = eventList.get(i + 1).getWhen().start();

        if(end - start >= request.getDuration()) {
          ranges.add(TimeRange.fromStartEnd(start, end, false));
        }
      }

      // Looking at the last event.
      if(i == eventList.size() - 1) {
        if(i != 0) {
          start = eventList.get(i - 1).getWhen().end();
          end = eventList.get(i).getWhen().start();
          if(end - start >= request.getDuration()) {
            ranges.add(TimeRange.fromStartEnd(start, end, false));
          }
        }

        start = eventList.get(i).getWhen().end();
        end = TimeRange.END_OF_DAY;
        if(end - start >= request.getDuration()) {
          ranges.add(TimeRange.fromStartEnd(start, end, true));
        }
      }
    } 
    return ranges;
    }
  }

