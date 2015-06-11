# mingming Design Notes

Right now this is not intended to provide a design overview. It is jsut a random collection of notes
about the design that capture some of the decisions made.

## Status Reporting

Workers report their status, including the status of all their work, by default every 5 seconds. At some point
this should become just another type of work, schedulable by the controller. All status metrics are cumulative,
which makes status reporting calls idempotent. It also enables support for multiple controllers, including
controllers that proxy others (a planned future feature).
