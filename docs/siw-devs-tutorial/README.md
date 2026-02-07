# DEVS Tutorial — Starter: Inventory Routing Problem

Welcome to the **DEVS Tutorial starter project**. In this exercise you will implement a small but complete workflow using [DEVS Streaming Framework](https://github.com/simlytics-cloud/devs-streaming) inside a working sample [Inventory Routing Problem](https://github.com/simlytics-cloud/irp-java) application.

This starter branch contains:

* runnable project scaffold
* tests
* stubbed classes with `TODO` markers
* step-by-step instructions below

The code compiles, but tests (or runtime checks) will fail until you complete the TODO sections.

---

# 🎯 Learning Objectives

By the end of this tutorial you will be able to:

* Use [DEVS Streaming Framework](https://github.com/simlytics-cloud/devs-streaming) to build a simple DEVS model.
* Run and validate results with the provided tests

---

# 🧰 Prerequisites

You will need:

* (Optional) Local toolchain:
  * Git
  * Java 21
  * Maven 3.9.X
* **OR** use Codespaces (recommended — zero setup)

---

# 🚀 Quick Start (Recommended: Codespaces)

1. Click **Code → Open in Codespaces**
2. Wait for the environment to build
3. Open a terminal
4. Run:

```
<build/run command>
```

You should see:

* project builds
* tests run
* failures reported in tutorial exercises

---

# 💻 Quick Start (Local Machine)

```
git clone <repo-url>
cd <repo>
<build command>
<run tests command>
```

If dependencies fail, see `docs/setup.md`.

---

# Retailer Model Tutorial — Scheduled DEVS Implementation Guide

This tutorial page explains how the **Retailer** model is implemented for the Inventory Routing Problem using the **DEVS Streaming Framework scheduled model pattern**. It provides the architectural background needed to complete the `RetailerImpl` exercise class.

You should read this before implementing the TODO sections in `RetailerImpl`.

---

# Overview

The Retailer model is built using the DEVS Streaming Framework’s **scheduled model base classes**. Much of the simulation timing and event mechanics are already implemented in the framework. Your job is to implement the **domain behavior**, not the simulation engine mechanics.

The inheritance chain is:

```
ScheduledDevsModel (DEVS Streaming Framework)
    ↑
Facility (generated)
    ↑
Retailer (generated)
    ↑
RetailerImpl (your implementation)
```

Before coding, review:

* the generated `Retailer` class (in the generated module)
* the generated `Facility` base class (in the generted module)
* the framework `ScheduledDevsModel` class in DEVS Streaming Framework
* completed examples: `VehicleImpl`, `ManufacturerImpl`

---

# ScheduledDevsModel Execution Model

`ScheduledDevsModel` provides a schedule-driven execution model. Its internal state extends:

```
ScheduleState
```

This means the model state includes:

* a **Schedule**
* the **current simulation time**

Because of this, the framework already implements several DEVS functions for you.

---

# Provided by the Framework

## Time Advance Function

The time advance function is already implemented.

Behavior:

* Returns the interval between:

  * current simulation time
  * the first scheduled item in the schedule

You do not implement time advance yourself.

---

## Output Function

The output function is already implemented.

Behavior:

* Returns a **bag of PortValues**
* Includes all outputs scheduled for the current simulation time

Outputs are pulled directly from the schedule.

---

## Internal State Transition

The internal transition function is also implemented by the framework.

It automatically:

1. Advances current simulation time
2. Removes published outputs from the schedule
3. Retrieves all scheduled internal events at the current time
4. Passes those events to your handler method

You do NOT override the internal transition directly.

Instead, you implement:

```
public void handleScheduledEvents(List<Object> events)
```

This is where your model reacts to scheduled internal events.

---

# Working with the Schedule

The schedule supports both:

* internal events
* scheduled outputs

A common pattern is to define **inner classes** to represent internal events.

---

## Scheduling Internal Events

Example — schedule a store opening event 6 hours after simulation start:

```java
modelState.getSchedule().scheduleInternalEvent(
    LongSimTime.create(60 * 6),
    new OpenEvent()
);
```

Typical usage:

* define small inner event classes
* schedule them at future times
* handle them inside `handleScheduledEvents`

---

## Scheduling Outputs

You can schedule output port values directly onto the schedule.

Example — publish daily inventory cost on the Retailers dailyInventoryCost port:

```java
modelState.getSchedule().scheduleOutput(
    currentTime, // Time of the scheduled output
    Retailer.dailyInventoryCost, // Port on which to place the output
    immutableInventoryCost // The output data structure consitent with the port tyep
);
```

These outputs will automatically be emitted by the framework output function at the scheduled time.

---

# Role of the Facility Base Class

`Facility` is a lightweight shared parent class.

Purpose:

* holds shared state values
* holds shared properties
* used by both:

  * Retailer
  * Manufacturer

You typically do not modify this class for the tutorial.

---

# External State Transition in Retailer

The Retailer model includes an external transition that is already partially implemented.

Framework behavior:

1. Advances current time correctly
2. Iterates over incoming PortValues
3. Dispatches by:

   * port name
   * value type

Incoming deliveries are forwarded to:

```
handleReceiveDelivery(
    ImmutableDelivery immutableDelivery,
    LongSimTime elapsedTime
)
```

You are responsible for completing this handler.

---

# What You Must Implement

For the Retailer tutorial exercise, you must complete the domain logic for:

## Delivery Handling

Implement:

```
handleReceiveDelivery(...)
```


---

## Scheduled Internal Events Handling

Implement:

```
handleScheduledEvents(List<Object> events)
```

Typical responsibilities:

* loop through event objects
* type-check events
* update model state
* schedule follow-on events and outputs

The framework already:

* pulls events from the schedule
* groups them by time
* calls your handler

---

## Confluent Transition

Implement the confluent transition behavior where:

* internal and external events occur at the same simulation time

A typical strategy is to decide whether you want to handle the input before or after
the internal event.  Based on that decisioin, call `externalStateTransitionFunction(LongSimTime.create(0), inputs);`
and `internalStateTransitionFunction();` in the corresponding order.  

---

# Time Representation

Simulation time uses:

```
LongSimTime
```

This represents:

> minutes since simulation start

To get the numeric value:

```java
long minutes = currentTime.getT();
```

To create a new value:

```java
LongSimTime scheduledEventTime = LongSimTime.create(60 * 14);
```

Use this for:

* computing delays
* scheduling future events
* calculating costs or penalties

---

# Recommended Study References

Before completing the Retailer implementation, review:

* `VehicleImpl` — completed scheduled model example
* `ManufacturerImpl` — completed facility example
* DEVS Streaming Framework example models
* Framework test models

Focus on:

* event class patterns
* scheduling style
* handler structure
* output scheduling patterns

---

# Implementation Strategy

Suggested order:

1. Read generated `Retailer` class
2. Review `ScheduledDevsModel` schedule + handler pattern
3. Study `VehicleImpl`
4. Implement delivery handler
5. Implement scheduled event handler
6. Implement confluent logic
7. Run tests and scenarios

---

# Next Step

Return to:

```
RetailerImpl
```

Search for:

```
TODO(tutorial-1)
```

and implement the required behavior using the scheduling and handler patterns described here.


Do not modify other files unless instructed.

---

# 🧪 Baseline Check

Run tests now:

```
<test command>
```

Expected result:

```
Tests run: XX
Failures: > 0   ✅ (this is expected)
```

Failures indicate unimplemented tutorial steps.

---

# 📚 Tutorial Steps

---

## Step 1 — Wire the Library Entry Point

Open:

```
src/.../ExerciseAdapter.java
```

Find:

```java
// TODO(tutorial-1 step-1):
// Create and configure the <LibraryClient>
```

Implement:

* construct client
* minimal configuration only
* do NOT enable advanced options

Run tests again.

---

## Step 2 — Build the Scenario

Open:

```
src/.../ScenarioBuilder.java
```

Find:

```java
// TODO(tutorial-1 step-2):
// Map input data into <LibraryModel>
```

Implement:

* field mappings
* required attributes only
* ignore optional extensions

Run tests.

---

## Step 3 — Execute and Capture Output

Return to:

```
ExerciseAdapter.java
```

Find:

```java
// TODO(tutorial-1 step-3):
// Execute and return result
```

Implement:

* invoke library
* capture result
* convert to expected return type

Run tests.

---

# ✅ Completion Criteria

You are done when:

* All tests pass
* Application runs without errors
* Output matches expected sample in:

```
docs/tutorial-1/expected-output.txt
```

---

# 🔍 Compare With Solution (Optional)

If you get stuck, you can diff against the solution branch:

```
git diff tutorial-1-solution -- src/.../ExerciseAdapter.java
```

Or browse:

```
Branch → tutorial-1-solution
```

Try to use this only after attempting the exercise.

---

# 🧭 Troubleshooting

## Build fails

Run:

```
<clean command>
<build command>
```

## Tests cannot find resources

Check working directory is project root.

## Codespaces rebuild needed

Command palette → “Rebuild Container”.

---

# 📖 Additional Reading

* `docs/tutorial-1/architecture.md`
* `docs/tutorial-1/data-flow.md`

---

# 📝 Notes for Learners

* Keep changes minimal
* Follow TODO markers exactly
* Avoid adding extra frameworks or helpers
* The goal is correct integration, not optimization

---

# 🧪 For Instructors / Reviewers

Solution branch:

```
tutorial-1-solution
```

Reference tag (if provided):

```
tut1-<version>
```

---

# ▶ Next Tutorial

Proceed to:

```
tutorial-2-starter branch
```

which introduces:

* <next feature>
* <more advanced scenario>

---

Happy building.
