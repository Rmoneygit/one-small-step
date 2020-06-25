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
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

public final class FindMeetingQuery {
  /**
    * Purpose: To return a collection of free time ranges
    * Free time ranges are time ranges where no meeting attendees have an event scheduled,
    * and it is long enough to conduct the whole meeting.
    */
  public Collection<TimeRange> query(Collection<Event> eventCollection, MeetingRequest request) {
    ArrayList<TimeRange> ranges = new ArrayList<TimeRange>();
    ArrayList<Event> events = new ArrayList<Event>(eventCollection);


    // If the meeting duration is longer than a day, return no ranges.
    if(request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return ranges;
    }

    // If no events exists, return the whole day as the range.
    if(events.isEmpty()) {
      ranges.add(TimeRange.WHOLE_DAY);
      return ranges;
    }

    // Determine if there is at least one attendee for any event.
    boolean attendeesExist = false;
    for (Event event:events) {
      if (!event.getAttendees().isEmpty()) {
        attendeesExist = true;
        break;
      }
    }

    // If no event has any attendees, return the whole day as the range.
    if (!attendeesExist) {
      ranges.add(TimeRange.WHOLE_DAY);
      return ranges;
    }

    /** Sort the events in chronological order.
      * This is required for the following algorithms to work properly.
      */
    Collections.sort(events, Event.ORDER_BY_START);

    // Trim the event list
    ArrayList<Event> trimmedEvents = trimEvents(events, request, request.getAttendees());

    // Now consider ALL attendees, mandatory and optional.
    ArrayList<String> allAttendees = new ArrayList<String>(request.getAttendees());
    allAttendees.addAll(request.getOptionalAttendees());
    ArrayList<Event> allTrimmedEvents = trimEvents(events, request, allAttendees);

    // If allTrimmedEvents is empty, it means that no events have any attendees in common with the meeting.
    if(allTrimmedEvents.isEmpty()) {
      ranges.add(TimeRange.WHOLE_DAY);
      return ranges;
    }

    // Return the ranges of time during which the meeting can take place.

    ranges = getRanges(allTrimmedEvents, request);
    /** If no meeting times are possible when considering optional attendees,
      * return meeting times for only mandatory attendees.
      */
    if(ranges.isEmpty()) {
      ranges = getRanges(trimmedEvents, request);
    }
    return ranges;
  }

  /**
   * Provide available meeting time ranges.
   */
  public ArrayList<TimeRange> getRanges(ArrayList<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> ranges = new ArrayList<TimeRange>();
    int start = 0;
    int end = 0;
    int duration = (int) request.getDuration();

    for(int i = 0; i < events.size(); i++) {
      // Case 1: The first event.
      if (i == 0) {
        start = TimeRange.START_OF_DAY;
        end = events.get(i).getWhen().start();
        if(start < end && start + duration <= end) {
          ranges.add(TimeRange.fromStartEnd(start, end, false));
        }
      }
      // Case 2: An event in the middle.
      else {
        start = events.get(i - 1).getWhen().end();
        end = events.get(i).getWhen().start();
        if(start < end && start + duration <= end) {
          ranges.add(TimeRange.fromStartEnd(start, end, false));
        }
      }
      // Case 3: The last event.
      if (i == events.size() - 1) {
        start = events.get(i).getWhen().end();
        end = TimeRange.END_OF_DAY;
        if(start < end && start + duration <= end) {
          ranges.add(TimeRange.fromStartEnd(start, end, true));
        }
      }
    }
    return ranges;
  }

  // Create a new list with only the events we should consider.
  public ArrayList<Event> trimEvents(ArrayList<Event> events, MeetingRequest request, Collection<String> attendeesCollection) {
    ArrayList<Event> trimmedEvents = new ArrayList<Event>();
    HashSet meetingAttendees = new HashSet(attendeesCollection);

    for(int i = 0; i < events.size(); i++) {
      Iterator<String> it = events.get(i).getAttendees().iterator();
      boolean hasOneCommonAttendee = false;

      // Only consider this event if it has attendees in common with our meeting.
      while(it.hasNext()) {
        if(meetingAttendees.contains(it.next())) {
          hasOneCommonAttendee = true;
        }
      }


      if(hasOneCommonAttendee) {
      /** Only insert the event if it isn't fully contained by the previous event.
        *  We can safely do this now, beacuse all the events with no attendees have
        *  been removed.
        */
        if(trimmedEvents.isEmpty()) {
          trimmedEvents.add(events.get(i));
        }
        else {
          if(!trimmedEvents.get(trimmedEvents.size() - 1).getWhen().contains(events.get(i).getWhen())) {
          trimmedEvents.add(events.get(i));
          }
        }
      }
    }
    return trimmedEvents;
  }
}
