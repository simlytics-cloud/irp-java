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

# 📁 Where You Will Work

You only need to modify these files:

```
src/.../ExerciseAdapter.java
src/.../ScenarioBuilder.java
```

Search for:

```
TODO(tutorial-1)
```

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
